package dev.wyseman.autisnauticsrifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public final class RiftBoundary {
    public record Face(
        String boundary,
        int lo,
        int hi,
        int anchorY,
        double sentinel,
        int clearLo,
        int clearHi,
        int waterBelow,
        boolean isCeiling,
        String neighborName
    ) {
        public boolean wantsWater(int y) {
            return waterBelow != Integer.MIN_VALUE && y < waterBelow;
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
            new Face("0_1", -153, 23, -52, -2000.0, -153, 23, 188, false, "The Deepsea"),
            null
        ),
        "oceanworld:deepsea", new DimensionFaces(
            new Face("1_2", -64, -39, -64, -2006.0, -64, -39, NO_WATER, false, "Dimension of Caves"),
            new Face("0_1", 275, 320, 299, -2005.0, 275, 320, NO_WATER, true, "Overworld Skylands")
        ),
        "dimension_of_caves:cave", new DimensionFaces(
            new Face("2_3", -48, -23, -48, -2008.0, -48, -23, NO_WATER, false, "Infinite Abyss"),
            new Face("1_2", 103, 128, 127, -2007.0, 103, 128, NO_WATER, true, "The Deepsea")
        ),
        "infinite_abyss:first_layer", new DimensionFaces(
            new Face("3_4", 0, 25, 0, -2002.0, 0, 25, NO_WATER, false, "The Otherside"),
            new Face("2_3", 103, 128, 127, -2001.0, 103, 128, NO_WATER, true, "Dimension of Caves")
        ),
        "deeperdarker:otherside", new DimensionFaces(
            new Face("4_5", 0, 25, 0, -2004.0, 0, 25, NO_WATER, false, "The Undergarden"),
            new Face("3_4", 103, 128, 127, -2003.0, 103, 128, NO_WATER, true, "Infinite Abyss")
        ),
        "undergarden:undergarden", new DimensionFaces(
            new Face("5_6", -64, -39, -64, -2014.0, -64, -39, NO_WATER, false, "Infernal Cross"),
            new Face("4_5", 103, 128, 127, -2013.0, 103, 128, NO_WATER, true, "The Otherside")
        ),
        "infernalcross:dimension", new DimensionFaces(
            new Face("6_7", -48, -23, -48, -2010.0, -48, -23, NO_WATER, false, "The Nether"),
            new Face("5_6", 39, 64, 63, -2009.0, 39, 64, NO_WATER, true, "The Undergarden")
        ),
        "minecraft:the_nether", new DimensionFaces(
            new Face("7_8", 0, 25, 0, -2012.0, 0, 25, NO_WATER, false, "Molten Depths"),
            new Face("6_7", 231, 256, 255, -2011.0, 231, 256, NO_WATER, true, "Infernal Cross")
        ),
        "autisnautics:molten_depths", new DimensionFaces(
            new Face("8_9", -64, -39, -64, -2017.0, -64, -39, NO_WATER, false, "The End"),
            new Face("7_8", 295, 320, 319, -2016.0, 295, 320, NO_WATER, true, "The Nether")
        ),
        "minecraft:the_end", new DimensionFaces(
            null,
            new Face("8_9", 359, 384, 383, -2015.0, 359, 384, NO_WATER, true, "Molten Depths")
        )
    );

    private RiftBoundary() {}

    @Nullable
    public static Face getFace(String dimensionId, boolean preferCeiling) {
        DimensionFaces df = FACES.get(dimensionId);
        if (df == null) return null;
        if (preferCeiling) {
            return df.ceiling() != null ? df.ceiling() : df.floor();
        }
        return df.floor() != null ? df.floor() : df.ceiling();
    }

    public static List<Face> getAllFaces(String dimensionId) {
        DimensionFaces df = FACES.get(dimensionId);
        return df == null ? List.of() : df.allFaces();
    }

    public static boolean hasDimension(String dimensionId) {
        return FACES.containsKey(dimensionId);
    }

    public static boolean hasBoth(String dimensionId) {
        DimensionFaces df = FACES.get(dimensionId);
        return df != null && df.floor() != null && df.ceiling() != null;
    }
}
