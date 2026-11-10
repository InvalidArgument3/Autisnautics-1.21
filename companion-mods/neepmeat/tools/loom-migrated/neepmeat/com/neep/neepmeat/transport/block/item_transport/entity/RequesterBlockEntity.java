/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.block.item_transport.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.RequesterBlock;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.neepmeat.transport.item_network.RoutingNetworkCache;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RequesterBlockEntity extends SyncableBlockEntity implements ConfigProvider, NetworkTrackingItemPipe
{
    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("Request"), this::receive)
            // TODO: set variant
            .applyChanges(this)
            .build();

    private final Vertex vertex = new Vertex(this);

    private int amount = 1;
    private RoutingNetwork.RequestType requestType = RoutingNetwork.RequestType.EXACT_AMOUNT;

    private final FilterList filterList = new FilterList(1, this::setChanged);
    private final RoutingNetworkCache networkCache = new RoutingNetworkCache();

    public boolean prevHigh;

    public RequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive(DataVariant dataVariant)
    {
        int requested = (int) dataVariant.floatValue();

        request(requested);
    }

    public void request()
    {
        request(amount);
    }

    public void request(int requested)
    {
        var network = networkCache.getNetwork();
        if (network != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                if (network.request(filterList::matches, requested,
                        worldPosition, getBlockState().getValue(RequesterBlock.FACING),
                        RoutingNetwork.RequestType.EXACT_AMOUNT,
                        new RoutingNetwork.RequestContext(), transaction))
                {
                    // Do something successful
                    transaction.commit();
                }
            }
        }
    }

    // Looks around the block for an item pipe
    @Nullable
    private Pair<BlockPos, Direction> findRequestPos(Direction exclude)
    {
        BlockPos.MutableBlockPos mutable = worldPosition.mutable();
        for (var face : Direction.Plane.HORIZONTAL)
        {
            if (face == exclude)
                continue;

            mutable.setWithOffset(worldPosition, face);
            var pipe = ItemPipe.findItemPipe(level, mutable, level.getBlockState(mutable));
            if (pipe != null)
            {
                return Pair.of(mutable, face.getOpposite());
            }
        }

        return null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("filter", filterList.writeNbt(new CompoundTag()));
        nbt.put("config", config.writeNbt(new CompoundTag()));
        nbt.put("vertex", vertex.writeNbt(new CompoundTag()));
        nbt.putInt("amount", amount);
        nbt.putBoolean("prev_high", prevHigh);
        nbt.putInt("request_type", requestType.ordinal());
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.filterList.readNbt(nbt.getCompound("filter"));
        this.config.readNbt(nbt.getCompound("config"));
        this.vertex.readNbt(nbt.getCompound("vertex"));
        this.amount = nbt.getInt("amount");
        this.prevHigh = nbt.getBoolean("prev_high");
        this.requestType = RoutingNetwork.RequestType.values()[nbt.getInt("request_type")];
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public FilterList getFilter()
    {
        return filterList;
    }

    public boolean matches(ItemVariant variant, long amount)
    {
        return filterList.matches(variant);
    }

    public ItemPipeVertex getVertex()
    {
        return vertex;
    }

    @Override
    public RoutingNetworkCache getCache()
    {
        return networkCache;
    }

    public void setPrevHigh(boolean high)
    {
        prevHigh = high;
        setChanged();
    }

    public int getAmount()
    {
        return amount;
    }

    public RoutingNetwork.RequestType getRequestType()
    {
        return requestType;
    }

    public void setValues(int amount, RoutingNetwork.RequestType type)
    {
        this.amount = amount;
        this.requestType = type;
        setChanged();
    }

    private class Vertex extends ItemPipeVertexImpl
    {
        public Vertex(BlockEntity parent)
        {
            super(parent);
        }

        @Override
        public boolean canItemEnter(ItemVariant variant, long amount, Direction inFace, boolean routed)
        {
            return super.canItemEnter(variant, amount, inFace, routed);
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}
