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

import com.neep.meatlib.item.MeatlibItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.entity.MobEggEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class MobEggItem extends Item implements MeatlibItem
{
    private final TooltipSupplier tooltip;

    public MobEggItem(RegistrationContext ctx, TooltipSupplier tooltip, Properties settings)
    {
        super(settings);
        this.tooltip = tooltip;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player user = context.getPlayer();
        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Vec3 hit = context.getClickLocation();
        if (!world.isClientSide())
        {
            EntityType<?> hatchType = EssentialSaltesItem.getEntityType(stack);
            MobEggEntity egg = new MobEggEntity(world, hatchType);
            egg.setPos(hit);
            if (world.noCollision(egg))
            {
                world.addFreshEntity(egg);
//                world.emitGameEvent(user, GameEvent.ENTITY_PLACE, BlockPos.ofFloored(hit));
                world.gameEvent(user, GameEvent.ENTITY_PLACE, BlockPos.containing(hit));
                if (!user.isCreative())
                {
                    stack.shrink(1);
                }
            }
            user.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        EntityType<?> type = EssentialSaltesItem.getEntityType(stack);
        if (type != null)
        {
            return Component.translatable(this.getDescriptionId(), type.getDescription());
        }
        else return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag context)
    {
        super.appendHoverText(stack, world, tooltip, context);
        this.tooltip.apply(stack, tooltip);
    }
}
