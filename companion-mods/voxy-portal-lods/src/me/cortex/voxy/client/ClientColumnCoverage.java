package me.cortex.voxy.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.ColumnCoveragePayload;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import me.cortex.voxy.server.ColumnCoverageIndex;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Reads lod-0 keys out of the local Voxy database and sends the regions,
 * nearest first. An older server has no {@code voxy:column_coverage} channel,
 * and then nothing is sent.
 */
public final class ClientColumnCoverage {
    public static final int REGIONS_PER_TICK = 4;
    private static final int RESCAN_TICKS = 200;

    private static String worldId = "";
    private static String engineState = "-";
    private static String channelState = "-";
    private static int ticksUntilScan = 0;
    private static int scans;
    private static int keysFound;
    private static int regionsSent;
    private static final ArrayList<Long> QUEUE = new ArrayList<Long>();
    private static final Long2ObjectOpenHashMap<byte[]> PENDING = new Long2ObjectOpenHashMap<byte[]>();
    private static final Long2ObjectOpenHashMap<byte[]> SENT = new Long2ObjectOpenHashMap<byte[]>();

    private ClientColumnCoverage() {
    }

    public static void reset() {
        worldId = "";
        engineState = "-";
        channelState = "-";
        ticksUntilScan = 0;
        QUEUE.clear();
        PENDING.clear();
        SENT.clear();
    }

    public static void onClientTick() {
        try {
            tick();
        } catch (Throwable ignored) {
            // Coverage is an optimisation. Losing a scan must not break the client tick.
        }
    }

    private static void tick() throws ReflectiveOperationException {
        Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
        Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
        Object player = minecraftClass.getField("player").get(minecraft);
        Object listener = minecraftClass.getMethod("getConnection").invoke(minecraft);
        if (player == null || listener == null) {
            channelState = "no-player";
            return;
        }
        if (VoxyConfig.CONFIG == null || !VoxyConfig.CONFIG.serverTerrainSyncReceive) {
            channelState = "receive-off";
            return;
        }
        Object levelObject = player.getClass().getMethod("level").invoke(player);
        if (!(levelObject instanceof Level)) {
            levelObject = minecraftClass.getField("level").get(minecraft);
        }
        if (!(levelObject instanceof Level)) {
            engineState = "no-level";
            return;
        }
        Level level = (Level) levelObject;
        String id = worldId(level);
        WorldEngine engine = WorldIdentifier.ofEngineNullable(level);
        if (engine == null) {
            engineState = "null";
        } else if (engine.storage == null) {
            engineState = "no-storage";
        } else {
            engineState = "ok";
        }
        ResourceLocation channel = ColumnCoveragePayload.TYPE.id();
        boolean open = false;
        Object connection = listener.getClass().getMethod("getConnection").invoke(listener);
        if (connection instanceof Connection) {
            open = NetworkRegistry.hasChannel((Connection) connection, ConnectionProtocol.PLAY, channel);
        }
        channelState = open ? "ok" : "missing";
        if (!id.equals(worldId)) {
            worldId = id;
            ticksUntilScan = 0;
            QUEUE.clear();
            PENDING.clear();
            SENT.clear();
        }
        if (!open || !"ok".equals(engineState)) {
            return;
        }
        if (ticksUntilScan <= 0) {
            if (scan(level, player)) {
                ticksUntilScan = RESCAN_TICKS;
            }
        } else {
            ticksUntilScan--;
        }
        flush();
    }

    public static String stats() {
        return "scans=" + scans + " lod0=" + keysFound + " regionsSent=" + regionsSent
                + " storage=" + (worldId.isEmpty() ? "-" : worldId)
                + " engine=" + engineState
                + " channel=" + channelState;
    }

