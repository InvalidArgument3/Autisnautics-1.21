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

    private record CacheKey(String dimension, boolean ceiling, int gridX, int gridZ) {}

    private static final Map<CacheKey, RiftTarget> CACHE = new ConcurrentHashMap<>();

    private RiftLocator() {}

    public static RiftTarget findNearestRift(ServerLevel level, BlockPos origin, boolean preferCeiling) {
        String dimId = level.dimension().location().toString();
        RiftBoundary.Face face = RiftBoundary.getFace(dimId, preferCeiling);
        if (face == null) {
            return new RiftTarget(dimId, 0, 0, 0, false, preferCeiling, "");
        }

        int gx = Math.floorDiv(origin.getX(), 32);
        int gz = Math.floorDiv(origin.getZ(), 32);
        CacheKey key = new CacheKey(dimId, face.isCeiling(), gx, gz);
        RiftTarget cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        DensityFunction density = level.getChunkSource().randomState().router().finalDensity();
        BlockPos hit = spiralSearch(density, origin.getX(), origin.getZ(), face.anchorY(), face.sentinel());
        RiftTarget result;
        if (hit == null) {
            result = new RiftTarget(dimId, 0, face.anchorY(), 0, false, face.isCeiling(), face.boundary());
        } else {
            BlockPos center = refineCenter(density, hit.getX(), hit.getZ(), face.anchorY(), face.sentinel());
            result = new RiftTarget(dimId, center.getX(), face.anchorY(), center.getZ(), true, face.isCeiling(), face.boundary());
        }

        if (CACHE.size() > 2048) {
            CACHE.clear();
        }
        CACHE.put(key, result);
        return result;
    }

    private static boolean isRift(DensityFunction density, int x, int y, int z, double sentinel) {
        double v = density.compute(new DensityFunction.SinglePointContext(x, y, z));
        return Math.abs(v - sentinel) <= 0.1;
    }

    @Nullable
    private static BlockPos spiralSearch(DensityFunction density, int ox, int oz, int anchorY, double sentinel) {
        if (isRift(density, ox, anchorY, oz, sentinel)) {
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
                    if (isRift(density, x, anchorY, z, sentinel)) {
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
                if (isRift(density, x, anchorY, z, sentinel)) {
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
