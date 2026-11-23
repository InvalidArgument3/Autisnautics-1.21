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

package com.neep.neepmeat.block.entity;

import com.neep.neepmeat.api.Burner;
import com.neep.neepmeat.machine.HeatableFurnace;
import com.neep.neepmeat.mixin.FurnaceAccessor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

public class FurnaceBurnerImpl implements Burner
{
    protected final FurnaceAccessor furnace;
    private long outputPower;

    public FurnaceBurnerImpl(FurnaceAccessor furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public void tickPowerConsumption()
    {
        Burner.super.tickPowerConsumption();

        // Prevent overunity
        if (((HeatableFurnace) furnace).neepMeat$getHeat() > 0)
        {
            outputPower = 0;
            return;
        }

        if (furnace.getBurnTime() == 0)
        {
            ItemStack itemStack = furnace.getInventory().get(1);
            int time = furnace.callGetFuelTime(itemStack);
            furnace.setFuelTime(time);
            furnace.setBurnTime(time);

            // Consume fuel stack or replace with the recipe remainder
            Item remainder = itemStack.getItem().getCraftingRemainingItem();
            if (remainder == null) itemStack.shrink(1);
            else
            {
                furnace.getInventory().set(1, new ItemStack(remainder));
            }
            updateBlockstate();
        }
        else
        {
            updateBlockstate();
        }
        furnace.setCookTime(0);
        outputPower = furnace.getBurnTime() > 0 ? 150 : 0;
    }

    @Override
    public double getOutputPower()
    {
        return outputPower;
    }

    protected void updateBlockstate()
    {
        AbstractFurnaceBlockEntity furnaceBE = (AbstractFurnaceBlockEntity) (furnace);
        Level world = furnaceBE.getLevel();
        world.setBlockAndUpdate(furnaceBE.getBlockPos(), furnaceBE.getBlockState().setValue(AbstractFurnaceBlock.LIT, furnace.getBurnTime() > 0));
    }

    public static Burner get(FurnaceBlockEntity be, Direction ctx)
    {
        return new FurnaceBurnerImpl((FurnaceAccessor) be);
    }
}
