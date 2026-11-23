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

package com.neep.meatlib.mixin;

import com.google.common.collect.Lists;
import com.neep.meatlib.enchantment.CustomEnchantment;
import com.neep.meatlib.entity.player.FakePlayerEntity;
import com.neep.meatlib.item.CustomEnchantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin
{
    @Inject(method = "getLooting", at = @At(value = "HEAD"), cancellable = true)
    private static void onGetLooting(LivingEntity entity, CallbackInfoReturnable<Integer> cir)
    {
        if (entity instanceof FakePlayerEntity fakePlayer)
        {
            cir.setReturnValue(fakePlayer.getLooting());
        }
    }

    @Inject(method = "getPossibleEntries", at = @At(value = "RETURN", target = "Lnet/minecraft/enchantment/Enchantment;isTreasure()Z"))
    private static void onInvokeIsTreasure(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir)
    {
        // Remove Meatlib enchantments if the item is invalid.
        // This prevents you from getting special enchantments that do nothing on a sword, for example.
        if (!(stack.getItem() instanceof CustomEnchantable))
        {
            var list = cir.getReturnValue();
            list.removeIf(e -> e.enchantment instanceof CustomEnchantment);
        }
    }

    @Inject(method = "getPossibleEntries", at = @At(value = "HEAD"), cancellable = true)
    private static void onGetEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir)
    {
        // Diverge only if the item implements CustomEnchantable. Vanilla items will be untouched.
        // There's probably a more appropriate tool for doing this.
        Item item = stack.getItem();
        if (item instanceof CustomEnchantable customEnchantable)
        {
            ArrayList<EnchantmentInstance> list = Lists.newArrayList();
            Set<Enchantment> others = customEnchantable.injectVanilla();

            boolean isBook = stack.is(Items.BOOK);
            mainBlock: for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT)
            {
                boolean allowed = false;
                if (enchantment instanceof CustomEnchantment)
                {
                    // Everything is the same as normal except this ignores the EnchantmentTarget.
                    allowed = enchantment.canEnchant(stack);
                }
                else
                {
                    // Vanilla behaviour for normal enchantments.
                    allowed = enchantment.category.canEnchant(item) || others.contains(enchantment);
                }

                if (!allowed && !isBook
                        || enchantment.isTreasureOnly() && !treasureAllowed
                        || !enchantment.isDiscoverable())
                    continue;

                for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i)
                {
                    if (power < enchantment.getMinCost(i) || power > enchantment.getMaxCost(i))
                        continue;

                    list.add(new EnchantmentInstance(enchantment, i));
                    continue mainBlock;
                }
            }
            cir.setReturnValue(list);
        }
    }
}
