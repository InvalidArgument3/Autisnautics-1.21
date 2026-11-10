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

package com.neep.meatlib.fluid;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBucketItem;
import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.util.MeatlibItemGroups;
import com.neep.meatlib.transfer.FluidVariant;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public abstract class BuiltFluid<Still extends FlowingFluid, Flowing extends FlowingFluid>
{
    private static final List<BuiltFluid<?, ?>> PENDING = new ArrayList<>();
    private static boolean listening;

    private final String namespace;
    private final String baseName;
    protected final int levelDecrease;
    protected final int tickRate;
    protected final boolean infinite;
    protected final Item bucketItem;
    protected final Flowing flowing;
    protected final Still still;
    protected final LiquidBlock block;
    
    @Nullable
    protected final SoundEvent fillSound;

    public BuiltFluid(String namespace, String baseName, int levelDecrease, int tickRate, boolean infinite, boolean makeBlock, boolean makeItem, @Nullable SoundEvent fillSound, @Nullable CreativeModeTab itemGroup)
    {
        this.namespace = namespace;
        this.baseName = baseName;
        this.levelDecrease = levelDecrease;
        this.tickRate = tickRate;
        this.infinite = infinite;
        this.fillSound = fillSound;

        still = makeStill();
        flowing = makeFlowing();

        if (makeBlock)
            block = makeBlock(still, MeatlibBlockSettings.copyOf(Blocks.WATER).properties());
        else
            block = null;

        if (makeItem)
        {
            bucketItem = new BaseBucketItem(still, new MeatlibItemSettings().stacksTo(1)
                    .craftRemainder(Items.BUCKET)
                    .group(itemGroup));

            if (bucketItem instanceof MeatlibItemExtension extension)
            {
                CreativeModeTab group = extension.meatlib$getItemGroup();
                if (group != null)
                {
                    MeatlibItemGroups.add(group, bucketItem);
                }
            }
        }
        else
        {
            bucketItem = null;
        }
        PENDING.add(this);
    }

    /** NeoForge freezes registries before the mod constructor. The objects are built immediately; the entries are published on {@link net.neoforged.neoforge.registries.RegisterEvent}. */
    public static void registerPending(net.neoforged.bus.api.IEventBus bus) {
        if (listening) {
            return;
        }
        listening = true;
        bus.addListener((net.neoforged.neoforge.registries.RegisterEvent event) -> onRegister(event));
    }

    private static void onRegister(net.neoforged.neoforge.registries.RegisterEvent event) {
        for (BuiltFluid<?, ?> fluid : PENDING) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(fluid.namespace, fluid.baseName);
            if (event.getRegistryKey() == Registries.FLUID) {
                event.register(Registries.FLUID, id, () -> fluid.still);
                event.register(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(fluid.namespace, "flowing_" + fluid.baseName), () -> fluid.flowing);
            } else if (event.getRegistryKey() == Registries.BLOCK && fluid.block != null) {
                event.register(Registries.BLOCK, id, () -> fluid.block);
            } else if (event.getRegistryKey() == Registries.ITEM && fluid.bucketItem != null) {
                event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(fluid.namespace, fluid.baseName + "_bucket"), () -> fluid.bucketItem);
            }
        }
    }

    protected abstract Still makeStill();

    protected abstract Flowing makeFlowing();

    protected LiquidBlock makeBlock(Fluid fluid, BlockBehaviour.Properties settings)
    {
        return new LiquidBlock(flowing, settings);
    }

    public Still still()
    {
        return still;
    }

    public Flowing flowing()
    {
        return flowing;
    }

    public LiquidBlock getBlock()
    {
        return block;
    }

    public FluidVariant variant()
    {
        return FluidVariant.of(still);
    }

    public Item getItem()
    {
        return bucketItem;
    }
}
