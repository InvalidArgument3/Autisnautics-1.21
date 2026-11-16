package me.cortex.voxy.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.cortex.voxy.common.world.WorldEngine;

/**
 * 32×32 chunk bitmaps. A stored lod-0 section is 32 blocks on a side, so it
 * covers the four chunk columns whose coordinates are {@code section << 1}.
 */
public final class ColumnCoverageIndex {
    public static final int REGION_SIZE = 32;
    public static final int BYTE_LENGTH = REGION_SIZE * REGION_SIZE / 8;

    private ColumnCoverageIndex() {
    }

    public static long regionKey(int regionX, int regionZ) {
        return (long) regionX & 0xFFFFFFFFL | ((long) regionZ & 0xFFFFFFFFL) << 32;
    }

    public static int regionX(long key) {
        return (int) key;
    }

    public static int regionZ(long key) {
        return (int) (key >>> 32);
    }

    /**
     * One key from {@code SectionStorage.iteratePositions}. Level 0 only.
     * Columns farther than {@code maxChunkRadius} plus one region are left out
     * so the report stays inside the walk the server will actually do.
     */
    public static void ingest(Long2ObjectOpenHashMap<byte[]> regions, long sectionKey, int playerChunkX, int playerChunkZ, int maxChunkRadius) {
        if (WorldEngine.getLevel(sectionKey) != 0) {
            return;
        }
        int sectionX = WorldEngine.getX(sectionKey);
        int sectionZ = WorldEngine.getZ(sectionKey);
        int baseX = sectionX << 1;
        int baseZ = sectionZ << 1;
        int limit = maxChunkRadius + REGION_SIZE;
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                int chunkX = baseX + dx;
                int chunkZ = baseZ + dz;
                int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
                if (distance <= limit) {
                    markChunk(regions, chunkX, chunkZ);
                }
            }
        }
    }

    public static void markChunk(Long2ObjectOpenHashMap<byte[]> regions, int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        long key = regionKey(regionX, regionZ);
        byte[] bits = regions.get(key);
        if (bits == null) {
            bits = new byte[BYTE_LENGTH];
            regions.put(key, bits);
        }
        set(bits, chunkX, chunkZ, true);
    }

    public static boolean test(byte[] bits, int chunkX, int chunkZ) {
        if (bits == null || bits.length < BYTE_LENGTH) {
            return false;
        }
        int bit = bitIndex(chunkX, chunkZ);
        return (bits[bit >>> 3] & (1 << (bit & 7))) != 0;
    }

    public static void clearStale(byte[] bits, int regionX, int regionZ, LongOpenHashSet stale) {
        if (bits == null || stale == null || stale.isEmpty()) {
            return;
        }
        int originX = regionX << 5;
        int originZ = regionZ << 5;
        for (int lz = 0; lz < REGION_SIZE; lz++) {
            for (int lx = 0; lx < REGION_SIZE; lx++) {
                int chunkX = originX + lx;
                int chunkZ = originZ + lz;
                long packed = (long) chunkX & 0xFFFFFFFFL | ((long) chunkZ & 0xFFFFFFFFL) << 32;
                if (stale.contains(packed)) {
                    set(bits, chunkX, chunkZ, false);
                }
            }
        }
    }

    public static int count(byte[] bits) {
        if (bits == null) {
            return 0;
        }
        int total = 0;
        for (byte value : bits) {
            total += Integer.bitCount(value & 0xFF);
        }
        return total;
    }

    private static void set(byte[] bits, int chunkX, int chunkZ, boolean on) {
        int bit = bitIndex(chunkX, chunkZ);
        int index = bit >>> 3;
        int mask = 1 << (bit & 7);
        if (on) {
            bits[index] = (byte) (bits[index] | mask);
        } else {
            bits[index] = (byte) (bits[index] & ~mask);
        }
    }

    private static int bitIndex(int chunkX, int chunkZ) {
        return (chunkZ & 31) * REGION_SIZE + (chunkX & 31);
    }
}
