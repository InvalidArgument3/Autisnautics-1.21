/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class WorkFluid {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeepMeat.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final DeferredHolder<FluidType, FluidType> TYPE = TYPES.register("work_fluid",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.neepmeat.work_fluid").density(1200).viscosity(1500)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> STILL = FLUIDS.register("work_fluid",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_work_fluid",
            () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredItem<BucketItem> BUCKET = ITEMS.register("work_fluid_bucket",
            () -> new BucketItem(STILL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private WorkFluid() {}

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, STILL, FLOWING).bucket(BUCKET);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        ITEMS.register(bus);
    }
}
