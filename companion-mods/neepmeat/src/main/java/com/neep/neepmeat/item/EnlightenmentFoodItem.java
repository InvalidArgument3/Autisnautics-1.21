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

import com.neep.meatlib.item.BaseCraftingItem;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnlightenmentFoodItem extends BaseCraftingItem
{
    public EnlightenmentFoodItem(RegistrationContext ctx, int loreLines, Properties settings)
    {
        super(ctx, loreLines, settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
        if (new net.minecraft.world.item.ItemStack(this).has(net.minecraft.core.component.DataComponents.FOOD))
        {
            var enlightenment = NMComponents.ENLIGHTENMENT_MANAGER.getNullable(user);
            if (enlightenment != null)
            {
                enlightenment.addChronic(3f);
            }

            return user.eat(world, stack);
        }

        return super.finishUsingItem(stack, world, user);
    }
}
