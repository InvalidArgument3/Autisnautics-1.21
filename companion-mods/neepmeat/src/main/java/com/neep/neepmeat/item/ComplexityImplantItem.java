/*
 * Copyright (c) MeatWheeze 2026.
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

import com.neep.meatlib.registry.RegistrationContext;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ComplexityImplantItem extends ItemImplantItem
{
    private final Predicate<Item> predicate;

    public ComplexityImplantItem(RegistrationContext ctx, String registryName, int lines, ResourceLocation implantId, Predicate<Item> predicate, Properties settings)
    {
        super(ctx, registryName, lines, implantId, settings);
        this.predicate = predicate;
    }

    @Override
    public boolean install(ItemStack stack, Context context)
    {
        if (predicate.test(stack.getItem()))
            return super.install(stack, context);

        return false;
    }
}
