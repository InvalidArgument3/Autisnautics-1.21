package dev.wyseman.moltensea;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class MoltenDepths {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("autisnautics", "molten_depths");
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);

    /** Inclusive top of the solid floor cap. Postgen clears Y=-64 through this Y. */
    public static final int FLOOR_CAP_TOP = -40;
    /** Inclusive bottom of the solid ceiling cap. */
    public static final int CEILING_CAP_BOTTOM = 295;

    private MoltenDepths() {}

    public static boolean capColumn(int y) {
        return y <= FLOOR_CAP_TOP || y >= CEILING_CAP_BOTTOM;
    }
}
