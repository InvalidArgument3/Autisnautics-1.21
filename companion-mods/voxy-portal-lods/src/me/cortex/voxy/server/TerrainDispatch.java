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
import me.cortex.voxy.common.network.SectionWire;

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
        if (ColumnProfile.suppressWalker) {
            return false;
        }
        return sendOnce(level, engine, player, inner, max);
    }

    /** One tick of the ring. The profile calls this directly so the measurement is the real walk. */
    public static boolean sendOnce(Object level, Object engine, Object player, int inner, int max) {
        if (level == null || player == null) {
            return false;
        }
        int mode = TerrainStreamPolicy.prepareWalk(player);
        if (mode == 1) {
            notePause(player);
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
                TerrainWorkers.drop(id);
                cursor.reset(cx, cz, start);
            } else {
                cursor.retargetIfMoved(cx, cz, start);
            }
            Counters counters = COUNTERS.computeIfAbsent(id + "|" + world, key -> new Counters());
            counters.ring = cursor.radius();
            counters.max = max;
            Io probe = new Io(level, engine, player, dimension, world, id, counters);
            int budget = columnsThisTick(serverBusy(player));
            int produced = 0;
            while (produced < budget) {
                counters.reason = "scanning";
                int[] sent = RingScan.scan(cursor, cx, cz, exclusive, max, probe, TerrainAvailability.MAX_CELL_ATTEMPTS, TerrainDispatch::pastDeadline, (x, z) -> {
                    counters.abandoned++;
                    counters.reason = "abandoned";
                    TerrainAvailability.logSkipOnce(dimension, x, z);
                });
                counters.ring = cursor.radius();
                counters.attempts = cursor.attempts();
                counters.holding = cursor.holding();
                if (sent == null) {
                    break;
                }
                produced++;
                counters.sent++;
                counters.reason = "sent";
                TerrainAvailability.clear(dimension, sent[0], sent[1]);
            }
            if (produced == 0) {
                if (!"backpressure".equals(counters.reason) && pastDeadline()) {
                    counters.reason = "deadline";
                } else if (cursor.holding()) {
                    counters.reason = counters.attempts > 0 ? "offer-fail" : "waiting";
            } else if (TerrainWorkers.busy(id)) {
                counters.reason = "scheduled";
            } else if ("scanning".equals(counters.reason)) {
                counters.reason = "idle";
            }
            }
            maybeSummarize();
            return produced > 0;
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
        TerrainWorkers.drop(id.toString());
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

    /** How many columns one tick may voxelize. A busy server stays at one. */
    public static int columnsThisTick(boolean serverBusy) {
        return serverBusy ? 1 : 32;
    }

    /** How many columns may be in the background read ahead of the cursor. */
    public static int asyncWindow(Object player) {
        return columnsThisTick(serverBusy(player)) == 1 ? 1 : 24;
    }

    /** Keep walking until the tick's column budget or the scan has nothing to send. */
    public static int burst(RingCursor cursor, RingScan.Probe probe, int cx, int cz, int exclusive, int max, int budget) {
        int produced = 0;
        while (produced < budget) {
            int[] sent = RingScan.scan(cursor, cx, cz, exclusive, max, probe, TerrainAvailability.MAX_CELL_ATTEMPTS, () -> false, (x, z) -> {
            });
            if (sent == null) {
                break;
            }
            produced++;
        }
        return produced;
    }

    private static void notePause(Object player) {
        UUID id = uuid(player);
        if (id == null) {
            return;
        }
        String prefix = id.toString() + "|";
        for (Map.Entry<String, Counters> entry : COUNTERS.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                entry.getValue().reason = "paused";
            }
        }
    }

    private static boolean serverBusy(Object player) {
        try {
            Object server = player.getClass().getMethod("getServer").invoke(player);
            if (server == null) {
                return false;
            }
            float average = 0.0f;
            try {
                Object value = server.getClass().getMethod("getAverageTickTime").invoke(server);
                if (value instanceof Number) {
                    average = ((Number) value).floatValue();
                }
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
            float limit = 40.0f;
            if (me.cortex.voxy.common.config.VoxyConfig.CONFIG != null) {
                limit = me.cortex.voxy.common.config.VoxyConfig.CONFIG.serverTerrainSyncBackgroundPauseMspt;
            }
            return limit > 0.0f && average > limit;
        } catch (Throwable ignored) {
            return false;
        }
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
            String mapKey = entry.getKey();
            int split = mapKey.indexOf('|');
            int clientHeld = split < 0 ? 0 : TerrainSentMemory.clientColumns(mapKey.substring(0, split), mapKey.substring(split + 1));
            Logger.info("[terrain-sync] player=" + mapKey
                    + " state=" + counters.reason
                    + " radius=" + counters.ring
                    + " target=" + counters.max
                    + " holding=" + counters.holding
                    + " attempts=" + counters.attempts
                    + " queueFrames=" + queueDepth()
                    + " columnsSent=" + counters.sent
                    + " columnsAcked=" + TerrainDelivery.acked()
                    + " framesTimedOut=" + TerrainDelivery.timedOut()
                    + " framesDropped=" + TerrainDelivery.dropped()
                    + " motion=" + TerrainStreamPolicy.motionCause()
                    + " clientHeld=" + clientHeld
                    + " heldSent=" + (split < 0 ? 0 : TerrainSentMemory.size(mapKey.substring(0, split), mapKey.substring(split + 1)))
                    + " skippedHeld=" + counters.skippedHeld
                    + " skippedClient=" + counters.skippedClient
                    + " skippedQueued=" + counters.skippedKnown
                    + " skippedMissing=" + counters.skippedMissing
                    + " abandoned=" + counters.abandoned
                    + " coveragePkts=" + TerrainSentMemory.coveragePackets()
                    + " coverageWorld=" + TerrainSentMemory.lastCoverageWorld());
        }
    }

    private static int queueDepth() {
        try {
            ensure();
            Object queue = queueField.get(null);
            return (Integer) queue.getClass().getMethod("entries").invoke(queue);
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }

    public static void armDeadline(long budgetNanos) {
        try {
            ensure();
            deadlineField.setLong(null, System.nanoTime() + budgetNanos);
        } catch (ReflectiveOperationException ignored) {
            // The profile tick then runs without a deadline and the scan's step cap still applies.
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
            if (ColumnProfile.timing) {
                ColumnProfile.cell();
            }
            int kind = TerrainSentMemory.holdKind(this.playerId, this.world, x, z);
            if (kind == TerrainSentMemory.NONE) {
                return false;
            }
            if (kind == TerrainSentMemory.CLIENT) {
                this.counters.skippedClient++;
                this.counters.reason = "skipping_client";
                if (ColumnProfile.timing) {
                    ColumnProfile.skip("client");
                }
            } else {
                this.counters.skippedHeld++;
                this.counters.reason = "skipping_held";
                if (ColumnProfile.timing) {
                    ColumnProfile.skip("held");
                }
            }
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
                    if (ColumnProfile.timing) {
                        ColumnProfile.skip("known");
                    }
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
                long started = ColumnProfile.timing ? System.nanoTime() : 0L;
                boolean hit = (Boolean) hasGenerated.invoke(null, this.level, Integer.valueOf(x), Integer.valueOf(z));
                if (ColumnProfile.timing) {
                    ColumnProfile.add("header", System.nanoTime() - started);
                }
                return hit;
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
                    long ingestStart = ColumnProfile.timing ? System.nanoTime() : 0L;
                    List<byte[]> wires = ingest(live);
                    if (ColumnProfile.timing) {
                        ColumnProfile.add("liveIngest", System.nanoTime() - ingestStart);
                    }
                    if (!wires.isEmpty()) {
                        return offer(x, z, wires);
                    }
                    if (disk) {
                        return region(x, z);
                    }
                    this.counters.skippedMissing++;
                    this.counters.reason = "skip_unreadable";
                    if (ColumnProfile.timing) {
                        ColumnProfile.skip("unreadable");
                    }
                    return RingScan.SKIP;
                }
                if (!disk) {
                    this.counters.skippedMissing++;
                    this.counters.reason = "skip_ungenerated";
                    if (ColumnProfile.timing) {
                        ColumnProfile.skip("ungenerated");
                    }
                    return RingScan.SKIP;
                }
                return region(x, z);
            } catch (Throwable failure) {
                return RingScan.FAIL;
            }
        }

        private int region(int x, int z) {
            if (ColumnProfile.timing && !ColumnProfile.asyncMeasure) {
                int result = ColumnStages.time(this.level, this.engine, this.player, x, z);
                if (result == RingScan.SKIP) {
                    this.counters.skippedMissing++;
                    this.counters.reason = "skip_unreadable";
                    ColumnProfile.skip("unreadable");
                } else if (result == RingScan.BACKPRESSURE) {
                    this.counters.reason = "backpressure";
                    ColumnProfile.skip("backpressure");
                } else if (result == RingScan.FAIL) {
                    ColumnProfile.skip("offer_rejected");
                }
                return result;
            }
            int scheduled = TerrainWorkers.schedule(this.level, this.engine, this.player, this.playerId, this.world, x, z);
            if (scheduled == RingScan.SCHEDULED) {
                this.counters.reason = "scheduled";
            } else if (scheduled == RingScan.WAIT) {
                this.counters.reason = "scheduled";
            }
            return scheduled;
        }

        private int offer(int x, int z, List<byte[]> wires) throws ReflectiveOperationException {
            if (saturated(String.valueOf(uuid(this.player)))) {
                this.counters.reason = "backpressure";
                return RingScan.BACKPRESSURE;
            }
            Object pos = chunkPosCtor.newInstance(Integer.valueOf(x), Integer.valueOf(z));
            long offerStart = ColumnProfile.timing ? System.nanoTime() : 0L;
            boolean sent = (Boolean) offerColumn.invoke(null, this.player, this.level, pos, frameFor(wires));
            if (ColumnProfile.timing) {
                ColumnProfile.add("offer", System.nanoTime() - offerStart);
                if (sent) {
                    ColumnProfile.sent();
                }
            }
            return sent ? RingScan.SENT : RingScan.FAIL;
        }

        /** Live chunks used to skip the worker, so their sections went out raw and the client dropped them. */
        private List<byte[]> frameFor(List<byte[]> wires) {
            if (wires == null || !SectionWire.compressFor(this.player)) {
                return wires;
            }
            ArrayList<byte[]> framed = new ArrayList<byte[]>(wires.size());
            for (int i = 0; i < wires.size(); i++) {
                framed.add(SectionWire.frame(wires.get(i)));
            }
            return framed;
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

    static void noteAsyncSkip(String playerId, String world) {
        Counters counters = COUNTERS.get(playerId + "|" + world);
        if (counters != null) {
            counters.skippedMissing++;
            counters.reason = "skip_unreadable";
        }
    }

    static void noteAsyncSent(String playerId, String world, int x, int z) {
        Counters counters = COUNTERS.get(playerId + "|" + world);
        if (counters != null) {
            counters.sent++;
            counters.reason = "sent";
        }
    }

    public static String worldKeyOf(Object level) throws ReflectiveOperationException {
        return dimension(level) + "#" + seed(level);
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
        int skippedClient;
        int skippedKnown;
        int skippedMissing;
        int abandoned;
        int attempts;
        boolean holding;
        String reason = "idle";
    }
}
