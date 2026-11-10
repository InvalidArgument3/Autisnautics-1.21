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

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlock;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemPumpBlock extends EjectorBlock
{
    public ItemPumpBlock(RegistrationContext ctx, ItemSettings itemSettings, FabricBlockSettings settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor(InventoryDetectorBlock::never));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof ItemPumpBlockEntity be && !world.isClientSide)
        {
            be.invalidate();
            be.updatePowered(world.hasNeighborSignal(pos));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof ItemPumpBlockEntity be)
            MeatlibStorageUtil.scatterAmount(world, pos, be.stored);

        if (world instanceof ServerLevel serverWorld)
        {

        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        if (world.getBlockEntity(pos) instanceof ItemPumpBlockEntity be)
        {
            Direction facing = be.getBlockState().getValue(ItemPumpBlock.FACING);
            if (facing == direction.getOpposite())
            {
                return ItemPipeUtil.EjectOperation.of(world, pos, facing)
                        .useRouting(false)
                        .pipeToAny(item, transaction)
                        .amount();
            }
        }
        return 0;
    }

    @Override
    public boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return direction.equals(state.getValue(FACING)) || direction.equals(state.getValue(FACING).getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ITEM_PUMP.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ITEM_PUMP, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }
}
