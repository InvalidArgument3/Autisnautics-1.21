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

package com.neep.neepmeat.item.util;

import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import com.neep.neepmeat.implant.item.LivingToolImplant;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.item.LivingToolImplantItem;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LivingTools
{
    @Nullable
    public static Integer getBarCol(ItemStack stack)
    {
        if (LivingToolImplantItem.applicable(stack.getItem()))
        {
            @Nullable ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);
            if (manager != null && manager.isInstalled(LivingToolImplant.ID))
                return PLCCols.MEAT_ITEM_BAR_COL;
        }

        return null;
    }

    public static boolean isBarVisible(ItemStack stack)
    {
        if (LivingToolImplantItem.applicable(stack.getItem()))
        {
            @Nullable ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);
            return manager != null && manager.isInstalled(LivingToolImplant.ID);
        }

        return false;
    }

    public static void onHit(Level world, ItemStack stack, LivingEntity user)
    {
        if (stack.getDamageValue() > (int) (0.9 * stack.getMaxDamage()))
        {
            @Nullable ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);
            if (manager == null)
                return;

            if (manager.isInstalled(LivingToolImplant.ID))
            {
                world.playSound(null, user, NMSounds.CAKE_SCREAM, SoundSource.PLAYERS,
                        (float) user.getRandom().triangle(1, 0.1),
                        (float) user.getRandom().triangle(1, 0.2));
            }
        }
    }

    /// 1 durability point is equivalent to 1 block mined.
    /// @return -1 if invalid.
    public static int getFluidForDurability(FluidVariant variant)
    {
        if (variant.isOf(NMFluids.ANIMAL_FEED.still()))
        {
            return 162; // 500 durability for 1 bucket
        }
        else if (variant.isOf(NMFluids.MEAT.still()))
        {
            return 108; // 750 durability for 1 bucket
        }
        else if (variant.isOf(NMFluids.FOOD.still()))
        {
            CompoundTag nbt = variant.getNbt();
            if (nbt != null)
            {
                return (int) (600 / MeatFluidUtil.getHunger(nbt));
            }
        }

        return -1;
    }
}
