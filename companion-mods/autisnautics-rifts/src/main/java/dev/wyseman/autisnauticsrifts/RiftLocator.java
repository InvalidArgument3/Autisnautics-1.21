package dev.wyseman.autisnauticsrifts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class RiftLocator {
    private static final int COARSE_STEP = 48;
    private static final int MAX_RADIUS = 3200;
    private static final int REFINE_STEP = 8;
    private static final int REFINE_RADIUS = 128;

    private record CacheKey(String dimension, boolean ceiling, int gridX, int gridZ, boolean abyss, int abyssRadius, boolean abyssOnly) {}

    private static final Map<CacheKey, RiftTarget> CACHE = new ConcurrentHashMap<>();

    private RiftLocator() {}

    public static boolean isRiftColumn(ServerLevel level, DensityFunction density, RiftBoundary.Face face, int x, int z) {
        if (face.isAbyssShaft()) {
            long r = RiftConfig.MADE_IN_ABYSS_RADIUS.get();
            return (long) x * x + (long) z * z <= r * r;
        }
        if (RiftConfig.MADE_IN_ABYSS_MODE.get() && RiftConfig.MADE_IN_ABYSS_DISABLES_CELLULAR_RIFTS.get()) {
            return false;
        }
        if (face.usesDensitySentinel()) {
            double v = density.compute(new DensityFunction.SinglePointContext(x, face.anchorY(), z));
            return Math.abs(v - face.sentinel()) <= 0.1;
        }
        return AdaptiveRiftMask.isInsideRift(level.getSeed(), face.salt(), x, z);
    }

    public static RiftTarget findNearestRift(ServerLevel level, BlockPos origin, boolean preferCeiling) {
        String dimId = level.dimension().location().toString();
        RiftBoundary.Face face = RiftBoundary.resolveFace(level, preferCeiling);
        if (face == null) {
            return new RiftTarget(dimId, 0, 0, 0, false, preferCeiling, "");
        }

        boolean abyss = RiftConfig.MADE_IN_ABYSS_MODE.get();
        int abyssRadius = RiftConfig.MADE_IN_ABYSS_RADIUS.get();
        boolean abyssOnly = abyss && RiftConfig.MADE_IN_ABYSS_DISABLES_CELLULAR_RIFTS.get();

        int gx = Math.floorDiv(origin.getX(), 32);
        int gz = Math.floorDiv(origin.getZ(), 32);
        CacheKey key = new CacheKey(dimId, face.isCeiling(), gx, gz, abyss, abyssRadius, abyssOnly);
        RiftTarget cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        BlockPos cellularCenter = null;
        if (!abyssOnly) {
            if (face.usesDensitySentinel()) {
                DensityFunction density = level.getChunkSource().randomState().router().finalDensity();
                BlockPos hit = spiralSearch(density, origin.getX(), origin.getZ(), face.anchorY(), face.sentinel());
                if (hit != null) {
                    cellularCenter = refineCenter(density, hit.getX(), hit.getZ(), face.anchorY(), face.sentinel());
                }
            } else {
                cellularCenter = AdaptiveRiftMask.findNearestCenter(level.getSeed(), face.salt(), origin.getX(), origin.getZ(), face.anchorY());
            }
        }

        BlockPos best = cellularCenter;
        if (abyss) {
            double abyssDistSq = (double) origin.getX() * origin.getX() + (double) origin.getZ() * origin.getZ();
            if (best == null) {
                best = new BlockPos(0, face.anchorY(), 0);
            } else {
                double cellDistSq = (double) (best.getX() - origin.getX()) * (best.getX() - origin.getX())
                    + (double) (best.getZ() - origin.getZ()) * (best.getZ() - origin.getZ());
                if (abyssDistSq < cellDistSq) {
                    best = new BlockPos(0, face.anchorY(), 0);
                }
            }
        }

        RiftTarget result = best == null
            ? new RiftTarget(dimId, 0, face.anchorY(), 0, false, face.isCeiling(), face.boundary())
            : new RiftTarget(dimId, best.getX(), face.anchorY(), best.getZ(), true, face.isCeiling(), face.boundary());

        if (CACHE.size() > 2048) {
            CACHE.clear();
        }
        CACHE.put(key, result);
        return result;
    }

    private static boolean isSentinelRift(DensityFunction density, int x, int y, int z, double sentinel) {
        double v = density.compute(new DensityFunction.SinglePointContext(x, y, z));
        return Math.abs(v - sentinel) <= 0.1;
    }

    @Nullable
    private static BlockPos spiralSearch(DensityFunction density, int ox, int oz, int anchorY, double sentinel) {
        if (isSentinelRift(density, ox, anchorY, oz, sentinel)) {
            return new BlockPos(ox, anchorY, oz);
        }
        int maxSteps = MAX_RADIUS / COARSE_STEP;
        int bestX = 0;
        int bestZ = 0;
        double bestDistSq = Double.MAX_VALUE;
        boolean found = false;

        for (int ring = 1; ring <= maxSteps; ring++) {
            int minD = ring * COARSE_STEP;
            if (found && (double) (minD - COARSE_STEP) * (minD - COARSE_STEP) > bestDistSq) {
                break;
            }
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.abs(dx) != ring && Math.abs(dz) != ring) {
                        continue;
                    }
                    int x = ox + dx * COARSE_STEP;
                    int z = oz + dz * COARSE_STEP;
                    if (isSentinelRift(density, x, anchorY, z, sentinel)) {
                        double dSq = (double) (x - ox) * (x - ox) + (double) (z - oz) * (z - oz);
                        if (dSq < bestDistSq) {
                            bestDistSq = dSq;
                            bestX = x;
                            bestZ = z;
                            found = true;
                        }
                    }
                }
            }
        }
        return found ? new BlockPos(bestX, anchorY, bestZ) : null;
    }

    private static BlockPos refineCenter(DensityFunction density, int hx, int hz, int anchorY, double sentinel) {
        long sumX = 0;
        long sumZ = 0;
        int count = 0;
        for (int dx = -REFINE_RADIUS; dx <= REFINE_RADIUS; dx += REFINE_STEP) {
            for (int dz = -REFINE_RADIUS; dz <= REFINE_RADIUS; dz += REFINE_STEP) {
                int x = hx + dx;
                int z = hz + dz;
                if (isSentinelRift(density, x, anchorY, z, sentinel)) {
                    sumX += x;
                    sumZ += z;
                    count++;
                }
            }
        }
        if (count == 0) {
            return new BlockPos(hx, anchorY, hz);
        }
        return new BlockPos((int) Math.round((double) sumX / count), anchorY, (int) Math.round((double) sumZ / count));
    }
}
