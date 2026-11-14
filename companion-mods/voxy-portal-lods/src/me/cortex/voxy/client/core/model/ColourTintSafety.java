package me.cortex.voxy.client.core.model;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Null-safe tint capture for Voxy's fake {@code BlockAndTintGetter}.
 *
 * <p>Regions Unexplored grass tints call {@code BiomeColors.getAverageGrassColor}, which calls
 * {@code getBlockTint}. A null biome (a hole in the sparse biome id list) makes
 * {@code ColorResolver.getColor} throw inside {@code ModelFactory$2.getBlockTint}. A mod colour
 * provider that throws must not take down the renderer either.
 */
public final class ColourTintSafety {
    /**
     * {@code GrassColor.get(0.5, 1.0)} on the vanilla 1.21.1 grass colormap. That is the plains
     * sample the item tint uses when no world is available.
     */
    public static final int PLAINS_GRASS_TINT = 0x7CBD6B;

    static final Set<String> LOGGED_PROVIDER_FAILURES = ConcurrentHashMap.newKeySet();

    private static final AtomicBoolean LOGGED_NULL_BIOME = new AtomicBoolean();
    private static final AtomicBoolean LOGGED_NULL_RESOLVER = new AtomicBoolean();
    private static final AtomicBoolean LOGGED_PLAINS_FAILURE = new AtomicBoolean();

    private static volatile Object plainsBiome;
    private static volatile boolean plainsLookupFailed;
    private static volatile Method modelTick;

    private ColourTintSafety() {}

    /**
     * {@code ModelFactory$2.getBlockTint}. Missing biome or resolver data falls back to the plains
     * biome colour from the same resolver, then to {@link #PLAINS_GRASS_TINT}.
     */
    public static int safeBlockTint(Biome biome, ColorResolver colorResolver) {
        if (colorResolver == null) {
            if (LOGGED_NULL_RESOLVER.compareAndSet(false, true)) {
                warn("Voxy block tint saw a null colour resolver; using the plains grass tint");
            }
            return PLAINS_GRASS_TINT;
        }
        if (biome != null) {
            Integer colour = invokeResolver(colorResolver, biome);
            if (colour != null) {
                return colour;
            }
        } else if (LOGGED_NULL_BIOME.compareAndSet(false, true)) {
            warn("Voxy block tint saw a null biome; using the plains colour");
        }
        Biome plains = plainsBiome();
        if (plains != null && plains != biome) {
            Integer colour = invokeResolver(colorResolver, plains);
            if (colour != null) {
                return colour;
            }
        }
        return PLAINS_GRASS_TINT;
    }

    /**
     * Probe getter used by {@code getColourProvider}. A null world still means "no colour yet"
     * ({@code -1}), which is what the probe loop already treats as a miss.
     */
    public static int safeProbeTint(Biome probeBiome, ColorResolver colorResolver) {
        if (probeBiome == null || colorResolver == null) {
            return -1;
        }
        Integer colour = invokeResolver(colorResolver, probeBiome);
        return colour == null ? -1 : colour;
    }

    /**
     * {@code captureColourConstant} / {@code isBiomeDependentColour}. One bad provider is logged
     * once per block and captured as "no tint".
     */
    public static int safeGetColor(BlockColor provider, BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        try {
            return provider.getColor(state, world, pos, tintIndex);
        } catch (Throwable thrown) {
            logProviderFailure(state, thrown);
            return -1;
        }
    }

    /**
     * {@code getColourProvider} lambdas delegate to {@code BlockColors.getColor}. Same once-per-block
     * log and no-tint fallback, so the exception never leaves the lambda.
     */
    public static int safeBlockColorsGet(BlockColors blockColors, BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        try {
            return blockColors.getColor(state, world, pos, tintIndex);
        } catch (Throwable thrown) {
            logProviderFailure(state, thrown);
            return -1;
        }
    }

