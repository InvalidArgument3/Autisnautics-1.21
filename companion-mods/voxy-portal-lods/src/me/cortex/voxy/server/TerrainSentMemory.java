package me.cortex.voxy.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Columns the player already has. {@link #HELD} is what this server sent.
 * {@link #COVERAGE} is what the client reported from its own Voxy database.
 * A chunk load is recorded in {@link #STALE} so a later report cannot put that
 * column back: the client's copy is older than the edit.
 */
public final class TerrainSentMemory {
    public static final int NONE = 0;
    public static final int SENT = 1;
    public static final int CLIENT = 2;
    public static final int CAP = 65536;

    private static final Map<String, LongOpenHashSet> HELD = new ConcurrentHashMap<String, LongOpenHashSet>();
    private static final Map<String, Long2ObjectOpenHashMap<byte[]>> COVERAGE = new ConcurrentHashMap<String, Long2ObjectOpenHashMap<byte[]>>();
    /** Columns this dimension has reloaded. Keyed by dimension id, not by player. */
    private static final Map<String, LongOpenHashSet> STALE = new ConcurrentHashMap<String, LongOpenHashSet>();
    private static int coveragePackets;
    private static String lastCoverageWorld = "-";

    private TerrainSentMemory() {
    }

    public static boolean holds(String player, String dimension, int x, int z) {
        return holdKind(player, dimension, x, z) != NONE;
    }

    public static int holdKind(String player, String dimension, int x, int z) {
        String id = key(player, dimension);
        LongOpenHashSet set = HELD.get(id);
        if (set != null) {
            synchronized (set) {
                if (set.contains(pack(x, z))) {
                    return SENT;
                }
            }
        }
        return clientBit(id, x, z) ? CLIENT : NONE;
    }

    /**
     * Replace one region of the client's report. Bits the server has already
     * seen change are cleared, so a rescan cannot revive a stale column.
     */
    public static void cover(String player, String dimension, int regionX, int regionZ, byte[] bitmap) {
        if (player == null || dimension == null || bitmap == null || bitmap.length != ColumnCoverageIndex.BYTE_LENGTH) {
            return;
        }
        byte[] copy = Arrays.copyOf(bitmap, ColumnCoverageIndex.BYTE_LENGTH);
        coveragePackets++;
        lastCoverageWorld = dimension;
        String id = key(player, dimension);
        LongOpenHashSet stale = STALE.get(dimensionName(dimension));
        if (stale != null) {
            synchronized (stale) {
                ColumnCoverageIndex.clearStale(copy, regionX, regionZ, stale);
            }
        }
        Long2ObjectOpenHashMap<byte[]> regions = COVERAGE.computeIfAbsent(id, ignored -> new Long2ObjectOpenHashMap<byte[]>());
        synchronized (regions) {
            regions.put(ColumnCoverageIndex.regionKey(regionX, regionZ), copy);
        }
    }

    public static int coveragePackets() {
        return coveragePackets;
    }

    public static String lastCoverageWorld() {
        return lastCoverageWorld;
    }

    public static void unmark(String player, String dimension, int x, int z) {
        if (player == null || dimension == null) {
            return;
        }
        LongOpenHashSet set = HELD.get(key(player, dimension));
        if (set == null) {
            return;
        }
        synchronized (set) {
            set.remove(pack(x, z));
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
        LongOpenHashSet one = new LongOpenHashSet();
        one.add(packed);
        LongOpenHashSet stale = STALE.computeIfAbsent(dimension, ignored -> new LongOpenHashSet());
        synchronized (stale) {
            stale.add(packed);
        }
        for (String heldKey : HELD.keySet()) {
            if (!matches(heldKey, suffix)) {
                continue;
            }
            LongOpenHashSet set = HELD.get(heldKey);
            if (set != null) {
                synchronized (set) {
                    set.remove(packed);
                }
            }
        }
        for (String heldKey : COVERAGE.keySet()) {
            if (!matches(heldKey, suffix)) {
                continue;
            }
            Long2ObjectOpenHashMap<byte[]> regions = COVERAGE.get(heldKey);
            if (regions != null) {
                synchronized (regions) {
                    byte[] bits = regions.get(ColumnCoverageIndex.regionKey(x >> 5, z >> 5));
                    if (bits != null) {
                        ColumnCoverageIndex.clearStale(bits, x >> 5, z >> 5, one);
                    }
                }
            }
        }
    }

    public static void clearPlayer(String player) {
        if (player == null) {
            return;
        }
        String prefix = player + "|";
        HELD.keySet().removeIf(key -> key.startsWith(prefix));
        COVERAGE.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public static void clearAll() {
        HELD.clear();
        COVERAGE.clear();
        STALE.clear();
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

    public static int clientColumns(String player, String dimension) {
        Long2ObjectOpenHashMap<byte[]> regions = COVERAGE.get(key(player, dimension));
        if (regions == null) {
            return 0;
        }
        int total = 0;
        synchronized (regions) {
            for (byte[] bits : regions.values()) {
                total += ColumnCoverageIndex.count(bits);
            }
        }
        return total;
    }

    private static boolean clientBit(String id, int x, int z) {
        Long2ObjectOpenHashMap<byte[]> regions = COVERAGE.get(id);
        if (regions == null) {
            return false;
        }
        synchronized (regions) {
            return ColumnCoverageIndex.test(regions.get(ColumnCoverageIndex.regionKey(x >> 5, z >> 5)), x, z);
        }
    }

    private static String dimensionName(String world) {
        int hash = world.indexOf('#');
        return hash < 0 ? world : world.substring(0, hash);
    }

    private static boolean matches(String heldKey, String suffix) {
        return heldKey.endsWith(suffix) || heldKey.contains(suffix + "#");
    }

    private static String key(String player, String dimension) {
        return player + "|" + dimension;
    }

    private static long pack(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }
}
