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

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.PipeConnections;
import com.neep.meatlib.util.Wrenchable;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlock;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EjectorBlock extends BaseFacingBlock implements EntityBlock, ItemPipe, Wrenchable
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public EjectorBlock(RegistrationContext ctx, ItemSettings itemSettings, FabricBlockSettings settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor(InventoryDetectorBlock::never));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.EJECTOR.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.EJECTOR, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        boolean redstone = context.getLevel().hasNeighborSignal(context.getClickedPos());
        BlockState superState = super.getStateForPlacement(context);

        if (superState == null)
            return null;

        // Not inverted by default
        return superState.setValue(ACTIVE, redstone);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (world.getBlockEntity(pos) instanceof EjectorBlockEntity be && !world.isClientSide())
        {
            be.updatePowered(world.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        super.onPlace(state, world, pos, oldState, notify);
        if (!oldState.is(this) && world.getBlockEntity(pos) instanceof EjectorBlockEntity be && !world.isClientSide())
        {
            be.updatePowered(world.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof EjectorBlockEntity be)
            MeatlibStorageUtil.scatterAmount(world, pos, be.stored);

        if (world instanceof ServerLevel serverWorld)
        {

        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof EjectorBlockEntity be)
        {
            if (player.isShiftKeyDown())
            {
                if (!world.isClientSide())
                {
                    be.changeMode();
                    world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 1, 1);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return direction == state.getValue(FACING);
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return canConnect(blockView, pos, state, face);
    }

    @Override
    public PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        Direction facing = state.getValue(FACING);
        if (forbidden != facing && forbidden != facing.getOpposite())
            return PipeConnections.EMPTY.copy();
        else if (forbidden == facing)
            return new PipeConnections(facing.getOpposite());
        else
            return new PipeConnections(facing);
    }
}
