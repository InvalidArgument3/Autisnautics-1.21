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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
        this.levelDecrease = levelDecrease;
        this.tickRate = tickRate;
        this.infinite = infinite;
        this.fillSound = fillSound;

        still = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, baseName), makeStill());
        flowing = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, "flowing_" + baseName), makeFlowing());

        if (makeBlock)
            block = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, baseName), makeBlock(still, MeatlibBlockSettings.copy(Blocks.WATER)));
        else
            block = null;

        if (makeItem)
        {
            bucketItem = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, baseName + "_bucket"), new BaseBucketItem(still, new MeatlibItemSettings().stacksTo(1)
                    .craftRemainder(Items.BUCKET)
                    .group(itemGroup))
            );

            CreativeModeTab group = ((MeatlibItemExtension) bucketItem).meatlib$getItemGroup();
            if (group != null)
            {
                MeatlibItemGroups.add(group, bucketItem);
            }
        }
        else
        {
            bucketItem = null;
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
