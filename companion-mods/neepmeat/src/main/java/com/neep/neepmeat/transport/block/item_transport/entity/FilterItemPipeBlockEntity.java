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

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FilterItemPipeBlockEntity extends ItemPipeBlockEntity
{
    protected FilterList filters = new FilterList(3, this::setChanged);

    public FilterItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, Vertex::new);
    }

    @Override
    public boolean canItemEnter(ItemVariant item)
    {
        return filters.matches(item);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        filters.readNbt(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        filters.writeNbt(tag);
    }

    public FilterList getFilters()
    {
        return filters;
    }

    private static class Vertex extends ItemPipeVertexImpl
    {
        public Vertex(BlockEntity parent)
        {
            super(parent);
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }

        @Override
        public boolean canItemEnter(ItemVariant variant, long amount, Direction inFace, boolean routed)
        {
            return ((FilterItemPipeBlockEntity) parent).filters.matches(variant);
        }
    }
}
