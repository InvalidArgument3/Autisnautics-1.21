package dev.wyseman.autisnauticsrifts;

import java.util.Map;
import javax.annotation.Nullable;

public final class RiftBoundary {
    public record Face(String boundary, int anchorY, double sentinel, boolean isCeiling, String neighborName) {}

    public record DimensionFaces(@Nullable Face floor, @Nullable Face ceiling) {}

    private static final Map<String, DimensionFaces> FACES = Map.of(
        "minecraft:overworld", new DimensionFaces(
            new Face("0_1", -52, -2000.0, false, "The Deepsea"),
            null
        ),
        "oceanworld:deepsea", new DimensionFaces(
            new Face("1_2", -64, -2006.0, false, "Dimension of Caves"),
            new Face("0_1", 299, -2005.0, true, "Overworld Skylands")
        ),
        "dimension_of_caves:cave", new DimensionFaces(
            new Face("2_3", -48, -2008.0, false, "Infinite Abyss"),
            new Face("1_2", 127, -2007.0, true, "The Deepsea")
        ),
        "infinite_abyss:first_layer", new DimensionFaces(
            new Face("3_4", 0, -2002.0, false, "The Otherside"),
            new Face("2_3", 127, -2001.0, true, "Dimension of Caves")
        ),
        "deeperdarker:otherside", new DimensionFaces(
            new Face("4_5", 0, -2004.0, false, "The Undergarden"),
            new Face("3_4", 127, -2003.0, true, "Infinite Abyss")
        ),
        "undergarden:undergarden", new DimensionFaces(
            new Face("5_6", -64, -2014.0, false, "Infernal Cross"),
            new Face("4_5", 127, -2013.0, true, "The Otherside")
        ),
        "infernalcross:dimension", new DimensionFaces(
            new Face("6_7", -48, -2010.0, false, "The Nether"),
            new Face("5_6", 63, -2009.0, true, "The Undergarden")
        ),
        "minecraft:the_nether", new DimensionFaces(
            new Face("7_8", 0, -2012.0, false, "Molten Depths"),
            new Face("6_7", 255, -2011.0, true, "Infernal Cross")
        ),
        "autisnautics:molten_depths", new DimensionFaces(
            new Face("8_9", -64, -2017.0, false, "The End"),
            new Face("7_8", 319, -2016.0, true, "The Nether")
        ),
        "minecraft:the_end", new DimensionFaces(
            null,
            new Face("8_9", 383, -2015.0, true, "Molten Depths")
        )
    );

    private RiftBoundary() {}

    @Nullable
    public static Face getFace(String dimensionId, boolean preferCeiling) {
        DimensionFaces df = FACES.get(dimensionId);
        if (df == null) {
            return null;
        }
        if (preferCeiling) {
            return df.ceiling() != null ? df.ceiling() : df.floor();
        }
        return df.floor() != null ? df.floor() : df.ceiling();
    }

    public static boolean hasBoth(String dimensionId) {
        DimensionFaces df = FACES.get(dimensionId);
        return df != null && df.floor() != null && df.ceiling() != null;
    }
}
