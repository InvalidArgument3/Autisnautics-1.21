/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The nine fluids upstream built with FluidBuilder. Ids, tick rate, level
 * decrease, and sludge's persistent block match that registration.
 */

package com.neep.neepmeat.fluid;

import com.neep.meatlib.fluid.BuiltFluid;
import com.neep.meatlib.fluid.FluidBuilder;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class CatalogueFluids {
    private CatalogueFluids() {}

    public static void register(IEventBus bus) {
        BuiltFluid.registerPending(bus);
        bus.addListener(EventPriority.HIGHEST, CatalogueFluids::buildAll);
    }

    private static void buildAll(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.FLUID || NMFluids.SLUDGE != null) {
            return;
        }
        NMFluids.PATINA_TREATMENT = build("patina_treatment", 5, 2, true, false);
        NMFluids.ELDRITCH_ENZYMES = build("eldritch_enzymes", 5, 2, true, false);
        NMFluids.P_MILK = build("pasteurised_milk", 5, 1, true, false);
        NMFluids.ANIMAL_FEED = new FluidBuilder(NeepMeat.NAMESPACE, "animal_feed")
                .tickRate(5).levelDecrease(4).fillSound(SoundEvents.SAND_PLACE)
                .withBlock().withItem(null).build();
        NMFluids.PINKDRINK = build("pinkdrink", 5, 2, true, false);
        NMFluids.SLUDGE = new FluidBuilder(NeepMeat.NAMESPACE, "sludge")
                .withBlock().fluidFactory(BuiltSludgeFluid::new).build();
        NMFluids.FAT = build("fat", 5, 2, true, false);
        NMFluids.LIQUID_CLAY = build("liquid_clay", 5, 2, true, false);
        NMFluids.OIL_OF_BROME = build("oil_of_brome", 5, 2, true, false);
    }

    private static BuiltFluid<?, ?> build(String name, int tickRate, int levelDecrease, boolean block, boolean noItem) {
        FluidBuilder builder = new FluidBuilder(NeepMeat.NAMESPACE, name).tickRate(tickRate).levelDecrease(levelDecrease);
        if (block) {
            builder.withBlock();
        }
        if (!noItem) {
            builder.withItem(null);
        }
        return builder.build();
    }
}
