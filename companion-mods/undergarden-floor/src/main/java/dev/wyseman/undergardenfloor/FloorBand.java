package dev.wyseman.undergardenfloor;

/**
 * The Undergarden noise settings place {@code minecraft:bedrock} with the
 * {@code minecraft:bedrock_floor} gradient: always at {@code above_bottom 0},
 * fading out by {@code above_bottom 5}. Immersive Portals then rewrites every
 * bedrock block in the chunk to {@code undergarden:depthrock}, so a live floor
 * is depthrock and a floor generated before the stack is still bedrock.
 * The two ids are indistinguishable from natural depthrock once that rewrite
 * has run, so the whole band is the conversion range.
 */
public final class FloorBand {
    public static final int LAYERS = 5;
    public static final String BEDROCK = "minecraft:bedrock";
    public static final String DEPTHROCK = "undergarden:depthrock";
    public static final String DIMENSION = "undergarden:undergarden";

    private FloorBand() {}

    public static boolean inFloorBand(int y, int minBuildHeight) {
        return y >= minBuildHeight && y < minBuildHeight + LAYERS;
    }

    public static boolean isFloorBlock(String blockId) {
        return BEDROCK.equals(blockId) || DEPTHROCK.equals(blockId);
    }
}
