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

package com.neep.meatweapons.meatgun;

import com.neep.meatweapons.MeatWeapons;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface AmmunitionProvider
{
    int F = 5;

    ItemApiLookup<AmmunitionProvider, Context> LOOKUP = ItemApiLookup.get(
            new ResourceLocation(MeatWeapons.NAMESPACE, "ammunition"), AmmunitionProvider.class, Context.class);

    AmmunitionType ammoType();

    int getAmount();

    // Currently, only consuming the entire item is supported.
    void consume();

    /// Do not mutate
    ItemStack getStack();

    interface Context
    {
        void setStack(ItemStack stack);
    }

    record InventoryContext(Container inventory, int slot) implements Context
    {
        public void setStack(ItemStack stack)
        {
            inventory.setItem(slot, stack);
        }
    }

//    record StackContext(ItemStack stack) implements Context
//    {
//        @Override
//        public void setStack(ItemStack stack)
//        {
//            this.stack.setCount(stack.getCount());
//        }
//    }
}
