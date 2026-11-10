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

package com.neep.neepmeat.transport.block.item_transport.machine;
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
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.PipeConnections;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.AdvancedEjectorBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.screen.AdvancedEjectorScreenHandler;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AdvancedEjectorBlock extends ItemPipeBlock implements Wrenchable
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public AdvancedEjectorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        if (direction == state.getValue(FACING))
            return false;

        return super.canConnect(world, pos, state, direction);
    }

    @Override
    public PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        var set = super.getConnections(state, forbidden);
        return set.with(state.getValue(FACING));
    }

    @Override
    protected boolean canConnectToApi(Level world, BlockPos pos, BlockState state, BlockState hereState, Direction direction)
    {
        if (direction == hereState.getValue(FACING))
            return false;

        return super.canConnectToApi(world, pos, state, hereState, direction);
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection)
    {
        if (toFace.getOpposite() == hereState.getValue(FACING))
            return false;

        return super.canConnectTo(hereState, toState, toFace, world, toPos, newConnection);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, fromPos, notify);
        if (world.getBlockEntity(pos) instanceof AdvancedEjectorBlockEntity be)
        {
            be.updateRedstone(world.hasNeighborSignal(pos));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Direction facing = ctx.getClickedFace().getOpposite();

        BlockPos offset = ctx.getClickedPos().relative(facing);

        BlockState state = defaultBlockState();

        var storage = ItemStorage.SIDED.find(ctx.getLevel(), offset, facing.getOpposite());

        // Facing direction needs to be applied before getConnectedState as some of the methods called by it require the facing property to be set
        if (storage != null)
        {
            state = state.setValue(FACING, facing);
        }
        else
        {
            state = state.setValue(FACING, (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown())
                    ? ctx.getClickedFace() : ctx.getClickedFace().getOpposite());
        }

        state = getConnectedState(ctx.getLevel(), state, ctx.getClickedPos(),
                ctx.getClickedFace().getOpposite(), true);

        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof AdvancedEjectorBlockEntity be)
        {
            MiscUtil.openHandledScreen(player, ((syncId, playerInventory, player1) ->
                    new AdvancedEjectorScreenHandler(be.getFilters(), be, playerInventory, syncId)));

            return InteractionResult.SUCCESS;
        }

        if (hand == InteractionHand.OFF_HAND || !player.getItemInHand(hand).isEmpty())
            return InteractionResult.PASS;

        if (!world.isClientSide)
        {
            Direction direction = hit.getDirection();

            Vec3 hitPos = hit.getLocation();

            Direction changeDirection = getUseDirection(direction, pos, hitPos);

            // Do not allow connecting on this side
            if (changeDirection == state.getValue(FACING))
                return InteractionResult.PASS;

            boolean connected = getConnection(state, changeDirection) == ConnectionType.SIDE;
            BlockState newState = state.setValue(DIR_TO_CONNECTION.get(changeDirection), connected ? ConnectionType.NONE : ConnectionType.SIDE);
            world.setBlockAndUpdate(pos, newState);
            world.playSound(null, pos, connectionChangeSound(world, state, pos), SoundSource.BLOCKS, 1, 1);
            onConnectionUpdate(world, state, newState, pos, player);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ADVANCED_EJECTOR.create(pos, state);
    }

    @Override
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

        Direction facing = state.getValue(FACING);

        return Shapes.or(shape, createFacingShape(facing));
    }

    private VoxelShape createFacingShape(Direction facing)
    {
        int gap = 0;
        int thickness = 7;
        return switch (facing)
        {
            case NORTH -> Block.box(gap, gap, 0, 16 - gap, 16 - gap, thickness);
            case EAST -> Block.box(16 - thickness, 0, gap, 16, 16 - gap, 16 - gap);
            case SOUTH -> Block.box(gap, gap, 16 - thickness, 16 - gap, 16 - gap, 16);
            case WEST -> Block.box(0, gap, gap, thickness, 16 - gap, 16 - gap);
            case UP -> Block.box(gap, 16 - thickness, gap, 16 - gap, 16, 16 - gap);
            case DOWN -> Block.box(gap, 0, gap, 16 - gap, thickness, 16 - gap);
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ADVANCED_EJECTOR, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(world1, pos, state1), null, world);
    }
}
