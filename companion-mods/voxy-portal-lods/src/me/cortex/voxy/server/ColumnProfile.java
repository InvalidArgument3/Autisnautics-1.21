package me.cortex.voxy.server;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Headless measurement of one stationary recipient. Enabled with
 * {@code -Dvoxy.columnProfile=before}. Pregenerates a square, then drives the
 * real ring for two minutes and records per-stage times.
 */
public final class ColumnProfile {
    public static volatile boolean suppressWalker;
    public static volatile boolean timing;
    /** When set, the measured walk uses the background reader instead of the server thread. */
    public static volatile boolean asyncMeasure;

    private static final int CAP = 100_000;
    private static final Map<String, Samples> SAMPLES = new ConcurrentHashMap<String, Samples>();
    private static final Map<String, long[]> SKIPS = new LinkedHashMap<String, long[]>();
    private static int cells;
    private static int sent;
    private static int sections;
    private static int wires;
    private static long bytes;
    private static int[] cellsPerTick = new int[20_000];
    private static int measuredTicks;
    private static long measureStart;
    private static int pregenDone;
    private static int pregenFailed;
    private static int pregenQueued;
    private static int scanRadius;
    private static int scanX = Integer.MIN_VALUE;
    private static int scanZ;
    private static final ArrayList<CompletableFuture<?>> IN_FLIGHT = new ArrayList<CompletableFuture<?>>();
    private static final int IN_FLIGHT_CAP = 48;
    private static int phase;
    private static ServerLevel level;
    private static WorldEngine engine;
    private static ServerPlayer player;
    private static int originX;
    private static int originZ;
    private static int cursor;
    private static long lastLog;
    private static boolean loggedFailure;

    private ColumnProfile() {
    }

    public static void onTick() {
        String mode = System.getProperty("voxy.columnProfile", "");
        if ((!"before".equals(mode) && !"after".equals(mode)) || phase == 3) {
            return;
        }
        asyncMeasure = "after".equals(mode);
        suppressWalker = true;
        try {
            tick(ServerLifecycleHooks.getCurrentServer());
        } catch (Throwable failure) {
            if (!loggedFailure) {
                loggedFailure = true;
                Logger.warn("[terrain-sync][profile] tick failed", failure);
            }
        }
    }

    public static synchronized void cell() {
        cells++;
    }

    public static synchronized void sent() {
        sent++;
    }

    public static synchronized void skip(String reason) {
        long[] count = SKIPS.get(reason);
        if (count == null) {
            count = new long[1];
            SKIPS.put(reason, count);
        }
        count[0]++;
    }

    public static synchronized void add(String stage, long nanos) {
        if (nanos < 0L) {
            return;
        }
        Samples samples = SAMPLES.computeIfAbsent(stage, key -> new Samples());
        samples.add(nanos);
    }

    public static synchronized void noteColumn(int raw, int compressed, long compressNs, long decompressNs, boolean roundTrip) {
        add("rawBytes", raw);
        add("compressedBytes", compressed);
        add("compress", compressNs);
        add("decompress", decompressNs);
        if (!roundTrip) {
            skip("roundtrip");
        }
    }

    public static synchronized void sizes(int converted, int encoded, int wireBytes) {
        sections += converted;
        wires += encoded;
        bytes += wireBytes;
    }

    private static void tick(MinecraftServer server) {
        if (server == null || !server.isRunning()) {
            return;
        }
        if (phase == 0) {
            ServerLevel overworld = server.overworld();
            if (overworld == null || VoxyCommon.getInstance() == null) {
                return;
            }
            level = overworld;
            engine = VoxyCommon.getInstance().getOrCreate(WorldIdentifier.of(level), true);
            if (engine == null) {
                return;
            }
            BlockPos spawn = level.getSharedSpawnPos();
            originX = spawn.getX() >> 4;
            originZ = spawn.getZ() >> 4;
            if (Boolean.getBoolean("voxy.profileSkipPregen")) {
                phase = 2;
                Logger.info("[terrain-sync][profile] using the chunks already generated around " + originX + "," + originZ);
            } else {
                phase = 1;
                Logger.info("[terrain-sync][profile] pregenerating radius " + radius() + " around chunk " + originX + "," + originZ);
            }
        }
        if (phase == 1) {
            pregenerate();
            return;
        }
        if (player == null) {
            player = install(server);
            if (player == null) {
                return;
            }
            timing = true;
            measureStart = System.nanoTime();
            Logger.info("[terrain-sync][profile] measuring for " + seconds() + "s");
        }
        int before = cells;
        TerrainDispatch.armDeadline(5_000_000L);
        TerrainDispatch.sendOnce(level, engine, player, 10, 512);
        if (measuredTicks < cellsPerTick.length) {
            cellsPerTick[measuredTicks++] = cells - before;
        }
        long elapsed = System.nanoTime() - measureStart;
        long now = System.currentTimeMillis();
        if (now - lastLog > 5000L) {
            lastLog = now;
            Logger.info("[terrain-sync][profile] measure sent=" + sent + " ticks=" + measuredTicks + " seconds=" + String.format("%.1f", elapsed / 1_000_000_000.0));
            try {
                Files.writeString(Path.of(asyncMeasure ? "/tmp/voxy-column-profile-after.txt" : "/tmp/voxy-column-profile-before.txt"), report(elapsed));
            } catch (Exception ignored) {
                // The log line remains if the disk write fails.
            }
        }
        if (elapsed >= seconds() * 1_000_000_000L) {
            finish(server, elapsed);
        }
    }

