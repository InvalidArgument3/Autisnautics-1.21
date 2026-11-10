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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.*;
import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class MetalScaffoldingBlock extends BaseBlock implements MeatlibBlock, SimpleWaterloggedBlock
{
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public final MeatlibBlock stairs;
    public final MeatlibBlock slab;

    public MetalScaffoldingBlock(RegistrationContext ctx, ItemSettings itemSettings, MeatlibBlockSettings settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());

        stairs = new BaseStairsBlock(ctx, this.defaultBlockState(), itemSettings, settings);
        ctx.append(this, stairs, s -> s + "_stairs");

        slab = new BaseSlabBlock(ctx, this.defaultBlockState(), itemSettings, settings);
        ctx.append(this, slab, s -> s + "_slab");

        this.registerDefaultState((this.stateDefinition.any()).setValue(WATERLOGGED, false).setValue(BOTTOM, false));
    }

    public BlockItem getBlockItem()
    {
        return blockItem;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockPos blockPos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        return (this.defaultBlockState()
                .setValue(WATERLOGGED, world.getFluidState(blockPos).getType() == Fluids.WATER))
                .setValue(BOTTOM, this.shouldBeBottom(world, blockPos));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        state = state.setValue(BOTTOM, world.getBlockState(pos.above()).getBlock() instanceof MetalScaffoldingBlock);

        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
//        if (!world.isClient())
//        {
//            world.scheduleBlockTick(pos, this, 1);
//        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(WATERLOGGED, BOTTOM);
    }

    private boolean shouldBeBottom(BlockGetter world, BlockPos pos)
    {
        return world.getBlockState(pos.below()).is(this);
    }

    public void generateRecipe(Consumer<FinishedRecipe> exporter)
    {
        MeatRecipeProvider.offerStairsRecipe(exporter, stairs, this);
        MeatRecipeProvider.slab(exporter, RecipeCategory.BUILDING_BLOCKS, slab, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, this.stairs, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, this.slab, this);
    }
}
