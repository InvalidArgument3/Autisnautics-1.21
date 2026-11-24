package dev.wyseman.autisnauticsrifts;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RiftConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue BEDROCK_ONLY_AT_RIFTS;
    public static final ModConfigSpec.BooleanValue ADAPTIVE_DIMENSION_STACK;
    public static final ModConfigSpec.IntValue ADAPTIVE_BAND_THICKNESS;
    public static final ModConfigSpec.BooleanValue MADE_IN_ABYSS_MODE;
    public static final ModConfigSpec.IntValue MADE_IN_ABYSS_RADIUS;
    public static final ModConfigSpec.BooleanValue MADE_IN_ABYSS_DISABLES_CELLULAR_RIFTS;
    public static final ModConfigSpec.BooleanValue MADE_IN_ABYSS_END_CEILING_ONLY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("bedrock");
        BEDROCK_ONLY_AT_RIFTS = b
            .comment("If true, overrides Immersive Portals' blanket bedrock replacement so bedrock is preserved everywhere except inside dimensional rifts (and the Made in Abyss central shaft).")
            .define("bedrockOnlyAtRifts", true);
        b.pop();

        b.push("adaptive");
        ADAPTIVE_DIMENSION_STACK = b
            .comment("If true, dynamically adapts rift carving and the Rift Compass to any arbitrary Immersive Portals dimension stack (reading live VerticalConnectingPortal floor/ceiling links).")
            .define("adaptiveDimensionStack", true);
        ADAPTIVE_BAND_THICKNESS = b
            .comment("Vertical thickness (in blocks) of floor/ceiling rift openings for dynamically discovered dimensions.")
            .defineInRange("adaptiveBandThickness", 25, 8, 64);
        b.pop();

        b.push("made_in_abyss");
        MADE_IN_ABYSS_MODE = b
            .comment("If true, carves a massive circular central shaft around (0, 0) through every dimension in the stack, stopping at 3/4 depth in the bottom dimension.")
            .define("madeInAbyssMode", false);
        MADE_IN_ABYSS_RADIUS = b
            .comment("Radius in blocks of the Made in Abyss central shaft around (0, 0).")
            .defineInRange("madeInAbyssRadius", 128, 16, 1024);
        MADE_IN_ABYSS_DISABLES_CELLULAR_RIFTS = b
            .comment("If true (while madeInAbyssMode is enabled), disables scattered cellular rifts so the central abyssal shaft is the sole vertical descent.")
            .define("madeInAbyssDisablesCellularRifts", false);
        MADE_IN_ABYSS_END_CEILING_ONLY = b
            .comment("If true and the bottom dimension is minecraft:the_end, the Made in Abyss central shaft stops right after piercing The End's ceiling instead of carving 3/4 of the way down.")
            .define("madeInAbyssEndCeilingOnly", true);
        b.pop();

        SPEC = b.build();
    }

    private RiftConfig() {}
}
