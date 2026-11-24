package dev.wyseman.autisnauticsrifts;

import java.util.Random;
import net.minecraft.core.BlockPos;

public final class AdaptiveRiftMask {
    private static final double JITTER = 0.9;

    private AdaptiveRiftMask() {}

    public static boolean usesCustomDensity() {
        return RiftConfig.RIFT_GRID_SPACING.get() != 2236
            || RiftConfig.MIN_RIFT_RADIUS.get() != 50
            || RiftConfig.MAX_RIFT_RADIUS.get() != 109;
    }

    public static long boundarySalt(String dimA, String dimB) {
        String first = dimA.compareTo(dimB) <= 0 ? dimA : dimB;
        String second = dimA.compareTo(dimB) <= 0 ? dimB : dimA;
        long h = 810457L;
        h = h * 31L + first.hashCode();
        h = h * 31L + second.hashCode();
        return h;
    }

    private static long cellSeed(long worldSeed, long salt, int cellX, int cellZ) {
        long s = worldSeed ^ salt;
        s ^= (long) cellX * 341873128712L;
        s ^= (long) cellZ * 132897987541L;
        return s;
    }

    private static void cellCenter(long worldSeed, long salt, int gridSize, int cellX, int cellZ, double[] out) {
        Random rng = new Random(cellSeed(worldSeed, salt, cellX, cellZ));
        double ox = (0.5 - JITTER * 0.5) + rng.nextDouble() * JITTER;
        double oz = (0.5 - JITTER * 0.5) + rng.nextDouble() * JITTER;
        double val = rng.nextDouble();
        out[0] = (cellX + ox) * gridSize;
        out[1] = (cellZ + oz) * gridSize;
        out[2] = val;
    }

    public static boolean isInsideRift(long worldSeed, long salt, int x, int z) {
        int gridSize = RiftConfig.RIFT_GRID_SPACING.get();
        int minR = RiftConfig.MIN_RIFT_RADIUS.get();
        int maxR = Math.max(minR, RiftConfig.MAX_RIFT_RADIUS.get());
        int cx = Math.floorDiv(x, gridSize);
        int cz = Math.floorDiv(z, gridSize);
        double bestDistSq = Double.MAX_VALUE;
        double bestVal = 0.0;
        double[] buf = new double[3];

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                cellCenter(worldSeed, salt, gridSize, cx + dx, cz + dz, buf);
                double ddx = x - buf[0];
                double ddz = z - buf[1];
                double dSq = ddx * ddx + ddz * ddz;
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestVal = buf[2];
                }
            }
        }
        double radius = minR + (maxR - minR) * bestVal;
        return bestDistSq <= radius * radius;
    }

    public static BlockPos findNearestCenter(long worldSeed, long salt, int originX, int originZ, int anchorY) {
        int gridSize = RiftConfig.RIFT_GRID_SPACING.get();
        int cx = Math.floorDiv(originX, gridSize);
        int cz = Math.floorDiv(originZ, gridSize);
        double bestDistSq = Double.MAX_VALUE;
        int bestX = 0;
        int bestZ = 0;
        double[] buf = new double[3];

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                cellCenter(worldSeed, salt, gridSize, cx + dx, cz + dz, buf);
                double ddx = originX - buf[0];
                double ddz = originZ - buf[1];
                double dSq = ddx * ddx + ddz * ddz;
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestX = (int) Math.round(buf[0]);
                    bestZ = (int) Math.round(buf[1]);
                }
            }
        }
        return new BlockPos(bestX, anchorY, bestZ);
    }
}
