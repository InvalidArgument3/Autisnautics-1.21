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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseSwordItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

public class RolledNewspaperItem extends BaseSwordItem implements OverrideSwingItem
{
    public RolledNewspaperItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(Tiers.STONE, 1, 3f, settings);
    }

    public static boolean isHolding(ItemStack stack)
    {
        return false;
//        return stack.isOf(NMItems.ROLLED_NEWSPAPER);
    }

    public static void attackEntity(Entity target, ServerPlayer player)
    {
       target.hurt(BulletDamageSource.create(player.level(), player, 0, false), 0.1f);
    }

    @Override
    public boolean onSwing(ItemStack stack, Player player)
    {
//        player.handSwingProgress = 0;
//        player.handSwingTicks = 0;
        return false;
    }
}
