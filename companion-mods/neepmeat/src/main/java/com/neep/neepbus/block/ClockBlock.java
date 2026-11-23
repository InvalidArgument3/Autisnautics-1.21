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

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.ClockBlockEntity;
import com.neep.neepbus.block.entity.IntegratorBlockEntity;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class ClockBlock extends BaseBlock implements NeepBusProvider, DataCable, EntityBlock
{
    public ClockBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NeepBus.CLOCK_BE, (world1, pos, state1, blockEntity) -> ((ClockBlockEntity) blockEntity).serverTick(), null, world);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof ClockBlockEntity be)
        {
            player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof ClockBlockEntity be)
        {
            be.invalidate();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.CLOCK_BE.create(pos, state);
    }
}