    /**
     * Reads lod-0 keys and returns the coverage packets a scan would send.
     * The client tick uses this, and the harness does too.
     */
    public static ArrayList<ColumnCoveragePayload> collect(WorldEngine engine, int playerX, int playerZ, int radius) {
        ArrayList<ColumnCoveragePayload> out = new ArrayList<ColumnCoveragePayload>();
        if (engine == null || engine.storage == null) {
            engineState = engine == null ? "null" : "no-storage";
            return out;
        }
        engineState = "ok";
        channelState = "ok";
        if (worldId.isEmpty()) {
            worldId = "minecraft:overworld";
        }
        scans++;
        Long2ObjectOpenHashMap<byte[]> found = new Long2ObjectOpenHashMap<byte[]>();
        int[] seen = new int[] {0};
        engine.storage.iteratePositions(0, sectionKey -> {
            seen[0]++;
            ColumnCoverageIndex.ingest(found, sectionKey, playerX, playerZ, radius);
        });
        keysFound += seen[0];
        int playerRegionX = playerX >> 5;
        int playerRegionZ = playerZ >> 5;
        ArrayList<Long> ordered = new ArrayList<Long>();
        for (long key : found.keySet()) {
            ordered.add(Long.valueOf(key));
        }
        ordered.sort((left, right) -> {
            int leftDistance = Math.max(Math.abs(ColumnCoverageIndex.regionX(left.longValue()) - playerRegionX), Math.abs(ColumnCoverageIndex.regionZ(left.longValue()) - playerRegionZ));
            int rightDistance = Math.max(Math.abs(ColumnCoverageIndex.regionX(right.longValue()) - playerRegionX), Math.abs(ColumnCoverageIndex.regionZ(right.longValue()) - playerRegionZ));
            return Integer.compare(leftDistance, rightDistance);
        });
        for (int i = 0; i < ordered.size(); i++) {
            long key = ordered.get(i).longValue();
            out.add(new ColumnCoveragePayload(ColumnCoverageIndex.regionX(key), ColumnCoverageIndex.regionZ(key), found.get(key).clone()));
        }
        return out;
    }

    private static boolean scan(Level level, Object player) throws ReflectiveOperationException {
        WorldEngine engine = WorldIdentifier.ofEngineNullable(level);
        if (engine == null || engine.storage == null) {
            return false;
        }
        Object chunk = player.getClass().getMethod("chunkPosition").invoke(player);
        int playerX = ((ChunkPos) chunk).x;
        int playerZ = ((ChunkPos) chunk).z;
        int radius = 0;
        if (VoxyConfig.CONFIG != null) {
            radius = Math.max(0, Math.round(VoxyConfig.CONFIG.sectionRenderDistance * 32.0f));
        }
        ArrayList<ColumnCoveragePayload> found = collect(engine, playerX, playerZ, radius);
        QUEUE.clear();
        PENDING.clear();
        for (int i = 0; i < found.size(); i++) {
            ColumnCoveragePayload payload = found.get(i);
            long key = ColumnCoverageIndex.regionKey(payload.regionX(), payload.regionZ());
            byte[] previous = SENT.get(key);
            if (previous != null && Arrays.equals(previous, payload.columns())) {
                continue;
            }
            PENDING.put(key, payload.columns());
            QUEUE.add(Long.valueOf(key));
        }
        return true;
    }

    private static void flush() {
        int budget = REGIONS_PER_TICK;
        while (budget > 0 && !QUEUE.isEmpty()) {
            long key = QUEUE.remove(0).longValue();
            byte[] bits = PENDING.remove(key);
            if (bits == null) {
                continue;
            }
            byte[] copy = bits.clone();
            PacketDistributor.sendToServer(new ColumnCoveragePayload(ColumnCoverageIndex.regionX(key), ColumnCoverageIndex.regionZ(key), copy));
            SENT.put(key, copy);
            regionsSent++;
            budget--;
        }
    }

    private static String worldId(Level level) {
        String dimension = String.valueOf(level.dimension().location());
        long seed = 0L;
        try {
            Object value = level.getClass().getMethod("getSeed").invoke(level);
            if (value instanceof Number) {
                seed = ((Number) value).longValue();
            }
        } catch (ReflectiveOperationException ignored) {
            seed = 0L;
        }
        return dimension + "#" + seed;
    }
}
