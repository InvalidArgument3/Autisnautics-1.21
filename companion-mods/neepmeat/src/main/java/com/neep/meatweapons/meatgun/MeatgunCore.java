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

import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.component.PowerResourceComponent;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.FoodCartonItem;
import com.neep.neepmeat.item.util.LivingTools;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/// Represents the core implant of the Meatgun.
/// In the future this needs to implicitly handle compressed air and food, accounting for which core implant is installed.
public record MeatgunCore(RootModuleHolder holder)
{
    public long acceptResource(ItemVariant variant, long maxAmount, TransactionContext transaction)
    {
        if (!variant.toStack().has(net.minecraft.core.component.DataComponents.FOOD)) // TODO: fluid insertion
            return 0;

        var entry = FoodCartonItem.getHungerSaturation(variant);
        int perItem = entry.food() * 10;
        int converted = Math.toIntExact(maxAmount * perItem);

        int capacity = holder.moduleCache().getCapacity(AmmunitionType.METABOLIC);
        int current = holder.moduleCache().getAmmo(AmmunitionType.METABOLIC);
        int canInsert = capacity - current;
        int toInsert = Math.min(canInsert, converted);

        if (toInsert > 0)
        {
            transaction.addCloseCallback((t, r) -> MeatlibStorageUtil.closeCallback(t, r, () ->
            {
                holder.insertAmmo(AmmunitionType.METABOLIC, toInsert);
                holder.markDirty(RootModuleHolder.Reason.SAVE_DATA);
            }));

            return (long) Math.ceil(toInsert / (float) perItem);
        }

        return 0;
    }

    public long acceptResource(FluidVariant variant, long maxAmount, TransactionContext transaction)
    {
        int fluidPerDurability = LivingTools.getFluidForDurability(variant);
        if (fluidPerDurability != -1)
        {
            float perDroplet = 1.f / fluidPerDurability;

            int equivalentAmmo = (int) Math.floor(maxAmount * perDroplet);
            AmmunitionType type = AmmunitionType.METABOLIC;

            int previous = holder.moduleCache().getAmmo(type);
            int capacity = holder.moduleCache().getCapacity(type);
            int toInsert = Math.min(equivalentAmmo, capacity - previous);

            transaction.addCloseCallback((t, r) -> MeatlibStorageUtil.closeCallback(t, r, () ->
            {
                holder.insertAmmo(AmmunitionType.METABOLIC, toInsert);
                holder.markDirty(RootModuleHolder.Reason.SAVE_DATA);
            }));

            long toReturn = (long) (toInsert / perDroplet);

            // Prevent leaving very small amounts of fluid behind that are less than a mb
            if (maxAmount - toReturn < 1 / perDroplet)
                return maxAmount;

            return toReturn;
        }

        return 0;
    }

    public boolean canRun(Player playerEntity, int min)
    {
        var fuelType = getResource();
        if (fuelType == AmmunitionType.AIR)
        {
            PowerResourceComponent component = NMComponents.COMPRESSED_AIR.getNullable(playerEntity);

            return component != null && component.getAir() > 0;
        }
        else if (fuelType == AmmunitionType.METABOLIC)
        {
            int amount = holder.moduleCache().getAmmo(AmmunitionType.METABOLIC);
            return amount > min;
        }

        return false;
    }

    public int getAmount()
    {
        return holder.moduleCache().getAmmo(AmmunitionType.METABOLIC);
    }

    public int getCapacity()
    {
        return holder.moduleCache().getCapacity(AmmunitionType.METABOLIC);
    }

    public @Nullable AmmunitionType getResource()
    {
        return holder.coreImplants().fuelType();
    }
}
