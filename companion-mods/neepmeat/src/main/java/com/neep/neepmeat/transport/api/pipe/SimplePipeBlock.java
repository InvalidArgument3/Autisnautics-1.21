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

package com.neep.neepmeat.transport.api.pipe;

import com.google.common.collect.Maps;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.fluid_network.SimplePipeProperties;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SimplePipeBlock extends Block implements MeatlibBlock
{
    public final BlockItem blockItem;

    public static final EnumProperty<ConnectionType> NORTH_CONNECTION = SimplePipeProperties.NORTH_CONNECTION;
    public static final EnumProperty<ConnectionType> EAST_CONNECTION = SimplePipeProperties.EAST_CONNECTION;
    public static final EnumProperty<ConnectionType> SOUTH_CONNECTION = SimplePipeProperties.SOUTH_CONNECTION;
    public static final EnumProperty<ConnectionType> WEST_CONNECTION = SimplePipeProperties.WEST_CONNECTION;
    public static final EnumProperty<ConnectionType> UP_CONNECTION = SimplePipeProperties.UP_CONNECTION;
    public static final EnumProperty<ConnectionType> DOWN_CONNECTION = SimplePipeProperties.DOWN_CONNECTION;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final Map<Direction, EnumProperty<ConnectionType>> DIR_TO_CONNECTION = Util.make(new EnumMap<>(Direction.class), map ->
    {
        map.put(Direction.NORTH, NORTH_CONNECTION);
        map.put(Direction.EAST, EAST_CONNECTION);
        map.put(Direction.SOUTH, SOUTH_CONNECTION);
        map.put(Direction.WEST, WEST_CONNECTION);
        map.put(Direction.DOWN, DOWN_CONNECTION);
        map.put(Direction.UP, UP_CONNECTION);
    });

    public static final Map<Direction, VoxelShape> DIR_SHAPES = Util.make(new EnumMap<>(Direction.class), map ->
    {
        map.put(Direction.NORTH, Block.box(4, 4, 0, 12, 12, 5));
        map.put(Direction.EAST, Block.box(11, 4, 4, 16, 12, 12));
        map.put(Direction.SOUTH, Block.box(4, 4, 11, 12, 12, 16));
        map.put(Direction.WEST, Block.box(0, 4, 4, 5, 12, 12));
        map.put(Direction.UP, Block.box(4, 11, 4, 12, 16, 12));
        map.put(Direction.DOWN, Block.box(4, 0, 4, 12, 5, 12));
    });

    private final Map<BlockState, VoxelShape> shapes = Maps.newHashMap();

    public SimplePipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH_CONNECTION, ConnectionType.NONE)
                .setValue(EAST_CONNECTION, ConnectionType.NONE)
                .setValue(SOUTH_CONNECTION, ConnectionType.NONE)
                .setValue(WEST_CONNECTION, ConnectionType.NONE)
                .setValue(UP_CONNECTION, ConnectionType.NONE)
                .setValue(DOWN_CONNECTION, ConnectionType.NONE)
                .setValue(WATERLOGGED, false)
        );

        this.blockItem = itemSettings.getFactory().create(this, ctx, itemSettings);

        for (BlockState state : this.getStateDefinition().getPossibleStates())
        {
            shapes.put(state, getShapeForState(state));
        }
    }

    protected VoxelShape getCentreShape()
    {
        return Block.box(4, 4, 4, 12, 12, 12);
    }

    public VoxelShape getShapeForState(BlockState state)
    {
        VoxelShape shape = getCentreShape();
        for (Direction direction : Direction.values())
        {
            if (state.getValue(DIR_TO_CONNECTION.get(direction)) == ConnectionType.SIDE)
            {
                shape = Shapes.or(shape, DIR_SHAPES.get(direction));
            }
        }
        return shape;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

        if (connection)
        {
            return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.SIDE);
        }

        return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.NONE);
    }

    protected BlockState getConnectedState(BlockGetter world, BlockState state, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            ConnectionType property = state.getValue(DIR_TO_CONNECTION.get(direction));

            if (property == ConnectionType.SIDE)
                continue;

            BlockPos adjPos = pos.relative(direction);
            BlockState adjState = world.getBlockState(adjPos);
            boolean connection  = canConnectTo(adjState, direction.getOpposite(), (Level) world, adjPos);
            state = state.setValue(DIR_TO_CONNECTION.get(direction), connection ? ConnectionType.SIDE : ConnectionType.NONE);
        }
        return state.setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        return this.getConnectedState(world, this.defaultBlockState(), pos);
    }

    public abstract boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(
                NORTH_CONNECTION,
                EAST_CONNECTION,
                SOUTH_CONNECTION,
                WEST_CONNECTION,
                UP_CONNECTION,
                DOWN_CONNECTION,
                WATERLOGGED
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }
}
