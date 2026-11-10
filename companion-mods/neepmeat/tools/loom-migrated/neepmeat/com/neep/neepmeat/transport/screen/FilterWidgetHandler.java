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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.filter.Filter;
import com.neep.neepmeat.item.filter.FilterList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class FilterWidgetHandler implements WidgetHandler
{
    public final ChannelManager<UpdateToClient> updateToClient;
    public final ChannelManager<UpdateToServer> updateToServer;
    public final ChannelManager<AddFilter> addFilter;
    public final ChannelManager<RemoveFilter> removeFilter;
    public final ChannelManager<SetInverted> setInverted;

    private FilterList filter;

    public FilterWidgetHandler(Player player, FilterList filter)
    {
        updateToServer = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "filter/update_to_server"),
                ChannelFormat.builder(UpdateToServer.class).param(ParamCodec.INT).param(ParamCodec.NBT).build(),
                player
        );

        updateToClient = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "filter/update_to_client"),
                ChannelFormat.builder(UpdateToClient.class).param(FilterList.CODEC).build(),
                player
        );

        addFilter = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "filter/add_filter"),
                ChannelFormat.builder(AddFilter.class).param(ParamCodecs.IDENTIFIER).build(),
                player
        );

        removeFilter = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "filter/remove_filter"),
                ChannelFormat.builder(RemoveFilter.class).param(ParamCodec.INT).build(),
                player
        );

        setInverted = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "set_inverted"),
                ChannelFormat.builder(SetInverted.class).param(ParamCodec.INT).param(ParamCodec.BOOLEAN).build(),
                player
        );

        this.filter = filter;

        updateToServer.receiver(this::updateToServer);
        addFilter.receiver(this::addFilter);
        removeFilter.receiver(this::removeFilter);
        setInverted.receiver(this::onSetInverted);
    }

    public void updateToServer(int index, CompoundTag nbt)
    {
        if (index >= 0 && index < filter.size()) // Don't trust client
        {
            filter.getEntries().get(index).update(nbt);
            filter.markDirty();
        }
    }

    public FilterList getFilters()
    {
        return filter;
    }

    private void removeFilter(int index)
    {
        if (index >= 0 && index < filter.size()) // Don't trust client
        {
            filter.remove(index);
            filter.markDirty();
        }
    }

    public void addFilter(Filter.Constructor<?> filter)
    {
        ResourceLocation id = Filter.REGISTRY.getKey(filter);
        addFilter.emitter().apply(id);
    }

    private void addFilter(ResourceLocation id)
    {
        Filter.Constructor<?> constructor = Filter.REGISTRY.get(id);
        if (constructor != null) // Don't trust client
        {
            Filter filter = constructor.create();
            this.filter.add(filter);
            this.filter.markDirty();
        }
    }

    public void setInverted(int index, boolean inverted)
    {
        setInverted.emitter().apply(index, inverted);
    }

    private void onSetInverted(int index, boolean inverted)
    {
        filter.getEntries().get(index).setInverted(inverted);
    }

    public void sendUpdates()
    {
        updateToClient.emitter().apply(filter);
    }

    public void updateOnClient(FilterList filter)
    {
        this.filter = filter;
    }

    @Override
    public void close()
    {
        updateToClient.close();
        updateToServer.close();
        addFilter.close();
        removeFilter.close();
        setInverted.close();
    }

    public interface UpdateToClient
    {
        void apply(FilterList filterList);
    }

    public interface UpdateToServer
    {
        void apply(int index, CompoundTag nbt);
    }

    public interface AddFilter
    {
        void apply(ResourceLocation id);
    }

    public interface RemoveFilter
    {
        void apply(int index);
    }

    public interface SetInverted
    {
        void apply(int index, boolean inverted);
    }
}
