package me.cortex.voxy.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.SectionSyncBatchPayload;
import me.cortex.voxy.common.network.TerrainApplyQueue;
import me.cortex.voxy.common.network.VoxelSectionWireCodec;
import me.cortex.voxy.common.network.VoxyChunkAckPayload;
import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.VoxyInstance;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;

/**
 * Applies terrain sections off the render thread and writes them into the
 * engine the renderer is actually using. Acks go back on the client thread.
 */
public final class ClientSectionApply {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "voxy-terrain-apply");
        thread.setDaemon(true);
        return thread;
    });
    private static final ConcurrentLinkedQueue<long[]> ACKS = new ConcurrentLinkedQueue<long[]>();
    private static final AtomicInteger QUEUED = new AtomicInteger();
    private static final Set<Long> COLUMNS = new HashSet<Long>();
    /** Matches the server in-flight window so a slow client pushes back instead of buffering the world. */
    static final int IN_FLIGHT_BATCHES = 48;
    static final int IN_FLIGHT_BYTES = 8 * 1024 * 1024;

    private static long sectionsWritten;
    private static long renderNotifies;
    private static long renderMisses;
    private static long engineMismatches;
    private static long distinctColumns;
    private static long batchesDone;
    private static long renderThreadNanos;
    private static long renderTicks;
    private static long decodeNanos;
    private static long mipNanos;
    private static long writeNanos;
    private static int queuedBatches;
    private static int queuedBytes;
    /** Harness-only engine. Production leaves this null and uses the live renderer. */
    private static volatile WorldEngine harnessEngine;
    private static boolean loggedEngine;
    private static volatile String applyError;
    private static Field queueField;
    private static Field appliedField;
    private static Field completedField;
    private static Field callbackField;

    private ClientSectionApply() {
    }

    public static String line() {
        long ticks = Math.max(1L, renderTicks);
        return "sectionsWritten=" + sectionsWritten
                + " distinctColumns=" + distinctColumns
                + " batchesDone=" + batchesDone
                + " renderNotifies=" + renderNotifies
                + " renderMisses=" + renderMisses
                + " engineMismatches=" + engineMismatches
                + " queueBatches=" + queuedBatches
                + " queueBytes=" + queuedBytes
                + " acksSent=" + acksSent
                + " renderThreadUs=" + (renderThreadNanos / ticks / 1_000L)
                + " decodeUs=" + micros(decodeNanos)
                + " mipUs=" + micros(mipNanos)
                + " writeUs=" + micros(writeNanos)
                + " " + TerrainRenderStats.line();
    }

    private static long micros(long nanos) {
        long sections = Math.max(1L, sectionsWritten);
        return nanos / sections / 1_000L;
    }

    /** Mean time spent inside {@link #drain()} on the client thread, in microseconds. */
    public static long renderThreadMicros() {
        long ticks = Math.max(1L, renderTicks);
        return renderThreadNanos / ticks / 1_000L;
    }

    public static int queuedBatches() {
        return queuedBatches;
    }

    public static String applyError() {
        return applyError == null ? "" : applyError;
    }

    public static void useEngineForHarness(WorldEngine engine) {
        harnessEngine = engine;
    }

    public static long acks() {
        return acksSent;
    }

    /**
     * Render-thread body of the batch handler. Never throws: a bad batch is
     * nacked so the server retries or drops it instead of the packet task dying.
     */
    public static void ingest(SectionSyncBatchPayload payload) {
        try {
            int count = payload == null || payload.sectionDatas() == null ? 0 : payload.sectionDatas().size();
            if (count < 1 || count > me.cortex.voxy.common.network.TerrainWireLimits.MAX_BATCH_SECTIONS) {
                ack(payload == null ? 0L : payload.transferId(), 2);
                return;
            }
            if (!receiveEnabled()) {
                addCounter(droppedField(), count);
                ack(payload.transferId(), 2);
                return;
            }
            addCounter(receivedField(), count);
            addCounter(bytesField(), payload.totalSectionBytes());
            WorldEngine forced = harnessEngine;
            if (forced == null) {
                Minecraft minecraft = Minecraft.getInstance();
                Level level = minecraft.level;
                if (level == null || me.cortex.voxy.commonImpl.VoxyCommon.getInstance() == null) {
                    addCounter(retriedField(), 1);
                    ack(payload.transferId(), 1);
                    return;
                }
                if (!world(payload).equals(me.cortex.voxy.commonImpl.WorldIdentifier.of(level))) {
                    addCounter(droppedField(), count);
                    ack(payload.transferId(), 2);
                    return;
                }
            }
            TerrainApplyQueue<SectionSyncBatchPayload> pending = queue();
            TerrainApplyQueue.Offer result = pending.offer(payload.transferId(), payload, Math.max(1, payload.totalSectionBytes()), count, System.nanoTime() / 1_000_000L);
            if (result == TerrainApplyQueue.Offer.COMPLETED) {
                ack(payload.transferId(), 0);
            } else if (result == TerrainApplyQueue.Offer.FULL) {
                addCounter(retriedField(), 1);
                ack(payload.transferId(), 1);
            }
        } catch (Throwable failure) {
            logOnce(failure.toString());
            if (payload != null) {
                ack(payload.transferId(), 1);
            }
        }
    }

    private static long acksSent;
    private static long lastRejectLog;
    private static Field receivedField;
    private static Field droppedField;
    private static Field retriedField;
    private static Field bytesField;

    static void logOnce(String message) {
        long now = System.nanoTime();
        if (now - lastRejectLog < 5_000_000_000L) {
            return;
        }
        lastRejectLog = now;
        me.cortex.voxy.common.Logger.warn("[terrain-sync][client] " + message);
    }

    private static boolean receiveEnabled() {
        try {
            if (VoxyConfig.CONFIG == null) {
                return true;
            }
            return VoxyConfig.CONFIG.serverTerrainSyncReceive;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static void ack(long id, int status) {
        acksSent++;
        if (harnessEngine != null || id <= 0L) {
            return;
        }
        try {
            if (Minecraft.getInstance().getConnection() != null) {
                PacketDistributor.sendToServer(new VoxyChunkAckPayload(id, status), new CustomPacketPayload[0]);
            }
        } catch (Throwable ignored) {
            // The batch was already rejected. A missing connection must not escape the handler.
        }
    }

    private static Field receivedField() {
        if (receivedField == null) {
            receivedField = counter("received");
        }
        return receivedField;
    }

    private static Field droppedField() {
        if (droppedField == null) {
            droppedField = counter("dropped");
        }
        return droppedField;
    }

    private static Field retriedField() {
        if (retriedField == null) {
            retriedField = counter("retried");
        }
        return retriedField;
    }

    private static Field bytesField() {
        if (bytesField == null) {
            bytesField = counter("bytesReceived");
        }
        return bytesField;
    }

    /** Client thread. Pulls queued batches and hands them to the apply worker. Decode stays off this thread. */
    public static void drain() {
        long started = System.nanoTime();
        try {
            flushAcks(true);
            TerrainApplyQueue<SectionSyncBatchPayload> queue = queue();
            if (queue == null) {
                return;
            }
            if (VoxyConfig.CONFIG == null || !VoxyConfig.CONFIG.serverTerrainSyncReceive) {
                queue.clear();
                queuedBatches = 0;
                queuedBytes = 0;
                return;
            }
            if (QUEUED.get() >= IN_FLIGHT_BATCHES) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            if (level == null) {
                return;
            }
            pump(queue, level.registryAccess(), level.getMinSection(), level.getMaxSection(), WorldIdentifier.of(level), renderEngine(minecraft));
        } finally {
            renderThreadNanos += System.nanoTime() - started;
            renderTicks++;
            // A full in-flight queue returns before pump(). The line still has to print.
            reportClient();
        }
    }

    public static void reportNow() {
        me.cortex.voxy.common.Logger.info("[terrain-sync][client] " + TerrainClientStats.line());
    }

    private static long lastClientReport;

    private static void reportClient() {
        long now = System.nanoTime();
        if (now - lastClientReport < 5_000_000_000L) {
            return;
        }
        lastClientReport = now;
        reportNow();
    }

    /**
     * Same steal-and-hand-off as {@link #drain()}, without touching {@link Minecraft}.
     * The harness calls this once per client tick.
     */
    public static void harnessTick(RegistryAccess registries, int minSection, int maxSection, WorldEngine engine) {
        long started = System.nanoTime();
        try {
            flushAcks(false);
            useEngineForHarness(engine);
            TerrainApplyQueue<SectionSyncBatchPayload> queue = queue();
            if (queue == null || QUEUED.get() >= IN_FLIGHT_BATCHES) {
                return;
            }
            pump(queue, registries, minSection, maxSection, null, engine);
        } finally {
            renderThreadNanos += System.nanoTime() - started;
            renderTicks++;
        }
    }

    @SuppressWarnings("unchecked")
    private static void pump(TerrainApplyQueue<SectionSyncBatchPayload> queue, RegistryAccess registries, int min, int max, WorldIdentifier current, WorldEngine shown) {
        ArrayList<TerrainApplyQueue.Item<SectionSyncBatchPayload>> batch = new ArrayList<TerrainApplyQueue.Item<SectionSyncBatchPayload>>();
        int bytes = queuedBytes;
        TerrainApplyQueue.Item<SectionSyncBatchPayload> item;
        while (QUEUED.get() + batch.size() < IN_FLIGHT_BATCHES && bytes < IN_FLIGHT_BYTES && (item = queue.peek()) != null) {
            if (!batch.isEmpty() && bytes + item.bytes > IN_FLIGHT_BYTES) {
                break;
            }
            queue.remove(item);
            batch.add(item);
            bytes += item.bytes;
        }
        queuedBatches = queue.size() + QUEUED.get() + batch.size();
        queuedBytes = bytes;
        if (batch.isEmpty()) {
            return;
        }
        QUEUED.addAndGet(batch.size());
        WorldIdentifier worldNow = current;
        WorldEngine shownNow = shown;
        WORKER.execute(() -> apply(batch, registries, min, max, worldNow, shownNow));
    }

    private static void apply(List<TerrainApplyQueue.Item<SectionSyncBatchPayload>> batch, RegistryAccess registries, int min, int max, WorldIdentifier current, WorldEngine shown) {
        int batchBytes = 0;
        for (int i = 0; i < batch.size(); i++) {
            batchBytes += batch.get(i).bytes;
        }
        try {
            VoxyInstance instance = VoxyCommon.getInstance();
            WorldEngine forced = harnessEngine;
            for (int i = 0; i < batch.size(); i++) {
                TerrainApplyQueue.Item<SectionSyncBatchPayload> item = batch.get(i);
                SectionSyncBatchPayload payload = item.payload;
                WorldEngine stored;
                WorldEngine target;
                if (forced != null) {
                    stored = forced;
                    target = forced;
                } else {
                    WorldIdentifier world = world(payload);
                    if (instance == null || current == null || world == null || !world.equals(current)) {
                        ACKS.add(new long[] {item.id, 2L});
                        continue;
                    }
                    stored = instance.getOrCreate(world);
                    target = stored;
                    if (shown != null && shown != stored && live(shown)) {
                        target = shown;
                        engineMismatches++;
                    }
                }
                if (target == null || !live(target)) {
                    ACKS.add(new long[] {item.id, 1L});
                    continue;
                }
                boolean armed = callbackArmed(target);
                if (!loggedEngine) {
                    loggedEngine = true;
                    me.cortex.voxy.common.Logger.info("[terrain-sync][client] apply engine=" + System.identityHashCode(target)
                            + " stored=" + System.identityHashCode(stored)
                            + " renderCallback=" + armed
                            + " offThread=voxy-terrain-apply");
                }
                try {
                    int written = 0;
                    List<byte[]> sections = payload.sectionDatas();
                    for (int s = 0; s < sections.size(); s++) {
                        RegistryFriendlyByteBuf buf = buffer(sections.get(s), registries);
                        try {
                            long startedDecode = System.nanoTime();
                            VoxelizedSection section = VoxelSectionWireCodec.readSection(buf, target);
                            long startedMip = System.nanoTime();
                            if (section.y < min || section.y >= max) {
                                throw new IllegalArgumentException("section outside height");
                            }
                            WorldVoxilizedSectionMipper.mipSection(section, target.getMapper());
                            long startedWrite = System.nanoTime();
                            WorldUpdater.insertUpdate(target, section);
                            long wrote = System.nanoTime();
                            decodeNanos += startedMip - startedDecode;
                            mipNanos += startedWrite - startedMip;
                            writeNanos += wrote - startedWrite;
                            noteColumn(section.x, section.z);
                            written++;
                            if (armed) {
                                renderNotifies++;
                            } else {
                                renderMisses++;
                            }
                        } finally {
                            buf.release();
                        }
                    }
                    sectionsWritten += written;
                    addCounter(appliedField(), written);
                    batchesDone++;
                    addCounter(completedField(), 1);
                    ACKS.add(new long[] {item.id, 0L});
                } catch (Throwable failure) {
                    if (applyError == null) {
                        StringBuilder text = new StringBuilder(failure.toString());
                        int frames = 0;
                        for (StackTraceElement frame : failure.getStackTrace()) {
                            text.append(" | ").append(frame);
                            if (++frames == 12) {
                                break;
                            }
                        }
                        applyError = text.toString();
                    }
                    ACKS.add(new long[] {item.id, 1L});
                }
            }
        } finally {
            QUEUED.addAndGet(-batch.size());
            queuedBatches = Math.max(0, queuedBatches - batch.size());
            queuedBytes = Math.max(0, queuedBytes - batchBytes);
        }
    }

    private static RegistryFriendlyByteBuf buffer(byte[] bytes, RegistryAccess registries) throws Exception {
        ByteBuf raw = Unpooled.wrappedBuffer(bytes);
        try {
            return (RegistryFriendlyByteBuf) RegistryFriendlyByteBuf.class
                    .getConstructor(ByteBuf.class, RegistryAccess.class, ConnectionType.class)
                    .newInstance(raw, registries, ConnectionType.NEOFORGE);
        } catch (NoSuchMethodException missing) {
            return new RegistryFriendlyByteBuf(raw, registries);
        }
    }

    private static void noteColumn(int x, int z) {
        long key = (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
        synchronized (COLUMNS) {
            if (COLUMNS.add(Long.valueOf(key))) {
                distinctColumns = COLUMNS.size();
            }
        }
    }

    private static void flushAcks(boolean sendPackets) {
        long[] ack;
        while ((ack = ACKS.poll()) != null) {
            acksSent++;
            if (sendPackets && Minecraft.getInstance().getConnection() != null) {
                PacketDistributor.sendToServer(new VoxyChunkAckPayload(ack[0], (int) ack[1]), new CustomPacketPayload[0]);
            }
        }
    }

    private static boolean live(WorldEngine engine) {
        try {
            return ((Boolean) engine.getClass().getMethod("isLive").invoke(engine)).booleanValue();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static boolean callbackArmed(WorldEngine engine) {
        try {
            if (callbackField == null) {
                callbackField = WorldEngine.class.getDeclaredField("dirtyCallback");
                callbackField.setAccessible(true);
            }
            return callbackField.get(engine) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** The engine the renderer is drawing. Coverage scans this when the level lookup is empty. */
    public static WorldEngine shownEngine() {
        if (harnessEngine != null) {
            return harnessEngine;
        }
        try {
            return renderEngine(Minecraft.getInstance());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static WorldEngine renderEngine(Minecraft minecraft) {
        try {
            Object renderer = minecraft.getClass().getField("levelRenderer").get(minecraft);
            if (renderer == null) {
                return null;
            }
            Object system = renderer.getClass().getMethod("voxy$getRenderSystem").invoke(renderer);
            if (system == null) {
                return null;
            }
            Object engine = system.getClass().getMethod("getEngine").invoke(system);
            return engine instanceof WorldEngine ? (WorldEngine) engine : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static WorldIdentifier world(SectionSyncBatchPayload payload) {
        try {
            Class<?> handler = Class.forName("me.cortex.voxy.common.network.TerrainSyncClientHandler");
            java.lang.reflect.Method method = handler.getDeclaredMethod("world", SectionSyncBatchPayload.class);
            method.setAccessible(true);
            return (WorldIdentifier) method.invoke(null, payload);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static TerrainApplyQueue<SectionSyncBatchPayload> queue() {
        try {
            if (queueField == null) {
                Class<?> handler = Class.forName("me.cortex.voxy.common.network.TerrainSyncClientHandler");
                queueField = handler.getDeclaredField("QUEUE");
                queueField.setAccessible(true);
            }
            return (TerrainApplyQueue<SectionSyncBatchPayload>) queueField.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Field appliedField() {
        if (appliedField == null) {
            appliedField = counter("applied");
        }
        return appliedField;
    }

    private static Field completedField() {
        if (completedField == null) {
            completedField = counter("completed");
        }
        return completedField;
    }

    private static Field counter(String name) {
        try {
            Field field = Class.forName("me.cortex.voxy.common.network.TerrainSyncClientHandler").getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void addCounter(Field field, int amount) {
        if (field == null || amount == 0) {
            return;
        }
        try {
            field.setLong(null, field.getLong(null) + amount);
        } catch (Throwable ignored) {
        }
    }
}
