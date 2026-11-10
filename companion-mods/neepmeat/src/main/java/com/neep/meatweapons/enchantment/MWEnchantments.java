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

package com.neep.meatweapons.enchantment;

import com.neep.meatlib.enchantment.CustomEnchantment;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MWEnchantments
{
//    public static Enchantment SPIKES;
    public static Enchantment ARGH;

    public static void init()
    {
//        SPIKES = register("spikes", new ThingEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.WEAPON, 4, EquipmentSlot.MAINHAND));
    }

    private static Enchantment register(String name, Enchantment enchantment)
    {
        return Registry.register(BuiltInRegistries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, name), enchantment);
    }

    protected static class ThingEnchantment extends Enchantment implements CustomEnchantment
    {
        protected final int maxLevel;

        protected ThingEnchantment(Rarity weight, EnchantmentCategory type, int maxLevel, EquipmentSlot... slotTypes)
        {
            super(weight, type, slotTypes);
            this.maxLevel = maxLevel;
        }

        public float getDamageBonus(int level, MobType group)
        {
            return Math.max(0f, level) * 0.5f;
        }

        @Override
        public int getMinCost(int level)
        {
            return (int) Math.max(0, Math.floor(Mth.lerpInt(((float) level) / maxLevel, 0, 30)) - 11);
        }

        @Override
        public int getMaxCost(int level)
        {
            return getMinCost(level) + 11;
        }

        @Override
        public int getMaxLevel()
        {
            return 4;
        }

        @Override
        public boolean canEnchant(ItemStack stack)
        {
            return stack.is(MWItems.ASSAULT_DRILL);
        }
    }
}
