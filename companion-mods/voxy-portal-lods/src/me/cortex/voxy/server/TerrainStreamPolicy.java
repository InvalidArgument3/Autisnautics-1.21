package me.cortex.voxy.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.cortex.voxy.common.config.VoxyConfig;

/**
 * Per-player LOD streaming policy for the dedicated server.
 *
 * <p>The outer radius stays the distance the client sent in {@code VoxyHelloPayload}
 * ({@code sectionRenderDistance * 32} chunks). The inner radius is the vanilla
 * view distance, a square of that many chunks. LOD packets used to start on the
 * next ring out ({@code distance > viewDistance}), which left the ring at the
 * view distance empty. They now start on that ring, so the outer vanilla chunks
 * and the first LOD ring overlap by one.
 * A configured {@code serverTerrainSyncInnerRadiusChunks} above zero is a fixed
 * override; zero follows the view distance.
 *
 * <p>Numbers for a 16G heap and a dozen players, with broadcast still opt-in:
 * the walker offers one generated chunk per server tick, the transport sends at
 * most 32 sections and 1 MiB per tick across everyone, and each player is capped
 * at 32 sections and {@code server_terrain_sync_player_bytes_per_tick} bytes per
 * tick (default 1 MiB, about 20 MB/s at 20 TPS). The value is clamped to
 * 64 KiB..8 MiB. A missing or non-positive key uses the default. Fast movement, elytra, a jump
 * of {@value #TELEPORT_BLOCKS} blocks, or a dimension change pauses new walks.
 * Teleports and dimension changes also drop that player's queued packets.
 */
public final class TerrainStreamPolicy {
    public static final int FAST_BLOCKS_PER_SECOND = 16;
    public static final int TELEPORT_BLOCKS = 48;
    public static final int FAST_PAUSE_TICKS = 15;
    public static final int TELEPORT_PAUSE_TICKS = 40;
    /** Sections one player may be offered in a tick. The byte cap is the other limit. */
    public static final int PER_PLAYER_SECTIONS_PER_TICK = 256;
    /** Default per-player compressed bytes per tick. About 20 MB/s at 20 TPS. */
    public static final int PER_PLAYER_BYTES_PER_TICK = 1048576;
    public static final int MIN_PLAYER_BYTES_PER_TICK = 64 * 1024;
    public static final int MAX_PLAYER_BYTES_PER_TICK = 8 * 1024 * 1024;
    private static volatile boolean loggedBudget;
    private static volatile boolean loggedSectionBudget;
    /** Replaces ReliableTerrainQueue's global completed-set cap of 1024. */
    public static final int COMPLETED_DEDUP_CAP = 32767;

    private static final ThreadLocal<Object> CURRENT = new ThreadLocal<Object>();
    private static final Map<Object, long[]> USED = new ConcurrentHashMap<Object, long[]>();
    private static final Map<UUID, Motion> MOTION = new ConcurrentHashMap<UUID, Motion>();

    private TerrainStreamPolicy() {
    }

    public static int innerRadiusChunks(int configured, int requested, int serverView) {
        if (configured > 0) {
            return configured;
        }
        int server = Math.max(0, serverView);
        int client = requested > 0 ? requested : server;
        if (server > 0) {
            client = Math.min(client, server);
        }
        // Unknown view distance still leaves the player's own chunks to vanilla.
        return Math.max(client, 2);
    }

    /**
     * First chebyshev ring to stream. Vanilla's view distance is a square of
     * this radius, and the walker used to begin at {@code radius + 1}.
     * Starting here overlaps that outer square by one chunk.
     */
    public static int lodStartChebyshev(int vanillaViewChunks) {
        return Math.max(1, vanillaViewChunks);
    }

    /** {@code distance <=} this value is left to vanilla. */
    public static int lodExclusiveInner(int vanillaViewChunks) {
        return Math.max(0, lodStartChebyshev(vanillaViewChunks) - 1);
    }

    public static int lodExclusiveInner(Object player) {
        return lodExclusiveInner(innerRadius(player));
    }

    public static int innerRadius(Object player) {
        int configured = 0;
        try {
            if (VoxyConfig.CONFIG != null) {
                configured = VoxyConfig.CONFIG.serverTerrainSyncInnerRadiusChunks;
            }
        } catch (Throwable ignored) {
            configured = 0;
        }
        int requested = 0;
        int server = 0;
        if (player != null) {
            requested = invokeInt(player, "requestedViewDistance");
            Object minecraftServer = invoke(player, "getServer");
            Object list = invoke(minecraftServer, "getPlayerList");
            server = invokeInt(list, "getViewDistance");
        }
        return innerRadiusChunks(configured, requested, server);
    }

