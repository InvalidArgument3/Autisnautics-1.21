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
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class WindowPipeBlockEntity extends FluidPipeBlockEntity<WindowPipeBlockEntity.WindowFluidPipeVertex> implements BlockEntityClientSerializable
{
    public float clientFraction;
    public long clientAmount;
    public FluidVariant clientVariant = FluidVariant.blank();
    public FluidVariant lastClientVariant = FluidVariant.blank();

    public WindowPipeBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.WINDOW_PIPE, pos, state, WindowFluidPipeVertex::new);
    }

    public WindowPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PipeConstructor<WindowFluidPipeVertex> constructor)
    {
        super(type, pos, state, constructor);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        fromClientTag(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        toClientTag(nbt);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        this.clientAmount = nbt.getLong("amount");
        this.clientVariant = FluidVariant.fromNbt(nbt.getCompound("variant"));
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        nbt.putLong("amount", vertex.maxAmount);
        nbt.put("variant", vertex.maxVariant.toNbt());
    }

    public static class WindowFluidPipeVertex extends BlockFluidPipeVertex
    {
        public int maxAmount;
        public FluidVariant maxVariant = FluidVariant.blank();

        public WindowFluidPipeVertex(FluidPipeBlockEntity<WindowFluidPipeVertex> fluidPipeBlockEntity)
        {
            super(fluidPipeBlockEntity);
        }

        @Override
        public void tick()
        {
            if (maxAmount != amount || maxVariant != variant)
            {
                maxAmount = (int) amount;
                maxVariant = variant;
                ((BlockEntityClientSerializable) parent).sync();
            }
            super.tick();
        }

        @Override
        protected void onFinalCommit()
        {
            super.onFinalCommit();
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}
