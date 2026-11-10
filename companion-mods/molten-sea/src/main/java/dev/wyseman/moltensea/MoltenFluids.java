package dev.wyseman.moltensea;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;

public final class MoltenFluids {
    public static final TagKey<Fluid> SEA = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath("autisnautics", "molten_sea"));

    private static volatile Set<Fluid> fluids = Set.of();
    private static volatile Set<FluidType> types = Set.of();

    private MoltenFluids() {}

    public static void rebuild(RegistryAccess access) {
        Set<Fluid> nextFluids = new HashSet<>();
        Set<FluidType> nextTypes = new HashSet<>();
        for (Holder<Fluid> holder : access.registryOrThrow(Registries.FLUID).getTagOrEmpty(SEA)) {
            Fluid fluid = holder.value();
            if (fluid == null || fluid == Fluids.EMPTY) {
                continue;
            }
            nextFluids.add(fluid);
            FluidType type = fluid.getFluidType();
            if (type != null) {
                nextTypes.add(type);
            }
        }
        fluids = Collections.unmodifiableSet(nextFluids);
        types = Collections.unmodifiableSet(nextTypes);
        MoltenSea.LOG.info("Molten sea tag has {} fluids", nextFluids.size());
    }

    public static boolean isSea(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        return fluids.contains(fluid);
    }

    public static boolean isSeaType(FluidType type) {
        return type != null && types.contains(type);
    }

    public static boolean bothSea(Fluid existing, Fluid incoming) {
        return isSea(existing) && isSea(incoming);
    }
}
