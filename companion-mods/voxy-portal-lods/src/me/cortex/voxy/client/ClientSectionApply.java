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
    private static final int IN_FLIGHT_BATCHES = 64;

    private static long sectionsWritten;
    private static long renderNotifies;
    private static long renderMisses;
    private static long engineMismatches;
    private static long distinctColumns;
    private static long batchesDone;
    private static boolean loggedEngine;
    private static Field queueField;
    private static Field appliedField;
    private static Field completedField;
    private static Field callbackField;

    private ClientSectionApply() {
    }

    public static String line() {
        return "sectionsWritten=" + sectionsWritten
                + " distinctColumns=" + distinctColumns
                + " batchesDone=" + batchesDone
                + " renderNotifies=" + renderNotifies
                + " renderMisses=" + renderMisses
                + " engineMismatches=" + engineMismatches;
    }

    /** Client thread. Pulls queued batches and hands them to the apply worker. */
    @SuppressWarnings("unchecked")
    public static void drain() {
        flushAcks();
        TerrainApplyQueue<SectionSyncBatchPayload> queue = queue();
        if (queue == null) {
            return;
        }
        if (VoxyConfig.CONFIG == null || !VoxyConfig.CONFIG.serverTerrainSyncReceive) {
            queue.clear();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || QUEUED.get() >= IN_FLIGHT_BATCHES) {
            return;
        }
        RegistryAccess registries = level.registryAccess();
        int min = level.getMinSection();
        int max = level.getMaxSection();
        WorldIdentifier current = WorldIdentifier.of(level);
        WorldEngine shown = renderEngine(minecraft);
        ArrayList<TerrainApplyQueue.Item<SectionSyncBatchPayload>> batch = new ArrayList<TerrainApplyQueue.Item<SectionSyncBatchPayload>>();
        TerrainApplyQueue.Item<SectionSyncBatchPayload> item;
        while (QUEUED.get() + batch.size() < IN_FLIGHT_BATCHES && (item = queue.peek()) != null) {
            queue.remove(item);
            batch.add(item);
        }
        if (batch.isEmpty()) {
            return;
        }
        QUEUED.addAndGet(batch.size());
        WORKER.execute(() -> apply(batch, registries, min, max, current, shown));
    }

    private static void apply(List<TerrainApplyQueue.Item<SectionSyncBatchPayload>> batch, RegistryAccess registries, int min, int max, WorldIdentifier current, WorldEngine shown) {
        try {
            VoxyInstance instance = VoxyCommon.getInstance();
            for (int i = 0; i < batch.size(); i++) {
                TerrainApplyQueue.Item<SectionSyncBatchPayload> item = batch.get(i);
                SectionSyncBatchPayload payload = item.payload;
                WorldIdentifier world = world(payload);
                if (instance == null || current == null || world == null || !world.equals(current)) {
                    ACKS.add(new long[] {item.id, 2L});
                    continue;
                }
                WorldEngine stored = instance.getOrCreate(world);
                WorldEngine target = stored;
                if (shown != null && shown != stored && live(shown)) {
                    target = shown;
                    engineMismatches++;
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
                            + " world=" + world);
                }
                try {
                    int written = 0;
                    List<byte[]> sections = payload.sectionDatas();
                    for (int s = 0; s < sections.size(); s++) {
                        RegistryFriendlyByteBuf buf = buffer(sections.get(s), registries);
                        try {
                            VoxelizedSection section = VoxelSectionWireCodec.readSection(buf, target);
                            if (section.y < min || section.y >= max) {
                                throw new IllegalArgumentException("section outside height");
                            }
                            WorldVoxilizedSectionMipper.mipSection(section, target.getMapper());
                            WorldUpdater.insertUpdate(target, section);
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
                    ACKS.add(new long[] {item.id, 1L});
                }
            }
        } finally {
            QUEUED.addAndGet(-batch.size());
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

    private static void flushAcks() {
        long[] ack;
        while ((ack = ACKS.poll()) != null) {
            if (Minecraft.getInstance().getConnection() != null) {
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
