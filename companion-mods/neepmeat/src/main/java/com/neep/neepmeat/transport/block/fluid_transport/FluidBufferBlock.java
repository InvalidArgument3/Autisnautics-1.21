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

package com.neep.neepmeat.transport.block.fluid_transport;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.machine.fluid_rationer.FluidRationerBlock;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidBufferBlockEntity;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("deprecation")
public class FluidBufferBlock extends BaseFacingBlock implements EntityBlock
{
    public static final BooleanProperty NORTH_CONNECTION = BooleanProperty.create("north");
    public static final BooleanProperty EAST_CONNECTION = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH_CONNECTION = BooleanProperty.create("south");
    public static final BooleanProperty WEST_CONNECTION = BooleanProperty.create("west");
    public static final BooleanProperty UP_CONNECTION = BooleanProperty.create("up");
    public static final BooleanProperty DOWN_CONNECTION = BooleanProperty.create("down");

    public static final Map<Direction, BooleanProperty> DIR_TO_CONNECTION = (new ImmutableMap.Builder<Direction, BooleanProperty>()
            .put(Direction.NORTH, NORTH_CONNECTION)
            .put(Direction.EAST, EAST_CONNECTION)
            .put(Direction.SOUTH, SOUTH_CONNECTION)
            .put(Direction.WEST, WEST_CONNECTION)
            .put(Direction.UP, UP_CONNECTION)
            .put(Direction.DOWN, DOWN_CONNECTION)
    ).build();

    public static final VoxelShape X_SHAPE = Block.box(0, 3, 3, 16, 13, 13);
    public static final VoxelShape Y_SHAPE = Block.box(3, 0, 3, 13, 16, 13);
    public static final VoxelShape Z_SHAPE = Block.box(3, 3, 0, 13, 13, 16);

    public FluidBufferBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FluidBufferBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        Direction facing = state.getValue(FACING);
        return switch (facing.getAxis())
        {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        Direction facing = state.getValue(FACING);
        return switch (facing.getAxis())
                {
                    case X -> X_SHAPE;
                    case Y -> Y_SHAPE;
                    case Z -> Z_SHAPE;
                };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (world.getBlockEntity(pos) instanceof FluidBufferBlockEntity tank && tank.onUse(player, hand))
        {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (!world.isClientSide())
        {
            return getConnectedState(world, state, pos);
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState state = this.defaultBlockState().setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getClickedFace() : ctx.getClickedFace().getOpposite());
        return this.getConnectedState(ctx.getLevel(), state, ctx.getClickedPos());
    }

    protected BlockState getConnectedState(LevelAccessor world, BlockState state, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            BlockState pipeState = world.getBlockState(pos.relative(direction));
            if (pipeState.getBlock() instanceof FluidPipe pipe
                    && Iterables.contains(pipe.getConnections(pipeState), direction.getOpposite())
                    || pipeState.getBlock() instanceof PumpBlock && (pipeState.getValue(PumpBlock.FACING).getAxis() == direction.getAxis())
                    || pipeState.getBlock() instanceof FluidRationerBlock && (pipeState.getValue(FluidRationerBlock.FACING).getAxis() == direction.getAxis())
            )
            {
                state = state.setValue(DIR_TO_CONNECTION.get(direction), true);
            }
            else
            {
                state = state.setValue(DIR_TO_CONNECTION.get(direction), false);
            }
        }
        return state;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH_CONNECTION, EAST_CONNECTION, SOUTH_CONNECTION, WEST_CONNECTION, UP_CONNECTION, DOWN_CONNECTION);
    }
}
