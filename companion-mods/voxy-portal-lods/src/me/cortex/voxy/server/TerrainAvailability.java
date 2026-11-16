package me.cortex.voxy.server;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.cortex.voxy.common.Logger;

/** Chunks a load event asked the ring walker to send. Keyed by level id. */
public final class TerrainAvailability {
    public static final int MAX_CELL_ATTEMPTS = 32;

    private static final Map<String, LongOpenHashSet> AVAILABLE = new ConcurrentHashMap<String, LongOpenHashSet>();
    private static final LongOpenHashSet LOGGED = new LongOpenHashSet();

    private TerrainAvailability() {
    }

    public static void note(String dimension, int x, int z) {
        if (dimension == null) {
            return;
        }
        LongOpenHashSet set = AVAILABLE.computeIfAbsent(dimension, key -> new LongOpenHashSet());
        synchronized (set) {
            set.add(pack(x, z));
        }
    }

    public static boolean contains(String dimension, int x, int z) {
        LongOpenHashSet set = AVAILABLE.get(dimension);
        if (set == null) {
            return false;
        }
        synchronized (set) {
            return set.contains(pack(x, z));
        }
    }

    public static void clear(String dimension, int x, int z) {
        LongOpenHashSet set = AVAILABLE.get(dimension);
        if (set == null) {
            return;
        }
        synchronized (set) {
            set.remove(pack(x, z));
        }
    }

    public static void clearAll() {
        AVAILABLE.clear();
        synchronized (LOGGED) {
            LOGGED.clear();
        }
    }

    /** Queue key is {@code levelId|dimensionType|seed}. */
    public static void noteCancelled(Object key) {
        if (key == null) {
            return;
        }
        try {
            String world = (String) key.getClass().getMethod("world").invoke(key);
            int x = ((Number) key.getClass().getMethod("x").invoke(key)).intValue();
            int z = ((Number) key.getClass().getMethod("z").invoke(key)).intValue();
            note(levelId(world), x, z);
        } catch (ReflectiveOperationException e) {
            Logger.warn("[terrain-sync] could not requeue a cancelled column", e);
        }
    }

    public static void logSkipOnce(String dimension, int x, int z) {
        long key = pack(x, z) ^ (dimension == null ? 0L : (long) dimension.hashCode() << 32);
        synchronized (LOGGED) {
            if (!LOGGED.add(key)) {
                return;
            }
        }
        Logger.warn("[terrain-sync] skipped LOD column after", MAX_CELL_ATTEMPTS, "failed sends at", dimension, x, z);
    }

    static String levelId(String world) {
        if (world == null || world.isEmpty()) {
            return "";
        }
        int bar = world.indexOf('|');
        return bar < 0 ? world : world.substring(0, bar);
    }

    public static int size(String dimension) {
        LongOpenHashSet set = AVAILABLE.get(dimension);
        if (set == null) {
            return 0;
        }
        synchronized (set) {
            return set.size();
        }
    }

    private static long pack(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }
}