    private static void pregenerate() {
        int radius = radius();
        int total = (radius * 2 + 1) * (radius * 2 + 1);
        for (int i = IN_FLIGHT.size() - 1; i >= 0; i--) {
            CompletableFuture<?> future = IN_FLIGHT.get(i);
            if (!future.isDone()) {
                continue;
            }
            IN_FLIGHT.remove(i);
            try {
                future.join();
                pregenDone++;
            } catch (Throwable failure) {
                pregenFailed++;
            }
        }
        int[] cell = new int[2];
        while (IN_FLIGHT.size() < IN_FLIGHT_CAP && nextCell(radius, cell)) {
            pregenQueued++;
            IN_FLIGHT.add(level.getChunkSource().getChunkFuture(originX + cell[0], originZ + cell[1], ChunkStatus.FULL, true));
        }
        long now = System.currentTimeMillis();
        if (now - lastLog > 5000L) {
            lastLog = now;
            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Logger.info("[terrain-sync][profile] pregen queued=" + pregenQueued + "/" + total + " full=" + pregenDone + " failed=" + pregenFailed + " flying=" + IN_FLIGHT.size() + " heapMB=" + (used / (1024 * 1024)));
        }
        if (pregenQueued >= total && IN_FLIGHT.isEmpty()) {
            phase = 2;
            Logger.info("[terrain-sync][profile] pregen complete full=" + pregenDone + " failed=" + pregenFailed);
        }
    }

    /** Nearest chebyshev ring first, so the walk has generated columns before the rim does. */
    private static boolean nextCell(int radius, int[] out) {
        if (scanX == Integer.MIN_VALUE) {
            scanRadius = 0;
            scanX = 0;
            scanZ = 0;
        }
        while (scanRadius <= radius) {
            if (scanRadius == 0) {
                out[0] = 0;
                out[1] = 0;
                scanRadius = 1;
                scanX = -1;
                scanZ = -1;
                return true;
            }
            while (scanZ <= scanRadius) {
                while (scanX <= scanRadius) {
                    int dx = scanX++;
                    if (Math.max(Math.abs(dx), Math.abs(scanZ)) == scanRadius) {
                        out[0] = dx;
                        out[1] = scanZ;
                        return true;
                    }
                }
                scanX = -scanRadius;
                scanZ++;
            }
            scanRadius++;
            scanX = -scanRadius;
            scanZ = -scanRadius;
        }
        return false;
    }

