package dev.wyseman.moltensea;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/**
 * Create Deep Seas is optional. Its compartment API is reached by reflection so this project
 * does not compile against that jar (the mod's license is All Rights Reserved).
 */
public final class DeepSeasBridge {
    private static final String TRACKER = "com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker";
    private static volatile boolean resolved;
    private static Method sealedExact;
    private static Method occluded;
    private static Method realFluid;

    private DeepSeasBridge() {}

    public static boolean loaded() {
        try {
            return ModList.get().isLoaded("create_submarine");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean sealed(Level level, Vec3 pos) {
        resolve();
        return callBoolean(sealedExact, level, pos);
    }

    public static boolean occluded(Level level, BlockPos pos) {
        resolve();
        return callBoolean(occluded, level, pos);
    }

    public static FluidState realFluid(Level level, BlockPos pos) {
        resolve();
        if (realFluid == null || level == null || pos == null) {
            return Fluids.EMPTY.defaultFluidState();
        }
        try {
            Object value = realFluid.invoke(null, level, pos);
            if (value instanceof FluidState state) {
                return state;
            }
        } catch (Throwable ignored) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    private static boolean callBoolean(Method method, Object first, Object second) {
        if (method == null || first == null || second == null) {
            return false;
        }
        try {
            Object value = method.invoke(null, first, second);
            return value instanceof Boolean flag && flag;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        synchronized (DeepSeasBridge.class) {
            if (resolved) {
                return;
            }
            resolved = true;
            if (!loaded()) {
                return;
            }
            try {
                Class<?> type = Class.forName(TRACKER);
                sealedExact = type.getMethod("isInSealedExact", Level.class, Vec3.class);
                occluded = type.getMethod("isOccluded", Level.class, BlockPos.class);
                realFluid = type.getMethod("realFluidState", Level.class, BlockPos.class);
            } catch (Throwable error) {
                MoltenSea.LOG.warn("Create Deep Seas is present, but CompartmentTracker hooks were not found. Molten hull integration is off.", error);
            }
        }
    }
}
