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

package com.neep.neepmeat.plc.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.PLCRedstoneInterfaceBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PLCRedstoneInterface extends BaseBlock implements EntityBlock, DataCable
{
    public PLCRedstoneInterface(RegistrationContext ctx, Properties settings)
    {
        super(ctx, NMItemSettings.block().plcInterrupt(), settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return PLCBlocks.REDSTONE_INTERFACE_ENTITY.create(pos, state);
    }

    @Override
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        if (world.getBlockEntity(pos) instanceof PLCRedstoneInterfaceBlockEntity be)
        {
            return be.getEmittedStrength();
        }
        return 0;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (world.getBlockEntity(pos) instanceof PLCRedstoneInterfaceBlockEntity be)
        {
            be.onScheduledTick();
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof PLCRedstoneInterfaceBlockEntity be)
        {
            be.updateReceived(world.getBestNeighborSignal(pos));
        }
    }
}
