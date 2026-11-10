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

package com.neep.meatweapons.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.entity.AbstractVehicleEntity;
import com.neep.meatweapons.entity.AirtruckEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class AirtruckItem extends BaseItem
{

    public AirtruckItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getEnchantmentValue()
    {
        return 1;
    }

    public InteractionResult useOn(UseOnContext context)
    {
        Player user = context.getPlayer();
        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Vec3 hit = context.getClickLocation();
        if (!world.isClientSide())
        {
            AbstractVehicleEntity entity = AirtruckEntity.create(world);
            entity.setPos(hit);
            if (world.noCollision(entity))
            {
                world.addFreshEntity(entity);
//                world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, BlockPos.ofFloored(hit));
                world.gameEvent((Entity)user, GameEvent.ENTITY_PLACE, BlockPos.containing(hit));
                if (!user.isCreative())
                {
                    stack.shrink(1);
                }
            }
            user.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
