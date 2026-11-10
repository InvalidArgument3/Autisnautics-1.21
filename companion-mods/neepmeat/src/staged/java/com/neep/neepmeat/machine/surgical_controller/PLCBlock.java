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

package com.neep.neepmeat.machine.surgical_controller;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.ComputerBinder;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class PLCBlock extends BaseHorFacingBlock implements EntityBlock, DataCable, NeepBusProvider, ComputerBinder
{
    public PLCBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof PLCBlockEntity be && !player.isHolding(PLCBlocks.PROGRAM_CARD))
        {
            if (player.isShiftKeyDown())
            {
                be.resetRobot();
            }
            else
            {
                be.enter(player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof PLCBlockEntity be)
        {
            stacks.forEach(be::saveToItem);

            return stacks;
        }
        return stacks;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
    {
        if (world.getBlockEntity(pos) instanceof PLCBlockEntity be)
        {
            if (!world.isClientSide() && player.isCreative() && !be.getProgramEditor().getSource().isEmpty())
                MiscUtil.dropBlockWithNbt(world, this, pos, be);
        }

        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, PLCBlocks.PLC_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(), null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return PLCBlocks.PLC_ENTITY.create(pos, state);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof PLCBlockEntity be)
        {
            be.getSender().invalidate();
        }
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        return false;
    }
}
