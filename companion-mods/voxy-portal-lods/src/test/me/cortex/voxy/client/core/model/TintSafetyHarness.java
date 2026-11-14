package me.cortex.voxy.client.core.model;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Calls the patched getBlockTint implementations and the capture fallback with a null biome and a
 * throwing provider. Neither path may throw.
 */
public final class TintSafetyHarness {
    public static void main(String[] args) throws Exception {
        Biome biome = new Biome();
        BlockState state = new BlockState() {
            @Override
            public String toString() {
                return "regions_unexplored:peat_grass_block";
            }
        };
        ColorResolver plains = (value, x, z) -> 0x112233;
        ColorResolver throwing = (value, x, z) -> {
            throw new NullPointerException("missing biome colour data");
        };

        check(ColourTintSafety.safeBlockTint(null, null) == ColourTintSafety.PLAINS_GRASS_TINT, "null resolver");
        check(ColourTintSafety.safeBlockTint(null, throwing) == ColourTintSafety.PLAINS_GRASS_TINT, "null biome");
        check(ColourTintSafety.safeBlockTint(biome, throwing) == ColourTintSafety.PLAINS_GRASS_TINT, "throwing resolver");
        check(ColourTintSafety.safeBlockTint(biome, plains) == 0x112233, "resolver colour");

        check(ColourTintSafety.safeProbeTint(null, plains) == -1, "probe null biome stays a miss");
        check(ColourTintSafety.safeProbeTint(biome, null) == -1, "probe null resolver");
        check(ColourTintSafety.safeProbeTint(biome, throwing) == -1, "probe throwing resolver");
        check(ColourTintSafety.safeProbeTint(biome, plains) == 0x112233, "probe colour");

        BlockColor badProvider = (blockState, world, pos, tint) -> {
            throw new RuntimeException("regions unexplored tint");
        };
        check(ColourTintSafety.safeGetColor(badProvider, state, null, BlockPos.ZERO, 0) == -1, "throwing provider");
        check(ColourTintSafety.safeGetColor(badProvider, state, null, BlockPos.ZERO, 1) == -1, "throwing provider again");
        check(ColourTintSafety.LOGGED_PROVIDER_FAILURES.size() == 1, "logged once per block");
        check(ColourTintSafety.LOGGED_PROVIDER_FAILURES.contains("regions_unexplored:peat_grass_block"), "log key");

        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$2", null, throwing, ColourTintSafety.PLAINS_GRASS_TINT, "capture getBlockTint null biome");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$2", biome, throwing, ColourTintSafety.PLAINS_GRASS_TINT, "capture getBlockTint throwing resolver");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$2", biome, plains, 0x112233, "capture getBlockTint colour");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$2", null, null, ColourTintSafety.PLAINS_GRASS_TINT, "capture getBlockTint null resolver");

        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$1", null, plains, -1, "probe getBlockTint null biome");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$1", biome, null, -1, "probe getBlockTint null resolver");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$1", biome, throwing, -1, "probe getBlockTint throwing resolver");
        invokeGetBlockTint("me.cortex.voxy.client.core.model.ModelFactory$1", biome, plains, 0x112233, "probe getBlockTint colour");

        Class<?> samplerClass = Class.forName("me.cortex.voxy.client.TimingStatistics$TimeSampler");
        Object sampler = samplerClass.getDeclaredConstructor().newInstance();
        Method start = samplerClass.getDeclaredMethod("start");
        Method reset = samplerClass.getDeclaredMethod("reset");
        start.setAccessible(true);
        reset.setAccessible(true);
        start.invoke(sampler);
        reset.invoke(sampler);
        start.invoke(sampler);
        Class<?> statistics = Class.forName("me.cortex.voxy.client.TimingStatistics");
        statistics.getMethod("abandonRunningSamplers").invoke(null);
        start.invoke(sampler);

        class Bakery {
            public void tick(long budget) {
                throw new NullPointerException("tint");
            }
        }
        try {
            ColourTintSafety.guardedModelTick(new Bakery(), 1L);
            throw new AssertionError("guarded tick should rethrow");
        } catch (NullPointerException expected) {
            check("tint".equals(expected.getMessage()), "original tint failure is rethrown");
        }
        Field running = samplerClass.getDeclaredField("running");
        running.setAccessible(true);
        check(!running.getBoolean(sampler), "interrupted sample was abandoned");
        start.invoke(sampler);

        System.out.println("TintSafetyHarness passed");
    }

    private static void invokeGetBlockTint(String className, Biome biome, ColorResolver resolver, int expected, String label) throws Exception {
        Class<?> type = Class.forName(className);
        Constructor<?> constructor = type.getDeclaredConstructor(Biome.class, BlockState.class);
        constructor.setAccessible(true);
        Object getter = constructor.newInstance(biome, new BlockState());
        Method tint = type.getMethod("getBlockTint", BlockPos.class, ColorResolver.class);
        int colour = (Integer) tint.invoke(getter, null, resolver);
        check(colour == expected, label + " got " + Integer.toHexString(colour));
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        System.out.println("ok " + label);
    }
}
