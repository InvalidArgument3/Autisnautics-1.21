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

package com.neep.neepmeat.machine.live_machine.component;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class ChestComponent implements LivingMachineComponent
{
    private final ChestBlockEntity be;
    private final InventoryStorage inventoryStorage;

    public ChestComponent(ChestBlockEntity be)
    {
        this.be = be;
        this.inventoryStorage = InventoryStorage.of(be, null);
    }

    @Override
    public boolean componentRemoved()
    {
        return be.isRemoved();
    }

    @Override
    public ComponentType<?> getComponentType()
    {
        return LivingMachineComponents.ITEM_OUTPUT;
    }

    public InventoryStorage getStorage()
    {
        return inventoryStorage;
    }

    public ChestBlockEntity get()
    {
        return be;
    }
}
