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

package com.neep.meatlib.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class BasePaneBlock extends IronBarsBlock implements MeatlibBlock
{
    protected BlockItem blockItem;

    public BasePaneBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        this.blockItem = itemSettings.create(this, ctx);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockGetter blockView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.south();
        BlockPos blockPos4 = blockPos.west();
        BlockPos blockPos5 = blockPos.east();
        BlockState blockState = blockView.getBlockState(blockPos2);
        BlockState blockState2 = blockView.getBlockState(blockPos3);
        BlockState blockState3 = blockView.getBlockState(blockPos4);
        BlockState blockState4 = blockView.getBlockState(blockPos5);
        return this.defaultBlockState()
                .setValue(NORTH, connectsTo(blockState, Direction.SOUTH, blockState.isFaceSturdy(blockView, blockPos2, Direction.SOUTH)))
                .setValue(SOUTH, connectsTo(blockState2, Direction.NORTH, blockState2.isFaceSturdy(blockView, blockPos3, Direction.NORTH)))
                .setValue(WEST, connectsTo(blockState3, Direction.EAST, blockState3.isFaceSturdy(blockView, blockPos4, Direction.EAST)))
                .setValue(EAST, connectsTo(blockState4, Direction.WEST, blockState4.isFaceSturdy(blockView, blockPos5, Direction.WEST)))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return direction.getAxis().isHorizontal()
                ? state.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(neighborState, direction.getOpposite(), neighborState.isFaceSturdy(world, neighborPos, direction.getOpposite())))
                : state;
    }

    // Could have used an AW, but I couldn't be bothered
    protected boolean connectsTo(BlockState state, Direction side, boolean sideSolidFullSquare)
    {
        boolean bl = state.getBlock() instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(state, side);

        return bl || !isExceptionForConnection(state) && sideSolidFullSquare || state.getBlock() instanceof IronBarsBlock || state.is(BlockTags.WALLS);
    }
}
