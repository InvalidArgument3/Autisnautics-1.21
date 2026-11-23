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

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.fluid.BaseFluid;
import com.neep.meatlib.item.BaseBucketItem;
import com.neep.meatlib.item.MeatlibItem;
import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.util.MeatlibItemGroups;
import com.neep.neepmeat.NMItemGroups;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Deprecated
public class FluidFactory
{
    public final String namespace;
    public final String baseName;
    public final String flowingName;
    public final String stillName;
    public final String bucketName;

    protected FlowingFluid still;
    protected FlowingFluid flowing;
    protected Block block;
    protected Item bucketItem = Items.AIR;

    protected final boolean isInfinite;
    protected final int tickRate;
    protected final int levelDecrease;

    public FluidFactory(final String namespace, final String baseName, boolean isInfinite, int tickRate, int levelDecrease)
    {
        this.namespace = namespace;
        this.baseName = baseName;
        this.flowingName = "flowing_" + baseName;
        this.stillName = baseName;
        this.bucketName = baseName + "_bucket";

        this.isInfinite = isInfinite;
        this.tickRate = tickRate;
        this.levelDecrease = levelDecrease;
    }

    public FlowingFluid registerStill()
    {
        still = makeStill();
        Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, stillName), still);
        return still;
    }

    public FlowingFluid registerFlowing()
    {
        flowing = makeFlowing();
        Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, flowingName), flowing);
        return flowing;
    }

    protected FlowingFluid makeStill()
    {
        return new Still();
    }

    protected FlowingFluid makeFlowing()
    {
        return new Flowing();
    }

    public Item registerItem()
    {
        if (bucketItem != Items.AIR)
            throw new IllegalStateException("A bucket item is already registered for fluid '" + baseName + "'");

        bucketItem = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, bucketName), new BaseBucketItem(still, new MeatlibItemSettings().stacksTo(1).craftRemainder(Items.BUCKET).group(NMItemGroups.GENERAL)));

        CreativeModeTab group = ((MeatlibItemExtension) bucketItem).meatlib$getItemGroup();
        if (group != null)
        {
            MeatlibItemGroups.add(group, bucketItem);
        }

        return bucketItem;
    }

    public Block registerBlock()
    {
        block = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, baseName), new LiquidBlock(still, MeatlibBlockSettings.copy(Blocks.WATER)){});
        return block;
    }

    protected abstract class Main extends BaseFluid
    {
        @Override
        protected int getDropOff(LevelReader worldView)
        {
            return levelDecrease;
        }

        @Override
        public int getTickDelay(LevelReader worldView)
        {
            return tickRate;
        }

        @Override
        public Fluid getSource()
        {
            return still;
        }

        @Override
        public Fluid getFlowing()
        {
            return flowing;
        }

        @Override
        public Item getBucket()
        {
            return bucketItem;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState fluidState)
        {
            return block.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(fluidState));
        }
    }

    private class Flowing extends Main
    {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState fluidState)
        {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState)
        {
            return false;
        }
    }

    private class Still extends Main
    {
        @Override
        public int getAmount(FluidState fluidState)
        {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState)
        {
            return true;
        }
    }
}
