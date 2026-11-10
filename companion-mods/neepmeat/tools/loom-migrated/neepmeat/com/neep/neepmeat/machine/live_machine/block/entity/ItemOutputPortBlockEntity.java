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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.ItemOutputPortBlock;
import com.neep.neepmeat.machine.live_machine.block.PortBlock;
import com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent;
import com.neep.neepmeat.screen_handler.ItemOutputScreenHandler;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ItemOutputPortBlockEntity extends SyncableBlockEntity implements ItemOutputComponent, MenuProvider, PortBlock.DestroyListener
{
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(9, this::setChanged);
    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);
    private final StorageDelegate delegate = new StorageDelegate();
    private final ExtractionOnlyDelegate publicStorage = new ExtractionOnlyDelegate();
    private final EjectPropertyDelegate propertyDelegate = new EjectPropertyDelegate();

    public ItemOutputPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<?> getComponentType()
    {
        return LivingMachineComponents.ITEM_OUTPUT;
    }

    public Storage<ItemVariant> getStorage(Direction unused)
    {
        return publicStorage;
    }

    @Override
    public Storage<ItemVariant> getInternalStorage()
    {
        return delegate;
    }

    @Override
    public void tryEject()
    {
        if (getBlockState().getValue(ItemOutputPortBlock.AUTO_EJECT))
        {
            // If items are already stored when auto eject is enabled, they need to be ejected.
            // Additionally, when ejection fails, items will be stored instead.
            // We need to periodically try to eject them to prevent weirdness.

            try (Transaction transaction = Transaction.openOuter())
            {
                int rand = getLevel().getRandom().nextInt(inventory.getContainerSize());
                var stack = inventory.getItem(rand);
                if (!stack.isEmpty())
                {
                    long amount = stack.getCount();
                    var resource = ItemVariant.of(stack);
                    long ejected = ItemPipeUtil.EjectOperation.of(level, worldPosition, getBlockState().getValue(PortBlock.FACING))
                            .useRouting(true)
                            .stackToAny(resource, amount, transaction)
                            .amount();
                    if (ejected == amount)
                    {
                        inventory.setItem(rand, ItemStack.EMPTY);
                        inventory.setItem(rand, stack.copyWithCount((int) (amount - ejected)));
                        transaction.commit();
                    }
                    else if (ejected > 0)
                    {
                        inventory.setItem(rand, stack.copyWithCount((int) (amount - ejected)));
                        transaction.commit();
                    }
                    else
                    {
                        transaction.abort();
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        inventory.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        inventory.readNbt(nbt);
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
        return new ItemOutputScreenHandler(playerInventory, inventory, syncId, propertyDelegate);
    }

    @Override
    public void onBlockDestroyed()
    {
        Containers.dropContents(level, getBlockPos(), inventory);
    }

    private class ExtractionOnlyDelegate implements ExtractionOnlyStorage<ItemVariant>
    {
        @Override
        public long extract(ItemVariant itemVariant, long l, TransactionContext transactionContext)
        {
            return inventoryStorage.extract(itemVariant, l, transactionContext);
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator()
        {
            return inventoryStorage.iterator();
        }
    }

    private class StorageDelegate implements Storage<ItemVariant>
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            long amount = maxAmount;
            if (getBlockState().getValue(ItemOutputPortBlock.AUTO_EJECT))
            {
                amount -= ItemPipeUtil.EjectOperation.of(level, worldPosition, getBlockState().getValue(PortBlock.FACING))
                        .useRouting(true)
                        .stackToAny(resource, amount,
//                        s -> s != this && s != publicStorage,
                        transaction)
                        .amount();
                if (amount == 0)
                    return maxAmount;
            }

            long inserted = inventoryStorage.insert(resource, amount, transaction);
            if (inserted < amount)
            {
                ItemPipeUtil.EjectOperation.of(level, worldPosition, getBlockState().getValue(PortBlock.FACING))
                        .useRouting(true)
                        .stackToAny(resource, maxAmount - inserted,
                        //s -> s != this && s != publicStorage,
                        transaction);
            }

            return maxAmount;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return inventoryStorage.extract(resource, maxAmount, transaction);
        }

        @Override
        public @NotNull Iterator<StorageView<ItemVariant>> iterator()
        {
            return inventoryStorage.iterator();
        }
    }

    private class EjectPropertyDelegate implements ContainerData
    {
        @Override
        public int get(int index)
        {
            if (index == 0)
                return getBlockState().getValue(ItemOutputPortBlock.AUTO_EJECT) ? 1 : 0;
            return 0;
        }

        @Override
        public void set(int index, int value)
        {
            if (index == 0)
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ItemOutputPortBlock.AUTO_EJECT, value > 0));
        }

        @Override
        public int getCount()
        {
            return 1;
        }
    }
}
