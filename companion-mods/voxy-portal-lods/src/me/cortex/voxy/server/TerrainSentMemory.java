package me.cortex.voxy.server;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Columns already given to a player. Outlives the transport's 30 second dedup
 * window. A chunk load invalidates that position so a real change is sent again.
 */
public final class TerrainSentMemory {
    public static final int CAP = 65536;

    private static final Map<String, LongOpenHashSet> HELD = new ConcurrentHashMap<String, LongOpenHashSet>();

    private TerrainSentMemory() {
    }

    public static boolean holds(String player, String dimension, int x, int z) {
        LongOpenHashSet set = HELD.get(key(player, dimension));
        if (set == null) {
            return false;
        }
        synchronized (set) {
            return set.contains(pack(x, z));
        }
    }

    public static void mark(String player, String dimension, int x, int z) {
        if (player == null || dimension == null) {
            return;
        }
        LongOpenHashSet set = HELD.computeIfAbsent(key(player, dimension), ignored -> new LongOpenHashSet());
        synchronized (set) {
            if (set.size() >= CAP) {
                set.clear();
            }
            set.add(pack(x, z));
        }
    }

    public static void invalidate(String dimension, int x, int z) {
        if (dimension == null) {
            return;
        }
        long packed = pack(x, z);
        String suffix = "|" + dimension;
        for (Map.Entry<String, LongOpenHashSet> entry : HELD.entrySet()) {
            String heldKey = entry.getKey();
            if (!heldKey.endsWith(suffix) && !heldKey.contains(suffix + "#")) {
                continue;
            }
            LongOpenHashSet set = entry.getValue();
            synchronized (set) {
                set.remove(packed);
            }
        }
    }

    public static void clearPlayer(String player) {
        if (player == null) {
            return;
        }
        String prefix = player + "|";
        HELD.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public static void clearAll() {
        HELD.clear();
    }

    public static int size(String player, String dimension) {
        LongOpenHashSet set = HELD.get(key(player, dimension));
        if (set == null) {
            return 0;
        }
        synchronized (set) {
            return set.size();
        }
    }

    private static String key(String player, String dimension) {
        return player + "|" + dimension;
    }

    private static long pack(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }
}