    /**
     * @return 0 walk, 1 paused, 2 walk and restart the ring at the current inner radius
     */
    public static int prepareWalk(Object player) {
        if (player == null) {
            return 1;
        }
        UUID id = uuid(player);
        if (id == null) {
            return 1;
        }
        Motion motion = MOTION.computeIfAbsent(id, key -> new Motion());
        int mode = observeMotion(motion, invokeDouble(player, "getX"), invokeDouble(player, "getZ"), dimension(player), invokeBool(player, "isFallFlying"), System.nanoTime(), innerRadius(player));
        if (motion.clearQueue) {
            motion.clearQueue = false;
            clearTransport(player);
        }
        return mode;
    }

    public static int observeMotion(Motion motion, double x, double z, String dimension, boolean elytra, long nanos, int inner) {
        String dim = dimension == null ? "" : dimension;
        if (!motion.init) {
            motion.init = true;
            motion.x = x;
            motion.z = z;
            motion.dimension = dim;
            motion.nanos = nanos;
            motion.lastInner = inner;
            return 0;
        }
        double dist = Math.hypot(x - motion.x, z - motion.z);
        double seconds = Math.max(0.001, (nanos - motion.nanos) / 1_000_000_000.0);
        double blocksPerSecond = dist / seconds;
        boolean dimensionChange = !dim.equals(motion.dimension);
        // A multi-second gap is a stalled server thread, not movement. Absorb the
        // position sample so the next tick does not see the same jump as a teleport.
        boolean stale = seconds > 2.0;
        boolean teleport = dimensionChange || (!stale && dist >= TELEPORT_BLOCKS);
        boolean fast = !stale && (elytra || blocksPerSecond >= FAST_BLOCKS_PER_SECOND);
        if (stale && !dimensionChange) {
            motion.cause = "stall " + String.format("%.1f", seconds) + "s dist " + String.format("%.1f", dist) + " ignored";
        }
        if (teleport) {
            motion.pauseTicks = TELEPORT_PAUSE_TICKS;
            motion.clearQueue = true;
            motion.restart = true;
            motion.cause = dimensionChange ? "dimension " + motion.dimension + " -> " + dim : "teleport " + String.format("%.1f", dist) + " blocks";
        } else if (fast) {
            motion.pauseTicks = Math.max(motion.pauseTicks, FAST_PAUSE_TICKS);
        } else if (motion.pauseTicks > 0) {
            motion.pauseTicks--;
        }
        if (motion.lastInner >= 0 && inner != motion.lastInner) {
            motion.restart = true;
            motion.cause = "inner " + motion.lastInner + " -> " + inner;
            if (inner > motion.lastInner) {
                motion.clearQueue = true;
                motion.cause = motion.cause + " clearQueue";
            }
        }
        motion.lastInner = inner;
        motion.x = x;
        motion.z = z;
        motion.dimension = dim;
        motion.nanos = nanos;
        if (motion.pauseTicks > 0) {
            return 1;
        }
        if (motion.restart) {
            motion.restart = false;
            if (motion.cause == null || motion.cause.isEmpty()) {
                motion.cause = "restart";
            }
            return 2;
        }
        if (motion.cause == null) {
            motion.cause = "walk";
        }
        return 0;
    }

    private static long nextPauseLog;

    /** A paused walk never reaches the ring summary, so the pause has to log itself. */
    public static void logIfPaused() {
        boolean paused = false;
        String cause = "";
        for (Motion motion : MOTION.values()) {
            if (motion.pauseTicks > 0) {
                paused = true;
                cause = motion.cause == null ? "paused" : motion.cause;
            }
        }
        if (!paused) {
            return;
        }
        long now = System.nanoTime();
        if (now < nextPauseLog) {
            return;
        }
        nextPauseLog = now + 5_000_000_000L;
        me.cortex.voxy.common.Logger.info("[terrain-sync] motion paused cause=" + cause);
    }

    /** Latest motion decision, including a stall that did not restart the ring. */
    public static String motionCause() {
        String cause = "none";
        for (Motion motion : MOTION.values()) {
            if (motion.cause != null) {
                cause = motion.cause;
            }
        }
        return cause;
    }

    public static void notePlayer(Object player) {
        CURRENT.set(player);
    }

    public static void onPlayerLeft(Object player) {
        UUID id = uuid(player);
        if (id != null) {
            MOTION.remove(id);
        }
        if (player != null) {
            USED.remove(player);
        }
        if (CURRENT.get() == player) {
            CURRENT.remove();
        }
    }

    public static void clearAll() {
        MOTION.clear();
        USED.clear();
        CURRENT.remove();
    }

    public static void resetTick() {
        USED.clear();
        CURRENT.remove();
    }

    public static boolean consume(int sections, long bytes) {
        Object key = CURRENT.get();
        if (!playerAllows(key, sections, bytes)) {
            return false;
        }
        if (!ServerTerrainSyncBudget.tryConsume(sections, bytes)) {
            return false;
        }
        playerAdd(key, sections, bytes);
        return true;
    }

