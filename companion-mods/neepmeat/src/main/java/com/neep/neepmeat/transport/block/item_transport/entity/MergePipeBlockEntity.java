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

import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MergePipeBlockEntity extends ItemPipeBlockEntity
{
    public MergePipeBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.MERGE_ITEM_PIPE, pos, state);
    }

    public MergePipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, Vertex::new);
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
        public boolean canItemLeave(ResourceAmount<ItemVariant> resource, Direction face, boolean routed)
        {
            return super.canItemLeave(resource, face, routed);
        }
    }
}

