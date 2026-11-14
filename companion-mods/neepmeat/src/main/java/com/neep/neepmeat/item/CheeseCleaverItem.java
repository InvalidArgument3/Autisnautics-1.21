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
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CheeseCleaverItem extends BaseSwordItem
{
//    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    public static String CONTROLLER_NAME = "controller";

    public CheeseCleaverItem(RegistrationContext ctx, Properties settings)
    {
        super(Tiers.DIAMOND, 4, -3f, settings);
    }

    public static void writeCharged(ItemStack stack, boolean charged)
    {
        com.neep.meatlib.item.StackNbt.edit(stack, nbt -> nbt.putBoolean("charged", charged));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        super.hurtEnemy(stack, target, attacker);

        if (stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean("charged"))
        {
            target.push(0, 0.4, 0);
            writeCharged(stack, false);
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        user.startUsingItem(hand);
        return InteractionResultHolder.pass(user.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        if (stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean("charged"))
        {
            return UseAnim.NONE;
        }
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks)
    {
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
       writeCharged(stack, true);
       return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 20;
    }
}
