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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.transport.fluid_network.PipeConnectionType;
import com.neep.neepmeat.transport.fluid_network.PipeProperties;
import com.neep.meatlib.util.MiscUtil;
import com.neep.neepmeat.util.NMMaths;
import com.neep.neepmeat.util.NMVec2f;
import net.minecraft.Util;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractPipeBlock extends BaseBlock implements SimpleWaterloggedBlock
{
    public static final EnumProperty<PipeConnectionType> NORTH_CONNECTION = EnumProperty.create("north", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> EAST_CONNECTION = PipeProperties.EAST_CONNECTION;
    public static final EnumProperty<PipeConnectionType> SOUTH_CONNECTION = PipeProperties.SOUTH_CONNECTION;
    public static final EnumProperty<PipeConnectionType> WEST_CONNECTION = PipeProperties.WEST_CONNECTION;
    public static final EnumProperty<PipeConnectionType> UP_CONNECTION = PipeProperties.UP_CONNECTION;
    public static final EnumProperty<PipeConnectionType> DOWN_CONNECTION = PipeProperties.DOWN_CONNECTION;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Map<BlockState, VoxelShape> shapes = Maps.newHashMap();

//    public static final Map<Direction, EnumProperty<PipeConnectionType>> DIR_TO_CONNECTION = new ImmutableMap.Builder<Direction, EnumProperty<PipeConnectionType>>()
    public static final Map<Direction, EnumProperty<PipeConnectionType>> DIR_TO_CONNECTION = Util.make(() ->
    {
        EnumMap<Direction, EnumProperty<PipeConnectionType>> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, NORTH_CONNECTION);
        map.put(Direction.EAST, EAST_CONNECTION);
        map.put(Direction.SOUTH, SOUTH_CONNECTION);
        map.put(Direction.WEST, WEST_CONNECTION);
        map.put(Direction.DOWN, DOWN_CONNECTION);
        map.put(Direction.UP, UP_CONNECTION);
        return map;
    });

    public static final Map<Direction, VoxelShape> DIR_SHAPES = (new ImmutableMap.Builder<Direction, VoxelShape>()
            .put(Direction.NORTH, Block.box(4, 4, 0, 12, 12, 5))
            .put(Direction.EAST, Block.box(11, 4, 4, 16, 12, 12))
            .put(Direction.SOUTH, Block.box(4, 4, 11, 12, 12, 16))
            .put(Direction.WEST, Block.box(0, 4, 4, 5, 12, 12))
            .put(Direction.UP, Block.box(4, 11, 4, 12, 16, 12))
            .put(Direction.DOWN, Block.box(4, 0, 4, 12, 5, 12))
    ).build();

    public AbstractPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.forceSolidOn());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH_CONNECTION, PipeConnectionType.NONE)
                .setValue(EAST_CONNECTION, PipeConnectionType.NONE)
                .setValue(SOUTH_CONNECTION, PipeConnectionType.NONE)
                .setValue(WEST_CONNECTION, PipeConnectionType.NONE)
                .setValue(UP_CONNECTION, PipeConnectionType.NONE)
                .setValue(DOWN_CONNECTION, PipeConnectionType.NONE)
                .setValue(WATERLOGGED, false)
        );

        for (BlockState state : this.getStateDefinition().getPossibleStates())
        {
            shapes.put(state, getShapeForState(state));
        }
    }

    public static EnumProperty<PipeConnectionType> getConnection(Direction direction)
    {
        return DIR_TO_CONNECTION.get(direction);
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
            if (state.getValue(DIR_TO_CONNECTION.get(direction)) == PipeConnectionType.SIDE)
            {
                shape = Shapes.or(shape, DIR_SHAPES.get(direction));
            }
        }
        return shape;
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
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        return this.getConnectedState(world, this.defaultBlockState(), pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

        // Already connected in direction
        if (connection && (state.getValue(DIR_TO_CONNECTION.get(direction)).isConnected()))
        {
            return state.setValue(DIR_TO_CONNECTION.get(direction), PipeConnectionType.SIDE);
        }

        return state.setValue(DIR_TO_CONNECTION.get(direction), PipeConnectionType.NONE);
    }

    public abstract boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos);

    protected BlockState getConnectedState(BlockGetter world, BlockState state, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            PipeConnectionType property = state.getValue(DIR_TO_CONNECTION.get(direction));
            if (property == PipeConnectionType.SIDE)
                continue;
            BlockPos adjPos = pos.relative(direction);
            BlockState adjState = world.getBlockState(adjPos);
            state = state.setValue(DIR_TO_CONNECTION.get(direction), canConnectTo(adjState, direction.getOpposite(), (Level) world, adjPos) ? PipeConnectionType.SIDE : PipeConnectionType.NONE);
        }
        return state.setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
    }

    protected boolean handCanChangeConnections(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        return hand != InteractionHand.OFF_HAND
                && (stack.isEmpty()
                || stack.is(NMTags.WRENCH)
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!handCanChangeConnections(world, pos, state, player, hand) || !MiscUtil.canPlayerModify(player, pos))
            return InteractionResult.PASS;

        if (!world.isClientSide)
        {
            Direction direction = hit.getDirection();
            if (player.isShiftKeyDown())
            {
                direction = direction.getOpposite();
            }

            Vec3 hitPos = hit.getLocation();

            Direction changeDirection = getUseDirection(direction, pos, hitPos);
            boolean connected = state.getValue(DIR_TO_CONNECTION.get(changeDirection)) == PipeConnectionType.SIDE;
            BlockState newState = state.setValue(DIR_TO_CONNECTION.get(changeDirection), connected ? PipeConnectionType.FORCED : PipeConnectionType.SIDE);
            world.setBlockAndUpdate(pos, newState);
            world.playSound(null, pos, connectionChangeSound(world, state, pos), SoundSource.BLOCKS, 1, 1);
            onConnectionUpdate(world, state, newState, pos, player);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public static Direction getUseDirection(Direction direction, BlockPos pos, Vec3 hitPos)
    {
        NMVec2f relative = NMMaths.removeAxis(direction.getAxis(), hitPos.subtract(pos.getX(), pos.getY(), pos.getZ()));

        Direction changeDirection = direction;
        if (!relative.isWithin(0.5f, 0.5f, 0.25f))
        {
            // X axis case
            if (relative.getY() > 0.75)
                changeDirection = Direction.SOUTH;
            if (relative.getY() < 0.25)
                changeDirection = Direction.NORTH;
            if (relative.getX() < 0.25)
                changeDirection = Direction.DOWN;
            if (relative.getX() > 0.75)
                changeDirection = Direction.UP;

            switch (direction.getAxis())
            {
                case Y -> changeDirection = changeDirection.getClockWise(Direction.Axis.Z);
                case Z -> changeDirection = NMMaths.swapDirections(changeDirection.getClockWise(Direction.Axis.Y));
            }
        }

        return changeDirection;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation)
    {
        return switch (rotation)
        {
            case NONE -> state;
            case CLOCKWISE_90 -> rotate(state, 1);
            case CLOCKWISE_180 -> rotate(state, 2);
            case COUNTERCLOCKWISE_90 -> rotate(state, 3);
        };
    }

    private static final Direction[] HORIZONTAL = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private static BlockState rotate(BlockState oldState, int offset)
    {
        BlockState newState = oldState;

        for (int i = 0; i < HORIZONTAL.length; ++i)
        {
            Direction newDirection = HORIZONTAL[(i + offset) % HORIZONTAL.length];
            newState = newState.setValue(getConnection(newDirection), oldState.getValue(getConnection(HORIZONTAL[i])));
        }

        return newState;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror)
    {
        return switch (mirror)
        {
            case NONE -> state;
            case LEFT_RIGHT -> mirrorZ(state);
            case FRONT_BACK -> mirrorX(state);
        };
    }

    private static BlockState mirrorX(BlockState state)
    {
        PipeConnectionType oldEast = state.getValue(getConnection(Direction.EAST));

        state = state
                .setValue(getConnection(Direction.EAST), state.getValue(getConnection(Direction.WEST)))
                .setValue(getConnection(Direction.WEST), oldEast);

        return state;
    }

    private static BlockState mirrorZ(BlockState state)
    {
        PipeConnectionType oldNorth = state.getValue(getConnection(Direction.NORTH));

        state = state
                .setValue(getConnection(Direction.NORTH), state.getValue(getConnection(Direction.SOUTH)))
                .setValue(getConnection(Direction.SOUTH), oldNorth);

        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
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

    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {

    }

    public SoundEvent connectionChangeSound(Level world, BlockState state, BlockPos pos)
    {
        return MeatlibSounds.WRENCH_CLICK;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState state, Fluid fluid)
    {
        return false;
    }

    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }
}
