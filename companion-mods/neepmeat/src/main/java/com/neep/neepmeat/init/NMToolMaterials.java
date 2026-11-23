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

package com.neep.neepmeat.init;

import com.google.common.base.Suppliers;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Supplier;

public enum NMToolMaterials implements Tier
{
    MEAT_STEEL(MiningLevels.DIAMOND, 1500, 7.0F, 2F, 14, () -> Ingredient.of(NMItems.MEAT_STEEL_INGOT)),
    EMBOSSED_MEAT_STEEL(MiningLevels.DIAMOND, 1500, 7.0F, 2F, 22, () -> Ingredient.of(NMItems.MEAT_STEEL_INGOT))

    ;

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;

    private final Supplier<Ingredient> repairIngredient;

    NMToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient)
    {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = Suppliers.memoize(repairIngredient::get); // Hmmmmmmmm
    }

    @Override
    public int getUses()
    {
        return this.itemDurability;
    }

    @Override
    public float getSpeed()
    {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamageBonus()
    {
        return this.attackDamage;
    }

    @Override
    public int getLevel()
    {
        return this.miningLevel;
    }

    @Override
    public int getEnchantmentValue()
    {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return this.repairIngredient.get();
    }
}
