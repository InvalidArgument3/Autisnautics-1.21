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

package com.neep.neepmeat.component;

import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.item.ItemStack;

public class ItemPressureWasherComponent extends ItemComponent implements PressureWasherComponent
{
    public ItemPressureWasherComponent(ItemStack stack)
    {
        super(stack);
    }

    @Override
    public void setFluid(FluidVariant fluid)
    {
        putCompound("fluid", fluid.toNbt());
    }

    @Override
    public FluidVariant getFluid()
    {
        return FluidVariant.fromNbt(getCompound("fluid"));
    }

    public void setAmount(long amount)
    {
        putLong("amount", amount);
        if (amount <= 0)
            setFluid(FluidVariant.blank());

//        int maxDamage = stack.getMaxDamage();
//        int newDamage = (int) MathHelper.clamp(
//                maxDamage * (1. - amount / (double) getCapacity()),
//                0., maxDamage);
//        stack.setDamage(newDamage);
    }

    @Override
    public long getAmount()
    {
        return getLong("amount");
    }

    @Override
    public long getCapacity()
    {
        return FluidConstants.BUCKET;
    }
}