    private static ServerPlayer install(MinecraftServer server) {
        try {
            GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("voxy-column-profile".getBytes()), "VoxyProfile");
            FakePlayer body = new FakePlayer(level, profile);
            body.setPos(originX * 16.0 + 8.0, 80.0, originZ * 16.0 + 8.0);
            Connection connection = new Connection(PacketFlow.SERVERBOUND);
            java.lang.reflect.Field channel = Connection.class.getDeclaredField("channel");
            channel.setAccessible(true);
            channel.set(connection, new EmbeddedChannel());
            ServerGamePacketListenerImpl listener = new ServerGamePacketListenerImpl(server, connection, body, CommonListenerCookie.createInitial(profile, false));
            if (body.connection == null) {
                body.connection = listener;
            }
            me.cortex.voxy.common.network.VoxySessionSupport.setClientProtocol(body, 3, 512);
            return body;
        } catch (Throwable failure) {
            Logger.warn("[terrain-sync][profile] could not install the stationary recipient", failure);
            phase = 3;
            return null;
        }
    }

    private static void finish(MinecraftServer server, long elapsed) {
        phase = 3;
        timing = false;
        String report = report(elapsed);
        Logger.info(report);
        try {
            Files.writeString(Path.of(asyncMeasure ? "/tmp/voxy-column-profile-after.txt" : "/tmp/voxy-column-profile-before.txt"), report);
        } catch (Exception ignored) {
            // The log line is the copy that survives.
        }
        server.halt(false);
    }

    private static String report(long elapsed) {
        double seconds = Math.max(0.001, elapsed / 1_000_000_000.0);
        StringBuilder text = new StringBuilder();
        text.append("[terrain-sync][profile] ").append(asyncMeasure ? "after" : "before").append('\n');
        text.append("seconds=").append(String.format("%.1f", seconds));
        text.append(" ticks=").append(measuredTicks);
        text.append(" sent=").append(sent);
        text.append(" columnsPerSecond=").append(String.format("%.2f", sent / seconds));
        text.append(" tps=").append(String.format("%.2f", measuredTicks / seconds)).append('\n');
        text.append("cellsProbedPerTick median=").append(percentile(cellsPerTick, measuredTicks, 0.50));
        text.append(" p95=").append(percentile(cellsPerTick, measuredTicks, 0.95)).append('\n');
        text.append("pregen full=").append(pregenDone).append(" failed=").append(pregenFailed).append('\n');
        if (sent > 0) {
            text.append("sectionsPerColumn=").append(String.format("%.2f", sections / (double) sent));
            text.append(" wiresPerColumn=").append(String.format("%.2f", wires / (double) sent));
            text.append(" bytesPerColumn=").append(bytes / sent).append('\n');
        }
        for (Map.Entry<String, long[]> entry : SKIPS.entrySet()) {
            text.append("skip ").append(entry.getKey()).append('=').append(entry.getValue()[0]).append('\n');
        }
        Samples raw = SAMPLES.get("rawBytes");
        Samples compressed = SAMPLES.get("compressedBytes");
        if (raw != null && raw.count > 0 && compressed != null && compressed.count > 0) {
            text.append("wireCodec=").append(me.cortex.voxy.common.network.SectionWire.codecName());
            text.append(" rawMean=").append(raw.mean());
            text.append(" rawMedian=").append(raw.percentile(0.50));
            text.append(" rawP95=").append(raw.percentile(0.95));
            text.append(" compressedMean=").append(compressed.mean());
            text.append(" compressedMedian=").append(compressed.percentile(0.50));
            text.append(" compressedP95=").append(compressed.percentile(0.95));
            text.append(" ratio=").append(String.format("%.3f", compressed.mean() / Math.max(1.0, raw.mean()))).append('\n');
        }
        for (String stage : new String[] {"header", "regionRead", "nbtParse", "import", "convert", "encode", "offer", "compress", "decompress", "liveIngest", "total"}) {
            Samples samples = SAMPLES.get(stage);
            if (samples == null || samples.count == 0) {
                text.append(stage).append(" n=0\n");
                continue;
            }
            text.append(stage).append(" n=").append(samples.count);
            text.append(" meanUs=").append(samples.mean() / (stage.contains("Bytes") ? 1.0 : 1_000.0));
            text.append(" medianUs=").append(samples.percentile(0.50) / 1_000L);
            text.append(" p95Us=").append(samples.percentile(0.95) / 1_000L).append('\n');
        }
        return text.toString();
    }

    private static int percentile(int[] values, int count, double p) {
        if (count <= 0) {
            return -1;
        }
        int[] copy = Arrays.copyOf(values, count);
        Arrays.sort(copy);
        int index = (int) Math.min(count - 1, Math.max(0, Math.ceil(p * count) - 1.0));
        return copy[index];
    }

    private static int radius() {
        return Integer.getInteger("voxy.profileRadius", 64).intValue();
    }

    private static int seconds() {
        return Integer.getInteger("voxy.profileSeconds", 120).intValue();
    }

    private static final class Samples {
        private long[] values = new long[1024];
        private int count;

        void add(long nanos) {
            if (count >= CAP) {
                return;
            }
            if (count == values.length) {
                values = Arrays.copyOf(values, Math.min(CAP, values.length * 2));
            }
            values[count++] = nanos;
        }

        double mean() {
            if (count <= 0) {
                return 0.0;
            }
            long sum = 0L;
            for (int i = 0; i < count; i++) {
                sum += values[i];
            }
            return sum / (double) count;
        }

        long percentile(double p) {
            long[] copy = Arrays.copyOf(values, count);
            Arrays.sort(copy);
            int index = (int) Math.min(count - 1, Math.max(0, Math.ceil(p * count) - 1.0));
            return copy[index];
        }
    }
}
