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

import com.google.common.collect.Iterators;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.screen_handler.RouterScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.machine.RouterInventory;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class RouterBlockEntity extends BlockEntity implements MenuProvider
{
    protected final ImplementedInventory inventory = new RouterInventory();
    public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
    private final StorageWrapper storageWrapper = new StorageWrapper();

    private final Vertex vertex = new Vertex(this);

    public RouterBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.ROUTER, pos, state);
    }

    public RouterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new RouterScreenHandler(syncId, inv, this.inventory);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        inventory.writeNbt(nbt);
        nbt.put("vertex", vertex.writeNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        inventory.readNbt(nbt);
        vertex.readNbt(nbt.getCompound("vertex"));
    }

    @Nullable
    public Direction getOutputDirection(ItemVariant item)
    {
        for (int i = 0; i < 6; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                int index = i * 3 + j;
                ItemStack filterStack = inventory.getItems().get(index);
//                System.out.println(filterStack);
                if (filterStack.is(item.getItem()))
                {
                    return DIRECTIONS[i];
                }
            }
        }
        return null;
    }

    public Storage<ItemVariant> getStorage(Direction direction)
    {
        return storageWrapper;
    }

    public ItemPipeVertex getVertex()
    {
        return vertex;
    }

    protected class StorageWrapper implements Storage<ItemVariant>, StorageView<ItemVariant>
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            @Nullable Direction output = getOutputDirection(resource);
            if (output != null && level instanceof ServerLevel serverWorld)
            {
                return ItemPipeUtil.EjectOperation.of(serverWorld, worldPosition, output)
                        .useRouting(true)
                        .stackToAny(resource, maxAmount, transaction)
                        .amount();
            }

            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public boolean isResourceBlank()
        {
            return true;
        }

        @Override
        public ItemVariant getResource()
        {
            return ItemVariant.BLANK;
        }

        @Override
        public long getAmount()
        {
            return 0;
        }

        @Override
        public long getCapacity()
        {
            return 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator()
        {
            return Iterators.singletonIterator(this);
        }
    }

    private class Vertex extends ItemPipeVertexImpl
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
            Direction output = getOutputDirection(variant);
            return inFace != output && output != null;
        }

        @Override
        public boolean canItemLeave(ResourceAmount<ItemVariant> resource, Direction face, boolean routed)
        {
            Direction output = getOutputDirection(resource.resource());
            return face == output;
        }
    }
}