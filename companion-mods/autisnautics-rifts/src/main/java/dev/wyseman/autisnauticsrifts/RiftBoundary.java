package dev.wyseman.autisnauticsrifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;

public final class RiftBoundary {
    public record Face(
        String boundary,
        int lo,
        int hi,
        int anchorY,
        double sentinel,
        long salt,
        int clearLo,
        int clearHi,
        int waterBelow,
        boolean isCeiling,
        boolean isAbyssShaft,
        String neighborName
    ) {
        public boolean wantsWater(int y) {
            return waterBelow != Integer.MIN_VALUE && y < waterBelow;
        }

        public boolean usesDensitySentinel() {
            return !Double.isNaN(sentinel);
        }
    }

    public record DimensionFaces(@Nullable Face floor, @Nullable Face ceiling) {
        public List<Face> allFaces() {
            List<Face> list = new ArrayList<>(2);
            if (floor != null) list.add(floor);
            if (ceiling != null) list.add(ceiling);
            return list;
        }
    }

    private static final int NO_WATER = Integer.MIN_VALUE;

    private static final Map<String, DimensionFaces> FACES = Map.of(
        "minecraft:overworld", new DimensionFaces(
            new Face("0_1", -153, 23, -52, -2000.0, 0L, -153, 23, 188, false, false, "The Deepsea"),
            null
        ),
        "oceanworld:deepsea", new DimensionFaces(
            new Face("1_2", -64, -39, -64, -2006.0, 0L, -64, -39, NO_WATER, false, false, "Dimension of Caves"),
            new Face("0_1", 275, 320, 299, -2005.0, 0L, 275, 320, NO_WATER, true, false, "Overworld Skylands")
        ),
        "dimension_of_caves:cave", new DimensionFaces(
            new Face("2_3", -48, -23, -48, -2008.0, 0L, -48, -23, NO_WATER, false, false, "Infinite Abyss"),
            new Face("1_2", 103, 128, 127, -2007.0, 0L, 103, 128, NO_WATER, true, false, "The Deepsea")
        ),
        "infinite_abyss:first_layer", new DimensionFaces(
            new Face("3_4", 0, 25, 0, -2002.0, 0L, 0, 25, NO_WATER, false, false, "The Otherside"),
            new Face("2_3", 103, 128, 127, -2001.0, 0L, 103, 128, NO_WATER, true, false, "Dimension of Caves")
        ),
        "deeperdarker:otherside", new DimensionFaces(
            new Face("4_5", 0, 25, 0, -2004.0, 0L, 0, 25, NO_WATER, false, false, "The Undergarden"),
            new Face("3_4", 103, 128, 127, -2003.0, 0L, 103, 128, NO_WATER, true, false, "Infinite Abyss")
        ),
        "undergarden:undergarden", new DimensionFaces(
            new Face("5_6", -64, -39, -64, -2014.0, 0L, -64, -39, NO_WATER, false, false, "Infernal Cross"),
            new Face("4_5", 103, 128, 127, -2013.0, 0L, 103, 128, NO_WATER, true, false, "The Otherside")
        ),
        "infernalcross:dimension", new DimensionFaces(
            new Face("6_7", -48, -23, -48, -2010.0, 0L, -48, -23, NO_WATER, false, false, "The Nether"),
            new Face("5_6", 39, 64, 63, -2009.0, 0L, 39, 64, NO_WATER, true, false, "The Undergarden")
        ),
        "minecraft:the_nether", new DimensionFaces(
            new Face("7_8", 0, 25, 0, -2012.0, 0L, 0, 25, NO_WATER, false, false, "Molten Depths"),
            new Face("6_7", 231, 256, 255, -2011.0, 0L, 231, 256, NO_WATER, true, false, "Infernal Cross")
        ),
        "autisnautics:molten_depths", new DimensionFaces(
            new Face("8_9", -64, -39, -64, -2017.0, 0L, -64, -39, NO_WATER, false, false, "The End"),
            new Face("7_8", 295, 320, 319, -2016.0, 0L, 295, 320, NO_WATER, true, false, "The Nether")
        ),
        "minecraft:the_end", new DimensionFaces(
            null,
            new Face("8_9", 359, 384, 383, -2015.0, 0L, 359, 384, NO_WATER, true, false, "Molten Depths")
        )
    );

