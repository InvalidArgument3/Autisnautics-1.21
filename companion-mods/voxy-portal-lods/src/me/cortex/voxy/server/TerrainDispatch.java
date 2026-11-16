package me.cortex.voxy.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.cortex.voxy.common.Logger;

/**
 * One ring step per player. The load hook only records positions; this is the only sender.
 */
public final class TerrainDispatch {
    private static final int COLUMN_FRAMES = 3;
    private static final long SUMMARY_NANOS = 5_000_000_000L;
    private static final Map<String, RingCursor> CURSORS = new ConcurrentHashMap<String, RingCursor>();
    private static final Map<String, Counters> COUNTERS = new ConcurrentHashMap<String, Counters>();
    private static final Map<String, String> LAST_WORLD = new ConcurrentHashMap<String, String>();
    private static int failures;
    private static long nextSummaryNanos;
    private static Method hasGenerated;
    private static Method sendColumn;
    private static Constructor<?> candidateCtor;
    private static Method offerColumn;
    private static Method known;
    private static Method forEach;
    private static Method encode;
    private static Method getChunkNow;
    private static Constructor<?> chunkPosCtor;
    private static Field queueField;
    private static Field pendingField;
    private static Field flightField;
    private static Field deadlineField;

    private TerrainDispatch() {
    }

    public static boolean trySend(Object level, Object engine, Object player, int inner, int max) {
        if (level == null || player == null) {
            return false;
        }
        int mode = TerrainStreamPolicy.prepareWalk(player);
        if (mode == 1) {
            noteReason(player, "paused");
            maybeSummarize();
            return false;
        }
        try {
            String dimension = dimension(level);
            String world = dimension + "#" + seed(level);
            String id = String.valueOf(uuid(player));
            String previousWorld = LAST_WORLD.put(id, world);
            if (previousWorld != null && !previousWorld.equals(world)) {
                TerrainSentMemory.clearPlayer(id);
            }
            int cx = chunkCoord(player, "x");
            int cz = chunkCoord(player, "z");
            int start = TerrainStreamPolicy.lodStartChebyshev(inner);
            int exclusive = Math.max(0, start - 1);
            RingCursor cursor = CURSORS.computeIfAbsent(id + "|" + world, key -> new RingCursor(cx, cz, start));
            if (mode == 2) {
                cursor.reset(cx, cz, start);
            } else {
                cursor.retargetIfMoved(cx, cz, start);
            }
            Counters counters = COUNTERS.computeIfAbsent(id + "|" + world, key -> new Counters());
            counters.ring = cursor.radius();
            counters.max = max;
            counters.reason = "scanning";
            int[] sent = RingScan.scan(cursor, cx, cz, exclusive, max, new Io(level, engine, player, dimension, world, id, counters), TerrainAvailability.MAX_CELL_ATTEMPTS, TerrainDispatch::pastDeadline, (x, z) -> {
                counters.abandoned++;
                counters.reason = "abandoned";
                TerrainAvailability.logSkipOnce(dimension, x, z);
            });
            counters.ring = cursor.radius();
            if (sent != null) {
                counters.sent++;
                counters.reason = "sent";
                TerrainAvailability.clear(dimension, sent[0], sent[1]);
                TerrainSentMemory.mark(id, world, sent[0], sent[1]);
                maybeSummarize();
                return true;
            }
            if (!"backpressure".equals(counters.reason) && pastDeadline()) {
                counters.reason = "deadline";
            } else if ("scanning".equals(counters.reason)) {
                counters.reason = "idle";
            }
            maybeSummarize();
            return false;
        } catch (Throwable failure) {
            if (failures++ < 3) {
                Logger.warn("[terrain-sync] ring step failed", failure);
            }
            return false;
        }
    }

    public static void onPlayerLeft(Object player) {
        UUID id = uuid(player);
        if (id == null) {
            return;
        }
        String prefix = id.toString() + "|";
        CURSORS.keySet().removeIf(key -> key.startsWith(prefix));
        COUNTERS.keySet().removeIf(key -> key.startsWith(prefix));
        LAST_WORLD.remove(id.toString());
        TerrainSentMemory.clearPlayer(id.toString());
    }

    public static void clearAll() {
        CURSORS.clear();
        COUNTERS.clear();
        LAST_WORLD.clear();
        TerrainAvailability.clearAll();
        failures = 0;
    }

    public static int cursors() {
        return CURSORS.size();
    }

    private static void noteReason(Object player, String reason) {
        UUID id = uuid(player);
        if (id == null) {
            return;
        }
        Counters counters = COUNTERS.computeIfAbsent(id.toString(), key -> new Counters());
        counters.reason = reason;
    }

