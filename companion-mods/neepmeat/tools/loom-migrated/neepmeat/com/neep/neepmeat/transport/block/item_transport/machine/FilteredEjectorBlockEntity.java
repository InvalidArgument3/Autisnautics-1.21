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

package com.neep.neepmeat.transport.block.item_transport.machine;

import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FilteredEjectorBlockEntity extends EjectorBlockEntity implements MenuProvider
{
    private final FilterList filterList = new FilterList(4, this::setChanged);

    public FilteredEjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        filterList.writeNbt(tag);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        filterList.readNbt(nbt);
        super.load(nbt);
    }

    public void serverTick()
    {
        super.serverTick();
    }

    protected void tryTransfer()
    {
        Storage<ItemVariant> storage;
        if ((storage = extractionCache.find()) != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ResourceAmount<ItemVariant> extractable = StorageUtil.findExtractableContent(storage, filterList::matches, transaction);

                if (extractable == null)
                {
                    transaction.abort();
                    return;
                }

                long extracted = storage.extract(extractable.resource(), 1, transaction);

                if (extracted >= 1)
                {
                    succeed();
                    stored = new ResourceAmount<>(extractable.resource(), extracted);
                    transaction.commit();
                    return;
                }
                transaction.abort();
            }
        }
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new FilterScreenHandler(filterList, playerInventory, syncId);
    }
}
