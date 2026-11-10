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

package com.neep.neepmeat.transport.block.item_transport.entity;

import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.screen_handler.AdvancedRouterScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AdvancedRouterBlockEntity extends RouterBlockEntity
{
    public static final int MAX_FILTERS = 2;

    private final List<FilterList> filters = Util.make(() ->
    {
        List<FilterList> filters = Arrays.asList(new FilterList[6]);
        filters.replaceAll(ignored -> new FilterList(MAX_FILTERS, this::setChanged));
        return filters;
    });

    public AdvancedRouterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Nullable
    public Direction getOutputDirection(ItemVariant item)
    {
        for (Direction direction : Direction.values())
        {
            FilterList filterList = filters.get(direction.ordinal());
            if (!filterList.isEmpty() && filterList.matches(item))
                return direction;
        }
        return null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        ListTag list = new ListTag();

        for (var filter : filters)
        {
            list.add(filter.writeNbt(new CompoundTag()));
        }

        nbt.put("filters", list);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        ListTag list = nbt.getList("filters", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            FilterList filterList = new FilterList(MAX_FILTERS, this::setChanged);
            filterList.readNbt(list.getCompound(i));
            filters.set(i, filterList);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new AdvancedRouterScreenHandler(syncId, inv, filters);
    }
}