    private RiftBoundary() {}

    @Nullable
    public static Face getFace(String dimensionId, boolean preferCeiling) {
        DimensionFaces df = FACES.get(dimensionId);
        if (df == null) return null;
        return preferCeiling
            ? (df.ceiling() != null ? df.ceiling() : df.floor())
            : (df.floor() != null ? df.floor() : df.ceiling());
    }

    public static DimensionFaces resolveDimensionFaces(ServerLevel level) {
        String dimId = level.dimension().location().toString();
        DimensionFaces hardcoded = FACES.get(dimId);
        if (hardcoded != null) {
            return hardcoded;
        }
        if (!RiftConfig.ADAPTIVE_DIMENSION_STACK.get()) {
            return new DimensionFaces(null, null);
        }
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int band = RiftConfig.ADAPTIVE_BAND_THICKNESS.get();

        Face floorFace = null;
        VerticalConnectingPortal floorPortal = VerticalConnectingPortal.getConnectingPortal(level, VerticalConnectingPortal.ConnectorType.floor);
        if (floorPortal != null) {
            String destDim = floorPortal.getDestDim().location().toString();
            int hi = Math.min(maxY, minY + band);
            long salt = AdaptiveRiftMask.boundarySalt(dimId, destDim);
            floorFace = new Face("dyn_" + dimId + "_" + destDim, minY, hi, minY, Double.NaN, salt, minY, hi, NO_WATER, false, false, prettyName(destDim));
        }

        Face ceilFace = null;
        VerticalConnectingPortal ceilPortal = VerticalConnectingPortal.getConnectingPortal(level, VerticalConnectingPortal.ConnectorType.ceil);
        if (ceilPortal != null) {
            String destDim = ceilPortal.getDestDim().location().toString();
            int lo = Math.max(minY, maxY - band);
            long salt = AdaptiveRiftMask.boundarySalt(dimId, destDim);
            ceilFace = new Face("dyn_" + destDim + "_" + dimId, lo, maxY, maxY - 1, Double.NaN, salt, lo, maxY, NO_WATER, true, false, prettyName(destDim));
        }

        return new DimensionFaces(floorFace, ceilFace);
    }

    @Nullable
    public static Face resolveFace(ServerLevel level, boolean preferCeiling) {
        DimensionFaces df = resolveDimensionFaces(level);
        return preferCeiling
            ? (df.ceiling() != null ? df.ceiling() : df.floor())
            : (df.floor() != null ? df.floor() : df.ceiling());
    }

    public static List<Face> resolveAllFaces(ServerLevel level) {
        return resolveDimensionFaces(level).allFaces();
    }

    public static boolean hasDimension(ServerLevel level) {
        DimensionFaces df = resolveDimensionFaces(level);
        return df.floor() != null || df.ceiling() != null;
    }

    public static boolean hasBoth(ServerLevel level) {
        DimensionFaces df = resolveDimensionFaces(level);
        return df.floor() != null && df.ceiling() != null;
    }

    public static boolean hasBoth(String dimensionId) {
        DimensionFaces df = FACES.get(dimensionId);
        return df != null && df.floor() != null && df.ceiling() != null;
    }

    public static boolean isBottomDimension(ServerLevel level) {
        DimensionFaces df = resolveDimensionFaces(level);
        return df.ceiling() != null && df.floor() == null;
    }

    private static String prettyName(String dimId) {
        int colon = dimId.indexOf(':');
        String path = colon >= 0 ? dimId.substring(colon + 1) : dimId;
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
