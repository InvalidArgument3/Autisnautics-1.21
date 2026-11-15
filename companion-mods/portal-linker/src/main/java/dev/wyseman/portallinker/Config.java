package dev.wyseman.portallinker;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_INTERIOR;
    public static final ModConfigSpec.ConfigValue<String> FRAME_BLOCK;
    public static final ModConfigSpec.ConfigValue<String> CORNER_BLOCK;
    public static final ModConfigSpec.BooleanValue CATALYST_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> CATALYST_ITEM;
    public static final ModConfigSpec.IntValue CATALYST_COUNT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("frames");
        MAX_INTERIOR = builder
                .comment("Largest interior edge, in blocks. A frame bigger than this on either edge is refused. Default 32 rejects a 33-long opening.")
                .defineInRange("maxInterior", 32, 2, 64);
        FRAME_BLOCK = builder
                .comment("Block id that counts as a frame edge, in addition to the portal_linker:portal_frames block tag. A datapack can add more ids to that tag. Default minecraft:crying_obsidian.")
                .define("frameBlock", "minecraft:crying_obsidian");
        CORNER_BLOCK = builder
                .comment("Block id that the four corners must be, in addition to the portal_linker:portal_corners block tag. Default minecraft:diamond_block. The other frame blocks stay frameBlock. Set this equal to frameBlock to allow one block for the whole ring (and then take crying obsidian out of portal_frames if that block should be the only one). A 32x32 ring is 4 corners plus 128 edges, so corners stay the diamond cost at every size.")
                .define("cornerBlock", "minecraft:diamond_block");
        builder.pop();
        builder.push("catalyst");
        CATALYST_ENABLED = builder
                .comment("When true, a successful link consumes catalystCount of catalystItem from the player. The linker item is never consumed. Default off.")
                .define("enabled", false);
        CATALYST_ITEM = builder
                .comment("Item id consumed when enabled is true.")
                .define("item", "minecraft:ender_pearl");
        CATALYST_COUNT = builder
                .comment("How many of that item a successful link consumes.")
                .defineInRange("count", 1, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }

    private Config() {}

    public static int maxInterior() {
        return SPEC.isLoaded() ? MAX_INTERIOR.get() : 32;
    }

    public static Block frameBlock() {
        if (!SPEC.isLoaded()) {
            return Blocks.CRYING_OBSIDIAN;
        }
        return blockOr(FRAME_BLOCK.get(), Blocks.CRYING_OBSIDIAN);
    }

    public static Block cornerBlock() {
        if (!SPEC.isLoaded()) {
            return Blocks.DIAMOND_BLOCK;
        }
        return blockOr(CORNER_BLOCK.get(), Blocks.DIAMOND_BLOCK);
    }

    /** False when both config blocks are the same, so the whole ring may be that one material. */
    public static boolean distinctCorners() {
        return cornerBlock() != frameBlock();
    }

    public static boolean catalystEnabled() {
        return SPEC.isLoaded() && CATALYST_ENABLED.get();
    }

    public static ResourceLocation catalystId() {
        String raw = SPEC.isLoaded() ? CATALYST_ITEM.get() : "minecraft:ender_pearl";
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id == null ? ResourceLocation.withDefaultNamespace("ender_pearl") : id;
    }

    public static int catalystCount() {
        return SPEC.isLoaded() ? CATALYST_COUNT.get() : 1;
    }

    private static Block blockOr(String raw, Block fallback) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return fallback;
        }
        return BuiltInRegistries.BLOCK.get(id);
    }
}