    private static boolean debugEnabled() {
        try {
            Class<?> configClass = Class.forName("me.cortex.voxy.common.config.VoxyConfig");
            Object config = configClass.getField("CONFIG").get(null);
            return configClass.getField("serverTerrainSyncDebug").getBoolean(config);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void maybeSummarize() {
        if (!debugEnabled()) {
            return;
        }
        long now = System.nanoTime();
        if (now < nextSummaryNanos) {
            return;
        }
        nextSummaryNanos = now + SUMMARY_NANOS;
        for (Map.Entry<String, Counters> entry : COUNTERS.entrySet()) {
            Counters counters = entry.getValue();
            Logger.info("[terrain-sync] player=" + entry.getKey()
                    + " ring=" + counters.ring + "/" + counters.max
                    + " sent=" + counters.sent
                    + " skippedHeld=" + counters.skippedHeld
                    + " skippedKnown=" + counters.skippedKnown
                    + " skippedMissing=" + counters.skippedMissing
                    + " abandoned=" + counters.abandoned
                    + " reason=" + counters.reason);
        }
    }

    private static boolean pastDeadline() {
        try {
            ensure();
            return System.nanoTime() >= deadlineField.getLong(null);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static final class Io implements RingScan.Probe {
        private final Object level;
        private final Object engine;
        private final Object player;
        private final String dimension;
        private final String world;
        private final String playerId;
        private final Counters counters;

        Io(Object level, Object engine, Object player, String dimension, String world, String playerId, Counters counters) {
            this.level = level;
            this.engine = engine;
            this.player = player;
            this.dimension = dimension;
            this.world = world;
            this.playerId = playerId;
            this.counters = counters;
        }

        @Override
        public boolean alreadyHeld(int x, int z) {
            if (!TerrainSentMemory.holds(this.playerId, this.world, x, z)) {
                return false;
            }
            this.counters.skippedHeld++;
            this.counters.reason = "skipping_held";
            return true;
        }

        @Override
        public boolean known(int x, int z) {
            try {
                ensure();
                Object pos = chunkPosCtor.newInstance(Integer.valueOf(x), Integer.valueOf(z));
                boolean hit = (Boolean) known.invoke(null, this.player, this.level, pos);
                if (hit) {
                    this.counters.skippedKnown++;
                    this.counters.reason = "skipping_known";
                }
                return hit;
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }

        @Override
        public boolean noted(int x, int z) {
            return TerrainAvailability.contains(this.dimension, x, z);
        }

        @Override
        public boolean onDisk(int x, int z) {
            try {
                ensure();
                return (Boolean) hasGenerated.invoke(null, this.level, Integer.valueOf(x), Integer.valueOf(z));
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }

        @Override
        public int attempt(int x, int z) {
            try {
                ensure();
                boolean disk = this.onDisk(x, z);
                Object live = liveChunk(x, z);
                if (live != null) {
                    List<byte[]> wires = ingest(live);
                    if (!wires.isEmpty()) {
                        return offer(x, z, wires);
                    }
                    if (disk) {
                        return region(x, z);
                    }
                    return RingScan.FAIL;
                }
                if (!disk) {
                    this.counters.skippedMissing++;
                    this.counters.reason = "skip_ungenerated";
                    return RingScan.SKIP;
                }
                return region(x, z);
            } catch (Throwable failure) {
                return RingScan.FAIL;
            }
        }

        private int region(int x, int z) throws ReflectiveOperationException {
            long key = pack(x, z);
            Object candidate = candidateCtor.newInstance(Integer.valueOf(x), Integer.valueOf(z), Long.valueOf(key));
            boolean sent = (Boolean) sendColumn.invoke(null, this.level, this.engine, this.player, candidate);
            return sent ? RingScan.SENT : RingScan.FAIL;
        }

        private int offer(int x, int z, List<byte[]> wires) throws ReflectiveOperationException {
            if (saturated(String.valueOf(uuid(this.player)))) {
                this.counters.reason = "backpressure";
                return RingScan.BACKPRESSURE;
            }
            Object pos = chunkPosCtor.newInstance(Integer.valueOf(x), Integer.valueOf(z));
            boolean sent = (Boolean) offerColumn.invoke(null, this.player, this.level, pos, wires);
            return sent ? RingScan.SENT : RingScan.FAIL;
        }

        private List<byte[]> ingest(Object chunk) throws ReflectiveOperationException {
            List<byte[]> wires = new ArrayList<byte[]>();
            forEach.invoke(null, this.engine, chunk, (java.util.function.Consumer<Object>) section -> {
                try {
                    byte[] wire = (byte[]) encode.invoke(null, this.level, this.engine, section);
                    if (wire != null && wire.length > 0) {
                        wires.add(wire);
                    }
                } catch (ReflectiveOperationException e) {
                    wires.clear();
                }
            });
            return wires;
        }

        private Object liveChunk(int x, int z) throws ReflectiveOperationException {
            Object source = this.level.getClass().getMethod("getChunkSource").invoke(this.level);
            if (getChunkNow == null) {
                getChunkNow = source.getClass().getMethod("getChunkNow", int.class, int.class);
            }
            return getChunkNow.invoke(source, Integer.valueOf(x), Integer.valueOf(z));
        }
    }

    private static boolean saturated(String owner) throws ReflectiveOperationException {
        ensure();
        Object queue = queueField.get(null);
        int entries = (Integer) queue.getClass().getMethod("entries").invoke(queue);
        int maxEntries = queue.getClass().getField("maxEntries").getInt(queue);
        int ownerMax = queue.getClass().getField("ownerEntries").getInt(queue);
        if (entries + COLUMN_FRAMES > maxEntries) {
            return true;
        }
        int mine = count(pendingField.get(queue), owner) + count(flightField.get(queue), owner);
        return mine + COLUMN_FRAMES > ownerMax;
    }

    private static int count(Object map, String owner) throws ReflectiveOperationException {
        int n = 0;
        for (Object frame : ((Map<?, ?>) map).values()) {
            if (owner.equals(PendingOrder.ownerOf(frame))) {
                n++;
            }
        }
        return n;
    }

    private static void ensure() throws ReflectiveOperationException {
        if (hasGenerated != null) {
            return;
        }
        Class<?> walker = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker");
        hasGenerated = walker.getDeclaredMethod("hasGeneratedChunk", Class.forName("net.minecraft.server.level.ServerLevel"), int.class, int.class);
        hasGenerated.setAccessible(true);
        Class<?> candidate = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker$ChunkCandidate");
        candidateCtor = candidate.getDeclaredConstructor(int.class, int.class, long.class);
        candidateCtor.setAccessible(true);
        sendColumn = walker.getDeclaredMethod("sendChunkColumn", Class.forName("net.minecraft.server.level.ServerLevel"), Class.forName("me.cortex.voxy.common.world.WorldEngine"), Class.forName("net.minecraft.server.level.ServerPlayer"), candidate);
        sendColumn.setAccessible(true);
        deadlineField = walker.getDeclaredField("walkerDeadlineNs");
        deadlineField.setAccessible(true);
        Class<?> transport = Class.forName("me.cortex.voxy.server.ServerTerrainTransport");
        Class<?> chunkPos = Class.forName("net.minecraft.world.level.ChunkPos");
        chunkPosCtor = chunkPos.getConstructor(int.class, int.class);
        offerColumn = transport.getMethod("offerColumn", Class.forName("net.minecraft.server.level.ServerPlayer"), Class.forName("net.minecraft.server.level.ServerLevel"), chunkPos, List.class);
        known = transport.getMethod("known", Class.forName("net.minecraft.server.level.ServerPlayer"), Class.forName("net.minecraft.server.level.ServerLevel"), chunkPos);
        forEach = Class.forName("me.cortex.voxy.common.world.service.VoxelIngestService").getMethod("forEachMippedSectionForTerrainSync", Class.forName("me.cortex.voxy.common.world.WorldEngine"), Class.forName("net.minecraft.world.level.chunk.LevelChunk"), java.util.function.Consumer.class);
        encode = Class.forName("me.cortex.voxy.common.network.SectionSyncPayload").getMethod("encodeSectionWireBytes", Class.forName("net.minecraft.server.level.ServerLevel"), Class.forName("me.cortex.voxy.common.world.WorldEngine"), Class.forName("me.cortex.voxy.common.voxelization.VoxelizedSection"));
        queueField = transport.getDeclaredField("QUEUE");
        queueField.setAccessible(true);
        Class<?> queue = Class.forName("me.cortex.voxy.common.network.ReliableTerrainQueue");
        pendingField = queue.getDeclaredField("pending");
        pendingField.setAccessible(true);
        flightField = queue.getDeclaredField("flight");
        flightField.setAccessible(true);
    }

    private static long seed(Object level) {
        try {
            Object value = level.getClass().getMethod("getSeed").invoke(level);
            return value instanceof Number ? ((Number) value).longValue() : 0L;
        } catch (ReflectiveOperationException e) {
            return 0L;
        }
    }

    private static String dimension(Object level) throws ReflectiveOperationException {
        Object key = level.getClass().getMethod("dimension").invoke(level);
        Object location = key.getClass().getMethod("location").invoke(key);
        return String.valueOf(location);
    }

    private static UUID uuid(Object player) {
        try {
            Object value = player.getClass().getMethod("getUUID").invoke(player);
            return value instanceof UUID ? (UUID) value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static int chunkCoord(Object player, String field) throws ReflectiveOperationException {
        Object pos = player.getClass().getMethod("chunkPosition").invoke(player);
        return pos.getClass().getField(field).getInt(pos);
    }

    private static long pack(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }

    static final class Counters {
        int ring;
        int max;
        int sent;
        int skippedHeld;
        int skippedKnown;
        int skippedMissing;
        int abandoned;
        String reason = "idle";
    }
}
