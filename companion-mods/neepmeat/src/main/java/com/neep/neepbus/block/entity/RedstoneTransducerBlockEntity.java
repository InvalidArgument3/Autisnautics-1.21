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

package com.neep.neepbus.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.RedstoneTransducerBlock;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneTransducerBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

    private final WritePort writePort = this::receive;
    private final DirectReadPort readPort = new DirectReadPort(new SimpleEntry("Redstone Read"), () -> DataVariant.of(this.inputLevel), sender::send);

    private int outputLevel;
    private int inputLevel;

    public long lastRedstoneUpdate;

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(new SimpleEntry("Redstone Write"), writePort)
            .output(readPort.entry(), readPort)
            .applyChanges(this)
            .build();

    public RedstoneTransducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive(DataVariant data)
    {
        this.outputLevel = Math.round(Mth.clamp(data.floatValue(), 0, 15));

        long time = getLevel().getGameTime();

        Block block = getBlockState().getBlock();

        int remainder = (int) (time % 2);
        if (!getLevel().getBlockTicks().hasScheduledTick(worldPosition, block))
        {
            if (remainder == 0)
            {
                ((RedstoneTransducerBlock) getBlockState().getBlock()).updateRedstone(getLevel(), worldPosition, getBlockState(), this);
            }
            else
            {
                getLevel().scheduleTick(worldPosition, block, 2 - remainder);
            }
        }
    }

    public int getValue()
    {
        return outputLevel;
    }

    public void updateInput(int level)
    {
        if (this.inputLevel != level)
        {
            this.inputLevel = level;
            readPort.send();
        }
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        config.writeNbt(nbt);
        nbt.putInt("output_level", outputLevel);
        nbt.putInt("input_level", inputLevel);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        config.readNbt(nbt);
        this.outputLevel = nbt.getInt("output_level");
        this.inputLevel = nbt.getInt("input_level");
    }

    public void invalidateSender()
    {
        sender.invalidate();
    }
}
