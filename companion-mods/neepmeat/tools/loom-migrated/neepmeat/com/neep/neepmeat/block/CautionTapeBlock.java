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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public class CautionTapeBlock extends BaseBlock
{
    public static final EnumProperty<RedstoneSide> CONNECTION_NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> CONNECTION_EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> CONNECTION_SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> CONNECTION_WEST = BlockStateProperties.WEST_REDSTONE;
    public static final Map<Direction, EnumProperty<RedstoneSide>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, CONNECTION_NORTH, Direction.EAST, CONNECTION_EAST, Direction.SOUTH, CONNECTION_SOUTH, Direction.WEST, CONNECTION_WEST));
    public BlockState dotState;

    public CautionTapeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(CONNECTION_NORTH, RedstoneSide.NONE).setValue(CONNECTION_EAST, RedstoneSide.NONE).setValue(CONNECTION_SOUTH, RedstoneSide.NONE).setValue(CONNECTION_WEST, RedstoneSide.NONE));
        this.dotState = this.defaultBlockState().setValue(CONNECTION_NORTH, RedstoneSide.SIDE).setValue(CONNECTION_EAST, RedstoneSide.SIDE).setValue(CONNECTION_SOUTH, RedstoneSide.SIDE).setValue(CONNECTION_WEST, RedstoneSide.SIDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.getPlacementState(ctx.getLevel(), dotState, ctx.getClickedPos());
    }

    private BlockState getPlacementState(BlockGetter world, BlockState state, BlockPos pos)
    {
        state = this.getDefaultWireState(world, defaultBlockState(), pos);
        if (isNotConnected(state))
        {
            return state;
        }
        boolean north = state.getValue(CONNECTION_NORTH).isConnected();
        boolean east = state.getValue(CONNECTION_SOUTH).isConnected();
        boolean south = state.getValue(CONNECTION_EAST).isConnected();
        boolean west = state.getValue(CONNECTION_WEST).isConnected();
        boolean bl6 = !north && !east;
        boolean bl7 = !south && !west;
        if (!west && bl6)
        {
            state = state.setValue(CONNECTION_WEST, RedstoneSide.SIDE);
        }
        if (!south && bl6)
        {
            state = state.setValue(CONNECTION_EAST, RedstoneSide.SIDE);
        }
        if (!north && bl7)
        {
            state = state.setValue(CONNECTION_NORTH, RedstoneSide.SIDE);
        }
        if (!east && bl7)
        {
            state = state.setValue(CONNECTION_SOUTH, RedstoneSide.SIDE);
        }
        return state;
    }

    private BlockState getDefaultWireState(BlockGetter world, BlockState state, BlockPos pos)
    {
        boolean bl = !world.getBlockState(pos.above()).isRedstoneConductor(world, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (state.getValue(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction)).isConnected()) continue;
            RedstoneSide wireConnection = this.getRenderConnectionType(world, pos, direction, bl);
            state = state.setValue(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (direction == Direction.DOWN)
        {
            return state;
        }
        if (direction == Direction.UP)
        {
            return this.getPlacementState(world, state, pos);
        }
        RedstoneSide connection = this.getRenderConnectionType(world, pos, direction, true);
        if (connection.isConnected() == state.getValue(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction)).isConnected() && !isFullyConnected(state))
        {
            return state.setValue(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), connection);
        }
        return this.getPlacementState(world, this.dotState.setValue(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), connection), pos);
    }

    protected static boolean connectsTo(BlockState state, @Nullable Direction dir)
    {
//        return state.isOf(NMBlocks.CAUTION_TAPE);
        return false;
    }

    private RedstoneSide getRenderConnectionType(BlockGetter world, BlockPos pos, Direction direction, boolean bl)
    {
        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (bl && this.canRunOnTop(world, blockPos, blockState) && connectsTo(world.getBlockState(blockPos.above()), null))
        {
            if (blockState.isFaceSturdy(world, blockPos, direction.getOpposite()))
            {
                return RedstoneSide.UP;
            }
            return RedstoneSide.SIDE;
        }
        if (connectsTo(blockState, direction))
        {
            return RedstoneSide.SIDE;
        }
        return RedstoneSide.NONE;
    }

    private static boolean isFullyConnected(BlockState state)
    {
        return state.getValue(CONNECTION_NORTH).isConnected() && state.getValue(CONNECTION_SOUTH).isConnected() && state.getValue(CONNECTION_EAST).isConnected() && state.getValue(CONNECTION_WEST).isConnected();
    }

    private static boolean isNotConnected(BlockState state)
    {
        return !state.getValue(CONNECTION_NORTH).isConnected() && !state.getValue(CONNECTION_SOUTH).isConnected() && !state.getValue(CONNECTION_EAST).isConnected() && !state.getValue(CONNECTION_WEST).isConnected();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockPos blockPos = pos.below();
        BlockState blockState = world.getBlockState(blockPos);
        return this.canRunOnTop(world, blockPos, blockState);
    }

    private boolean canRunOnTop(BlockGetter world, BlockPos pos, BlockState floor)
    {
        return floor.isFaceSturdy(world, pos, Direction.UP) || floor.is(Blocks.HOPPER);
    }

    private void updateNeighbors(Level world, BlockPos pos)
    {
        if (!world.getBlockState(pos).is(this))
        {
            return;
        }
        world.updateNeighborsAt(pos, this);
        for (Direction direction : Direction.values())
        {
            world.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        if (oldState.is(state.getBlock()) || world.isClientSide)
        {
            return;
        }
        for (Direction direction : Direction.Plane.VERTICAL)
        {
            world.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (moved || state.is(newState.getBlock()))
        {
            return;
        }
        super.onRemove(state, world, pos, newState, moved);
        if (world.isClientSide)
        {
            return;
        }
        for (Direction direction : Direction.values())
        {
            world.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(CONNECTION_NORTH, CONNECTION_EAST, CONNECTION_SOUTH, CONNECTION_WEST);
    }
}
