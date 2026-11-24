package dev.wyseman.autisnauticsrifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;

public final class RiftBoundary {
    public enum FillType {
        AIR,
        WATER,
        LAVA;

        public static FillType parse(String raw) {
            if (raw == null) return AIR;
            return switch (raw.trim().toUpperCase()) {
                case "WATER" -> WATER;
                case "LAVA" -> LAVA;
                default -> AIR;
            };
        }

        public BlockState blockState() {
            return switch (this) {
                case WATER -> Blocks.WATER.defaultBlockState();
                case LAVA -> Blocks.LAVA.defaultBlockState();
                case AIR -> Blocks.AIR.defaultBlockState();
            };
        }
    }

    public record Face(
        String boundary,
        boolean enabled,
        int lo,
        int hi,
        int anchorY,
        double sentinel,
        long salt,
        int clearLo,
        int clearHi,
        FillType fillType,
        int fluidMaxY,
        boolean sealFluids,
        boolean isCeiling,
        boolean isAbyssShaft,
        String neighborName
    ) {
        public boolean wantsFluidAt(int y) {
            if (fillType == FillType.AIR) return false;
            return fluidMaxY == Integer.MAX_VALUE || y < fluidMaxY;
        }

        public BlockState fillBlockStateAt(int y) {
            return wantsFluidAt(y) ? fillType.blockState() : Blocks.AIR.defaultBlockState();
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

    private static final int NO_FLUID_CAP = Integer.MAX_VALUE;

    private static final Map<String, DimensionFaces> FACES = Map.of(
        "minecraft:overworld", new DimensionFaces(
            new Face("0_1", true, -64, 240, -52, -2000.0, 0L, -64, 240, FillType.WATER, 188, false, false, false, "The Deepsea"),
            null
        ),
        "oceanworld:deepsea", new DimensionFaces(
            new Face("1_2", true, -64, 36, -64, -2006.0, 0L, -64, 36, FillType.WATER, NO_FLUID_CAP, false, false, false, "Dimension of Caves"),
            new Face("0_1", false, 275, 320, 299, -2005.0, 0L, 275, 320, FillType.WATER, NO_FLUID_CAP, false, true, false, "Overworld Skylands")
        ),
        "dimension_of_caves:cave", new DimensionFaces(
            new Face("2_3", true, -48, 52, -48, -2008.0, 0L, -48, 52, FillType.AIR, NO_FLUID_CAP, false, false, false, "Infinite Abyss"),
            new Face("1_2", true, 28, 128, 127, -2007.0, 0L, 28, 128, FillType.AIR, NO_FLUID_CAP, false, true, false, "The Deepsea")
        ),
        "infinite_abyss:first_layer", new DimensionFaces(
            new Face("3_4", true, 0, 100, 0, -2002.0, 0L, 0, 100, FillType.AIR, NO_FLUID_CAP, false, false, false, "The Otherside"),
            new Face("2_3", true, 28, 128, 127, -2001.0, 0L, 28, 128, FillType.AIR, NO_FLUID_CAP, false, true, false, "Dimension of Caves")
        ),
        "deeperdarker:otherside", new DimensionFaces(
            new Face("4_5", true, 0, 100, 0, -2004.0, 0L, 0, 100, FillType.AIR, NO_FLUID_CAP, false, false, false, "The Undergarden"),
            new Face("3_4", true, 28, 128, 127, -2003.0, 0L, 28, 128, FillType.AIR, NO_FLUID_CAP, false, true, false, "Infinite Abyss")
        ),
        "undergarden:undergarden", new DimensionFaces(
            new Face("5_6", true, -64, 36, -64, -2014.0, 0L, -64, 36, FillType.AIR, NO_FLUID_CAP, false, false, false, "Infernal Cross"),
            new Face("4_5", true, 28, 128, 127, -2013.0, 0L, 28, 128, FillType.AIR, NO_FLUID_CAP, false, true, false, "The Otherside")
        ),
        "infernalcross:dimension", new DimensionFaces(
            new Face("6_7", true, -48, 52, -48, -2010.0, 0L, -48, 52, FillType.AIR, NO_FLUID_CAP, false, false, false, "The Nether"),
            new Face("5_6", true, -36, 64, 63, -2009.0, 0L, -36, 64, FillType.AIR, NO_FLUID_CAP, false, true, false, "The Undergarden")
        ),
        "minecraft:the_nether", new DimensionFaces(
            new Face("7_8", true, 0, 100, 0, -2012.0, 0L, 0, 100, FillType.LAVA, NO_FLUID_CAP, false, false, false, "Molten Depths"),
            new Face("6_7", true, 156, 256, 255, -2011.0, 0L, 156, 256, FillType.AIR, NO_FLUID_CAP, false, true, false, "Infernal Cross")
        ),
        "autisnautics:molten_depths", new DimensionFaces(
            new Face("8_9", true, -64, 36, -64, -2017.0, 0L, -64, 36, FillType.LAVA, NO_FLUID_CAP, false, false, false, "The End"),
            new Face("7_8", true, 220, 320, 319, -2016.0, 0L, 220, 320, FillType.AIR, NO_FLUID_CAP, false, true, false, "The Nether")
        ),
        "minecraft:the_end", new DimensionFaces(
            null,
            new Face("8_9", true, 284, 384, 383, -2015.0, 0L, 284, 384, FillType.AIR, NO_FLUID_CAP, false, true, false, "Molten Depths")
        )
    );

    private RiftBoundary() {}

    public static DimensionFaces detectDefaultFaces(ServerLevel level) {
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
            floorFace = new Face("dyn_" + dimId + "_" + destDim, true, minY, hi, minY, Double.NaN, salt, minY, hi, FillType.AIR, NO_FLUID_CAP, false, false, false, prettyName(destDim));
        }

        Face ceilFace = null;
        VerticalConnectingPortal ceilPortal = VerticalConnectingPortal.getConnectingPortal(level, VerticalConnectingPortal.ConnectorType.ceil);
        if (ceilPortal != null) {
            String destDim = ceilPortal.getDestDim().location().toString();
            int lo = Math.max(minY, maxY - band);
            long salt = AdaptiveRiftMask.boundarySalt(dimId, destDim);
            ceilFace = new Face("dyn_" + destDim + "_" + dimId, true, lo, maxY, maxY - 1, Double.NaN, salt, lo, maxY, FillType.AIR, NO_FLUID_CAP, false, true, false, prettyName(destDim));
        }

        return new DimensionFaces(floorFace, ceilFace);
    }

    public static DimensionFaces resolveDimensionFaces(ServerLevel level) {
        String dimId = level.dimension().location().toString();
        DimensionFaces configured = RiftStackConfig.getConfigured(dimId);
        if (configured != null) {
            return configured;
        }
        return detectDefaultFaces(level);
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
