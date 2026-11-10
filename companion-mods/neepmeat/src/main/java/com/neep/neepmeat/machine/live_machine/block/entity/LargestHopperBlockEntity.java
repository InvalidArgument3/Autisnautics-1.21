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
 *
 * Mob damage is not in this block entity. Item entities are swallowed the way upstream does.
 * The component type is the item input, which is what the Loom hopper returns.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.component.ItemInputComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LargestHopperBlockEntity extends BlockEntity implements ItemInputComponent {
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(4, this::setChanged);
    private final InventoryStorage storage = InventoryStorage.of(inventory, null);

    public LargestHopperBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.HOPPER_BE.get(), pos, state);
    }

    public void insertEntity(ItemEntity itemEntity) {
        if (itemEntity.isRemoved()) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (inventory.isEmpty()) {
            inventory.setItem(0, stack.copy());
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
            setChanged();
            return;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (inserted == 0) {
                return;
            }
            if (inserted == stack.getCount()) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                stack.shrink((int) inserted);
            }
            transaction.commit();
        }
    }

    public ImplementedInventory inventory() {
        return inventory;
    }

    @Override
    public Storage<ItemVariant> getStorage(Direction ctx) {
        return storage;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends com.neep.neepmeat.api.live_machine.LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.ITEM_INPUT;
    }
}