    /**
     * Runs {@code ModelBakerySubsystem.tick} and abandons any sampler the tick left open when it
     * throws. {@code renderOpaqueImpl} calls this instead of {@code tick} directly.
     */
    public static void guardedModelTick(Object modelBakery, long budget) {
        try {
            Method tick = modelTick;
            if (tick == null || tick.getDeclaringClass() != modelBakery.getClass()) {
                tick = modelBakery.getClass().getMethod("tick", long.class);
                modelTick = tick;
            }
            tick.invoke(modelBakery, budget);
        } catch (InvocationTargetException wrapped) {
            abandonRunningSamplers();
            Throwable cause = wrapped.getCause() == null ? wrapped : wrapped.getCause();
            rethrow(cause);
        } catch (Throwable thrown) {
            abandonRunningSamplers();
            rethrow(thrown);
        }
    }

    private static void abandonRunningSamplers() {
        try {
            Class.forName("me.cortex.voxy.client.TimingStatistics").getMethod("abandonRunningSamplers").invoke(null);
        } catch (Throwable ignored) {
        }
    }

    private static Integer invokeResolver(ColorResolver colorResolver, Biome biome) {
        try {
            return colorResolver.getColor(biome, 0.0, 0.0);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void logProviderFailure(BlockState state, Throwable thrown) {
        String key = state == null ? "<null block>" : String.valueOf(state);
        if (!LOGGED_PROVIDER_FAILURES.add(key)) {
            return;
        }
        warn("Voxy colour provider failed for " + key + "; capturing no tint", thrown);
    }

    private static void warn(String message, Throwable thrown) {
        warn(new Object[]{message, thrown});
    }

    private static void warn(String message) {
        warn(new Object[]{message});
    }

    private static void warn(Object[] arguments) {
        try {
            Class<?> logger = Class.forName("me.cortex.voxy.common.Logger");
            Method method = logger.getMethod("warn", Object[].class);
            method.invoke(null, (Object) arguments);
        } catch (Throwable ignored) {
        }
    }

    private static void rethrow(Throwable thrown) {
        if (thrown instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (thrown instanceof Error error) {
            throw error;
        }
        throw new RuntimeException(thrown);
    }

    @SuppressWarnings("unchecked")
    private static Biome plainsBiome() {
        if (plainsBiome != null || plainsLookupFailed) {
            return (Biome) plainsBiome;
        }
        synchronized (ColourTintSafety.class) {
            if (plainsBiome != null || plainsLookupFailed) {
                return (Biome) plainsBiome;
            }
            try {
                Class<?> minecraft = Class.forName("net.minecraft.client.Minecraft");
                Object client = minecraft.getMethod("getInstance").invoke(null);
                Object level = minecraft.getField("level").get(client);
                if (level == null) {
                    return null;
                }
                Object access = level.getClass().getMethod("registryAccess").invoke(level);
                Object biomeRegistryKey = Class.forName("net.minecraft.core.registries.Registries").getField("BIOME").get(null);
                Object lookup = invokeOneArg(access, "lookupOrThrow", biomeRegistryKey);
                if (lookup == null) {
                    lookup = invokeOneArg(access, "registryOrThrow", biomeRegistryKey);
                }
                Object plainsKey = Class.forName("net.minecraft.world.level.biome.Biomes").getField("PLAINS").get(null);
                Object holder = invokeOneArg(lookup, "getOrThrow", plainsKey);
                Object value = holder.getClass().getMethod("value").invoke(holder);
                plainsBiome = value;
                return (Biome) value;
            } catch (ClassNotFoundException missing) {
                plainsLookupFailed = true;
                return null;
            } catch (Throwable failed) {
                plainsLookupFailed = true;
                if (LOGGED_PLAINS_FAILURE.compareAndSet(false, true)) {
                    warn("Voxy could not resolve the plains biome for tint fallback", failed);
                }
                return null;
            }
        }
    }

    private static Object invokeOneArg(Object target, String name, Object argument) throws ReflectiveOperationException {
        if (target == null || argument == null) {
            return null;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != 1) {
                continue;
            }
            if (method.getParameterTypes()[0].isInstance(argument)) {
                return method.invoke(target, argument);
            }
        }
        return null;
    }
}
