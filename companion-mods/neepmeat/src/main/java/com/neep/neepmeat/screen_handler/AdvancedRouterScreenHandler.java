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

package com.neep.neepmeat.screen_handler;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class AdvancedRouterScreenHandler extends BasicScreenHandler
{
    private final List<FilterList> filters;
    private List<Boolean> notEmpty = new BooleanArrayList(6);

    public final ChannelManager<OpenFilter> openFilterC2S;
    public final ChannelManager<UpdateFilters> updateFiltersS2C;

    // Client
    public AdvancedRouterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, NonNullList.withSize(6, new FilterList(0, () -> {})));

        updateFiltersS2C.receiver(this::onUpdateFilters);
    }

    // Server
    public AdvancedRouterScreenHandler(int syncId, Inventory playerInventory, List<FilterList> filters)
    {
        super(NMScreenHandlers.ADVANCED_ROUTER, playerInventory, null, syncId, null);
        this.filters = filters;

        openFilterC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "open_filter"),
                ChannelFormat.builder(OpenFilter.class).param(ParamCodec.INT).build(),
                playerInventory.player);

        updateFiltersS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_filters"),
                ChannelFormat.builder(UpdateFilters.class).param(ParamCodec.list(ParamCodec.BOOLEAN)).build(),
                playerInventory.player);

        openFilterC2S.receiver(this::onOpenFilter);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        updateFiltersS2C.emitter().apply(filters.stream().map(f -> !f.isEmpty()).toList());
    }

    private void onOpenFilter(int index)
    {
        getPlayer().openMenu(new FilterScreenHandler.Factory(filters.get(index)));
    }

    private void onUpdateFilters(List<Boolean> list)
    {
        notEmpty = list;
    }

    public Player getPlayer()
    {
        return playerInventory.player;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        openFilterC2S.close();
        updateFiltersS2C.close();
    }

    public boolean notEmpty(Direction direction)
    {
        int index = direction.ordinal();

        // Check just in case
        return index < notEmpty.size() && notEmpty.get(direction.ordinal());
    }

    public interface OpenFilter
    {
        void apply(int index);
    }

    public interface UpdateFilters
    {
        void apply(List<Boolean> notEmpty);
    }
}

