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
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.screen.LimiterValveScreenHandler;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LimiterValveBlockEntity extends FluidPipeBlockEntity<LimiterValveBlockEntity.LimiterValveVertexFluid>
{
    protected int maxFlowRate = 81000;
    protected FluidUnit mode = FluidUnit.DROPLETS;
    // Old behaviour: 0 for droplets, 1 for mb.

    public LimiterValveBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.LIMITER_VALVE, pos, state, LimiterValveVertexFluid::new);
    }

    public LimiterValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PipeConstructor<LimiterValveVertexFluid> constructor)
    {
        super(type, pos, state, constructor);
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.maxFlowRate = nbt.getInt("maxFlowRate");
        this.mode = FluidUnit.values()[nbt.getInt("mbMode")];
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putInt("maxFlowRate", maxFlowRate);
        nbt.putInt("mbMode", mode.ordinal());
    }

    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata();
    }

    protected int getMaxFlowRate()
    {
//        return maxFlowRate * (mode == 0 ? 1 : 81);
        return (int) (maxFlowRate * switch (mode)
                {
                    case DROPLETS -> 1;
                    case MILLIBUCKETS -> 81;
                    case EJ_T -> 0.5;
                });
    }

    public int getMaxAmountInUnit()
    {
        return maxFlowRate;
    }

    public FluidUnit getMode()
    {
        return mode;
    }

    public void setValues(int maxAmount, FluidUnit mode)
    {
        this.maxFlowRate = maxAmount;
        this.mode = mode;
        setChanged();
    }

    public static class LimiterValveVertexFluid extends BlockFluidPipeVertex
    {
        public LimiterValveVertexFluid(FluidPipeBlockEntity<LimiterValveVertexFluid> fluidPipeBlockEntity)
        {
            super(fluidPipeBlockEntity);
        }

        @Override
        public long canInsert(ServerLevel world, int inDir, FluidVariant variant, long maxAmount)
        {
            long superAmount = super.canInsert(world, inDir, variant, maxAmount);
            return Math.min(superAmount, ((LimiterValveBlockEntity) parent).getMaxFlowRate());
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }

}
