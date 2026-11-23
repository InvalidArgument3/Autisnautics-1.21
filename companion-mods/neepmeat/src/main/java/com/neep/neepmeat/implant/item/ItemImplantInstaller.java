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

package com.neep.neepmeat.implant.item;

import net.minecraft.world.item.ItemStack;

/**
 * An implant installer that operates on an ItemStack.
 */
public interface ItemImplantInstaller
{
//    SimpleRegistry<ItemImplantInstaller> REGISTRY = FabricRegistryBuilder.createSimple(
//            ItemImplantInstaller.class, new Identifier(NeepMeat.NAMESPACE, "item_implant_installer")).buildAndRegister();

//    Map<Item, ItemImplantInstaller> ENTRIES = new IdentityHashMap<>();
//
//    static void register(Item item, ItemImplantInstaller installer)
//    {
//        ENTRIES.put(item, installer);
//    }

    boolean install(ItemStack stack, Context context);

    interface Context
    {
        void acceptByproduct(ItemStack stack);
    }
}
