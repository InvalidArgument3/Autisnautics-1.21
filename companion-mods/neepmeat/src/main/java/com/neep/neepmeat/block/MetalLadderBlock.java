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

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class MetalLadderBlock extends LadderBlock implements MeatlibBlock
{
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public MetalLadderBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        ctx.append(this, itemSettings.create(this, ctx));
        registerDefaultState(getStateDefinition().any().setValue(TOP, false));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return true;
    }

    private BlockState getState(LevelAccessor world, BlockPos pos, BlockState state)
    {
        Direction facing = state.getValue(FACING);
        boolean ladderAbove = world.getBlockState(pos.above()).getBlock() instanceof MetalLadderBlock;
        BlockPos offset = pos.relative(facing.getOpposite());
        boolean solidUpFace = world.getBlockState(offset).isFaceSturdy(world, offset, Direction.UP);

        return state.setValue(TOP, !ladderAbove && solidUpFace);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        if (!ctx.replacingClickedOnBlock())
        {
            BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite()));
            if (blockState.is(this) && blockState.getValue(FACING) == ctx.getClickedFace())
            {
                return null;
            }
        }

        LevelAccessor world = ctx.getLevel();
        FluidState fluidState = world.getFluidState(ctx.getClickedPos());

        BlockState blockState = this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return getState(world, ctx.getClickedPos(), blockState);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        return getState(world, pos, super.updateShape(state, direction, neighborState, world, pos, neighborPos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(TOP);
    }
}
