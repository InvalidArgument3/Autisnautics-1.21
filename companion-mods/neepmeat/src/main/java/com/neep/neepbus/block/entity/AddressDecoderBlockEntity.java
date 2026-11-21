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
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.include.com.google.common.primitives.Ints;

public class AddressDecoderBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    protected final int maxOutputs;

    protected final CachingSender readSender = new CachingSender(this::getLevel, getBlockPos());
    protected final MultiCachingSender writeSender = new MultiCachingSender(this::getLevel, getBlockPos(), this::validAddress);

    private final ConfigEntry lookupEntry = new SimpleEntry("Address");
    private final WritePort lookupWritePort = this::receiveLookup;
    private final WritePort dataWritePort = this::receiveData;

    // Use the MultiCachingSender here so that all valid ports are cached in one sweep
    private final SimpleReadPort dataOutPort = new SimpleReadPort(new SimpleEntry("Data out"), writeSender::send);
    private final DirectReadPort dataReadPort = new DirectReadPort(new SimpleEntry("Data read"), this::readViaLookup, readSender::send);

    protected final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .applyChanges(this)
            .input(lookupEntry, lookupWritePort)
            .input(new SimpleEntry("Data"), dataWritePort)
            .output(dataOutPort.entry(), dataOutPort)
            .output(dataReadPort.entry(), dataReadPort)
            .build();

    private int addressInt;

    public AddressDecoderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.maxOutputs = 64;
    }

    protected boolean validAddress(String address)
    {
        Integer integer = Ints.tryParse(address);
        return integer != null && integer >= 0 && integer < maxOutputs;
    }

    private void receiveLookup(DataVariant addressInt)
    {
        this.addressInt = addressInt.intValue();
        dataOutPort.entry().setAddress(String.valueOf(this.addressInt));
        dataReadPort.send();
    }

    private DataVariant readViaLookup()
    {
        return readSender.read(dataOutPort.entry().getAddress());
    }

    private void receiveData(DataVariant data)
    {
        if (addressInt >= 0 && addressInt < maxOutputs)
        {
            dataOutPort.entry().setAddress(String.valueOf(addressInt));
            dataOutPort.send(data);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        config.writeNbt(nbt);
        dataOutPort.writeNbt(nbt);
        nbt.putInt("address_int", addressInt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        config.readNbt(nbt);
        dataOutPort.writeNbt(nbt);
        this.addressInt = nbt.getInt("address_int");
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void invalidate()
    {
        readSender.invalidate();;
    }
}
