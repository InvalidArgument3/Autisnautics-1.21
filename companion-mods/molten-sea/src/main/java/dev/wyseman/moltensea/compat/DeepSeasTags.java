package dev.wyseman.moltensea.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.loading.FMLLoader;

/**
 * Helper for the Deep Seas tag hooks. Lives outside the mixin package: Mixin refuses to let
 * normal code load any class from a package declared in moltensea.mixins.json.
 *
 * Called from FluidState.is / Fluid.is, which run on every worldgen and fluid tick, so it must
 * stay cheap: a cached boolean and a TagKey identity/equals check. It never touches a tag
 * registry and never loads a Create Deep Seas class.
 */
public final class DeepSeasTags {
    /** create_submarine:water. TagKey.create interns, so the usual match is by identity. */
    public static final TagKey<Fluid> WATER = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath("create_submarine", "water"));

    /** True only when Create Deep Seas is in the loading mod list. Fixed for the session. */
    public static final boolean DEEP_SEAS_PRESENT = detect();

    private DeepSeasTags() {}

    public static boolean isWater(TagKey<?> tag) {
        return tag == WATER || (tag != null && WATER.equals(tag));
    }

    private static boolean detect() {
        try {
            return FMLLoader.getLoadingModList().getModFileById("create_submarine") != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
