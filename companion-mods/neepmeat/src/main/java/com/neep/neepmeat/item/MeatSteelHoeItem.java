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

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MeatSteelHoeItem extends HoeItem
{
//    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public MeatSteelHoeItem(Tier material, int attackDamage, float attackSpeed, Properties settings)
    {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (state.is(Blocks.COBWEB))
        {
            return 15.0f;
        }
        else if (state.is(BlockTags.SWORD_EFFICIENT))
        {
            return 1.5f;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state)
    {
        return super.isCorrectToolForDrops(state) || state.is(Blocks.COBWEB);
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return super.isEnchantable(stack);
    }
}