    /** Per-player half of {@link #consume}, without the global budget. */
    public static boolean tryPlayer(Object key, int sections, long bytes) {
        if (!playerAllows(key, sections, bytes)) {
            return false;
        }
        playerAdd(key, sections, bytes);
        return true;
    }

    /**
     * Compressed bytes one player may be offered in a tick.
     * Config key {@code server_terrain_sync_player_bytes_per_tick}.
     */
    public static int playerBytesPerTick() {
        int value = PER_PLAYER_BYTES_PER_TICK;
        try {
            if (VoxyConfig.CONFIG != null) {
                Object raw = VoxyConfig.CONFIG.getClass().getField("serverTerrainSyncPlayerBytesPerTick").get(VoxyConfig.CONFIG);
                if (raw instanceof Number) {
                    value = ((Number) raw).intValue();
                }
            }
        } catch (Throwable ignored) {
            value = PER_PLAYER_BYTES_PER_TICK;
        }
        if (value <= 0) {
            value = PER_PLAYER_BYTES_PER_TICK;
        }
        int clamped = Math.max(MIN_PLAYER_BYTES_PER_TICK, Math.min(MAX_PLAYER_BYTES_PER_TICK, value));
        if (!loggedBudget) {
            loggedBudget = true;
            me.cortex.voxy.common.Logger.info("[terrain-sync] per-player byte budget " + clamped + " B/tick (about " + String.format("%.1f", clamped * 20.0 / (1024.0 * 1024.0)) + " MB/s at 20 TPS), allowed " + MIN_PLAYER_BYTES_PER_TICK + ".." + MAX_PLAYER_BYTES_PER_TICK);
        }
        return clamped;
    }

    /**
     * The previous default of 32 sections per tick, with about 42 sections in a
     * column, is under one column per tick. A configured 32 is that old default.
     */
    public static void ensureSectionBudget() {
        try {
            if (VoxyConfig.CONFIG == null || VoxyConfig.CONFIG.serverTerrainSyncMaxSectionsPerTick != 32) {
                return;
            }
            VoxyConfig.CONFIG.serverTerrainSyncMaxSectionsPerTick = PER_PLAYER_SECTIONS_PER_TICK;
            if (!loggedSectionBudget) {
                loggedSectionBudget = true;
                me.cortex.voxy.common.Logger.info("[terrain-sync] section budget 32 was the old per-tick default; using " + PER_PLAYER_SECTIONS_PER_TICK + " so the byte cap is the limit");
            }
        } catch (Throwable ignored) {
        }
    }

    public static int motionCount() {
        return MOTION.size();
    }

    private static boolean playerAllows(Object key, int sections, long bytes) {
        if (key == null || sections < 1 || bytes < 1L) {
            return key == null && sections > 0 && bytes > 0L;
        }
        long[] used = USED.get(key);
        long sectionsUsed = used == null ? 0L : used[0];
        long bytesUsed = used == null ? 0L : used[1];
        return sectionsUsed + sections <= PER_PLAYER_SECTIONS_PER_TICK && bytesUsed + bytes <= playerBytesPerTick();
    }

    private static void playerAdd(Object key, int sections, long bytes) {
        if (key == null) {
            return;
        }
        long[] used = USED.computeIfAbsent(key, ignored -> new long[2]);
        used[0] += sections;
        used[1] += bytes;
    }

    private static void clearTransport(Object player) {
        try {
            Class<?> type = Class.forName("me.cortex.voxy.server.ServerTerrainTransport");
            for (Method method : type.getMethods()) {
                if ("clearPlayer".equals(method.getName()) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isInstance(player)) {
                    method.invoke(null, player);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // The queue is also dropped on the next transport tick when the column is out of range.
        }
    }

    private static UUID uuid(Object player) {
        Object value = invoke(player, "getUUID");
        return value instanceof UUID ? (UUID) value : null;
    }

    private static String dimension(Object player) {
        Object level = invoke(player, "serverLevel");
        Object key = invoke(level, "dimension");
        Object location = invoke(key, "location");
        return location == null ? "" : location.toString();
    }

    private static Object invoke(Object target, String name) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(name);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int invokeInt(Object target, String name) {
        Object value = invoke(target, name);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static double invokeDouble(Object target, String name) {
        Object value = invoke(target, name);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private static boolean invokeBool(Object target, String name) {
        Object value = invoke(target, name);
        return value instanceof Boolean && (Boolean) value;
    }

    public static final class Motion {
        double x;
        double z;
        String dimension = "";
        long nanos;
        int pauseTicks;
        boolean init;
        boolean clearQueue;
        boolean restart;
        int lastInner = -1;
        String cause = "";

        public String cause() {
            return this.cause == null ? "" : this.cause;
        }

        public boolean droppedQueue() {
            return this.clearQueue;
        }
    }
}
