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

package com.neep.neepbus.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.block.entity.RedstoneTransducerBlockEntity;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.api.pipe.SimplePipeBlock;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.Wrenchable;
import org.jetbrains.annotations.Nullable;

public class RedstoneTransducerBlock extends SimplePipeBlock implements EntityBlock, NeepBusProvider, DataCable, Wrenchable
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public RedstoneTransducerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState superState = super.getStateForPlacement(ctx);
        if (superState == null || ctx.getPlayer() == null)
            return null;

        return superState.setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getNearestLookingDirection().getOpposite() : ctx.getNearestLookingDirection());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof ConfigProvider provider)
        {
            player.openMenu(NeepBusConfigScreenHandler.getFactory(provider.getConfig()));
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
    {
        if (toState.getBlock() instanceof DataCable cable)
        {
            return cable.connectInDirection(world, toPos, toState, toFace);
        }
        return false;
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return true;
    }

    @Override
    public VoxelShape getShapeForState(BlockState state)
    {
        VoxelShape shape = getCentreShape();
        for (Direction direction : Direction.values())
        {
            if (state.getValue(DIR_TO_CONNECTION.get(direction)) == ConnectionType.SIDE || state.getValue(FACING) == direction)
            {
                shape = Shapes.or(shape, DIR_SHAPES.get(direction));
            }
        }
        return shape;
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        return state.getValue(DIR_TO_CONNECTION.get(direction)).isConnected();
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof RedstoneTransducerBlockEntity be)
        {
            int power = world.getBestNeighborSignal(pos);
            be.updateInput(power);
        }
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
    }

    private int getRedstonePower(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        // Not sure why it has to be opposite. I would expect direction to be relative to this block, not the redstone wire.
        if (direction != state.getValue(FACING).getOpposite())
            return 0;

        if (world.getBlockEntity(pos) instanceof RedstoneTransducerBlockEntity be)
        {
            return be.getValue();
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        return getRedstonePower(state, world, pos, direction);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        return getRedstonePower(state, world, pos, direction);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof RedstoneTransducerBlockEntity be)
        {
            be.invalidateSender();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (world.getBlockEntity(pos) instanceof RedstoneTransducerBlockEntity be)
        {
            updateRedstone(world, pos, state, be);
        }
    }

    public void updateRedstone(Level world, BlockPos pos, BlockState state, RedstoneTransducerBlockEntity be)
    {
        // Avoid multiple block updates in the same tick
        if (be.lastRedstoneUpdate < world.getGameTime())
        {
            be.lastRedstoneUpdate = world.getGameTime();
            updateNeighbors(world, pos, state);
        }
    }

    protected void updateNeighbors(Level world, BlockPos pos, BlockState state)
    {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction);
        world.neighborChanged(blockPos, this, pos);
        world.updateNeighborsAtExceptFromFacing(blockPos, this, direction.getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.REDSTONE_TRANSDUCER_BE.create(pos, state);
    }
}
