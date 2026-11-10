package dev.wyseman.moltensea;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue REFUSE_REPLACEMENT;
    public static final ModConfigSpec.BooleanValue SELF_HEAL;
    public static final ModConfigSpec.BooleanValue HOLD_RIFT_CAPS;
    public static final ModConfigSpec.BooleanValue DEEP_SEAS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("sea");
        REFUSE_REPLACEMENT = builder
                .comment("Refuse BaseFlowingFluid replacement when the existing fluid and the incoming fluid are both in autisnautics:molten_sea.")
                .define("refuseSeaReplacement", true);
        SELF_HEAL = builder
                .comment("In autisnautics:molten_depths only, allow a tagged molten fluid to become a source when vanilla's two-neighbour rule would. Holes inside one metal can refill. A cell sitting on a different metal still cannot.")
                .define("selfHealInMoltenDepths", true);
        HOLD_RIFT_CAPS = builder
                .comment("In autisnautics:molten_depths, refuse downward spread of a tagged molten fluid into Y<=-40 or Y>=295. That keeps the carved rift caps from filling. The sea itself is Y=-39 through Y=294.")
                .define("holdRiftCaps", true);
        DEEP_SEAS = builder
                .comment("When Create Deep Seas (create_submarine) is installed, treat autisnautics:molten_sea as create_submarine:water for its sea checks, hide those fluids inside an occluded hull, and skip molten contact damage and molten fog for a sealed occupant. Turn this off to leave Deep Seas water-only.")
                .define("deepSeasIntegration", true);
        builder.pop();
        SPEC = builder.build();
    }

    private Config() {}

    public static boolean refuseReplacement() {
        return loaded(REFUSE_REPLACEMENT, true);
    }

    public static boolean selfHeal() {
        return loaded(SELF_HEAL, true);
    }

    public static boolean holdRiftCaps() {
        return loaded(HOLD_RIFT_CAPS, true);
    }

    public static boolean deepSeas() {
        return loaded(DEEP_SEAS, true);
    }

    private static boolean loaded(ModConfigSpec.BooleanValue value, boolean fallback) {
        return SPEC.isLoaded() ? value.get() : fallback;
    }
}
