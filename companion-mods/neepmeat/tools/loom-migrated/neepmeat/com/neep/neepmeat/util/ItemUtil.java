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

package com.neep.neepmeat.util;

import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@SuppressWarnings("UnstableApiUsage")
public class ItemUtil
{
    public static boolean containsResource(List<StorageView<ItemVariant>> list, ItemVariant stack)
    {
        for (StorageView<ItemVariant> patternStack : list)
        {
            if (patternStack.getResource().equals(stack))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(List<StorageView<ItemVariant>> list, StorageView<ItemVariant> observedView, FilterUtils.Filter filter)
    {
        for (StorageView<ItemVariant> patternStack : list)
        {
            if (patternStack.getResource().equals(observedView.getResource())
                    && filter.test(observedView.getAmount(), patternStack.getAmount()))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean notBlank(StorageView<ItemVariant> view)
    {
        return !view.isResourceBlank();
    }

    public static boolean singleVariantInteract(Player player, InteractionHand hand, SingleVariantStorage<ItemVariant> storage)
    {
        ItemStack stack = player.getItemInHand(hand);
        try (Transaction transaction = Transaction.openOuter())
        {
            if ((storage.isResourceBlank() || storage.getResource().matches(stack)) && !stack.isEmpty())
            {
                long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                stack.shrink((int) inserted);
                transaction.commit();
                return true;
            }
            else if ((stack.isEmpty() || !storage.getResource().matches(stack)) && !storage.isResourceBlank())
            {
                ItemStack giveStack = storage.getResource().toStack((int) storage.getAmount());
                player.addItem(giveStack);
                storage.extract(storage.getResource(), storage.getAmount(), transaction);
                transaction.commit();
                return true;
            }
            else transaction.abort();
        }
        return false;
    }

    public static <T extends TransferVariant<?>> Optional<Long> totalAmount(Storage<T> storage, T resource, Transaction transaction)
    {
        return StreamSupport.stream(storage.spliterator(), false)
                .filter(view -> view.getResource().equals(resource))
                .map(StorageView::getAmount)
                .reduce(Long::sum);
    }

    public static boolean playerHoldingPipe(Player player, InteractionHand hand)
    {
        return player.getItemInHand(hand).getItem() instanceof BlockItem blockItem && (blockItem.getBlock() instanceof FluidPipe
                || blockItem instanceof FluidComponentItem);
    }

    public static boolean checkFluidComponent(Item item)
    {
        return item instanceof BlockItem bi && bi instanceof FluidComponentItem;
    }
}
