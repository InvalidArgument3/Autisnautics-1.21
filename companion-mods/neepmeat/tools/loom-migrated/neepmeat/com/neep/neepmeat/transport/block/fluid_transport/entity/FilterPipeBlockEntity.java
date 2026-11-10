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

package com.neep.neepmeat.transport.block.fluid_transport.entity;

import com.neep.meatlib.blockentity.BlockEntityClientSerializable;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.fluid_transport.FilterPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FilterPipeBlockEntity extends FluidPipeBlockEntity<FilterPipeBlock.FilterFluidPipeVertex> implements BlockEntityClientSerializable
{
    protected FluidVariant filterVariant = FluidVariant.blank();

    public FilterPipeBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FILTER_PIPE, pos, state, FilterPipeBlock.FilterFluidPipeVertex::new);
    }

    public FilterPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PipeConstructor<FilterPipeBlock.FilterFluidPipeVertex> constructor)
    {
        super(type, pos, state, constructor);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.filterVariant = FluidVariant.fromNbt(nbt.getCompound("filterVariant"));
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("filterVariant", filterVariant.toNbt());
    }

    public FluidVariant getFilterVariant()
    {
        return filterVariant;
    }

    public void setFilterFluid(FluidVariant variant)
    {
        filterVariant = variant;
        sync();
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        load(nbt);
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        saveAdditional(nbt);
    }

    @Override
    public void onReceiveNbt(CompoundTag nbt)
    {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
    }
}
