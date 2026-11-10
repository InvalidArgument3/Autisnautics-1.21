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

import net.minecraft.world.item.ItemStack;

public class SimpleItemAmmunitionProvider implements AmmunitionProvider
{
    private final ItemStack stack;
    private final Context context;
    private final AmmunitionType type;
    private final int amount;

    public SimpleItemAmmunitionProvider(ItemStack stack, Context context, AmmunitionType type, int amount)
    {
        this.stack = stack;

        this.context = context;
        this.type = type;
        this.amount = amount;
    }

    @Override
    public AmmunitionType ammoType()
    {
        return type;
    }

    @Override
    public int getAmount()
    {
        return amount;
    }

    @Override
    public void consume()
    {
        ItemStack newStack = stack.copy();
        newStack.shrink(1);
        context.setStack(newStack);
    }

    @Override
    public ItemStack getStack()
    {
        return stack;
    }
}
