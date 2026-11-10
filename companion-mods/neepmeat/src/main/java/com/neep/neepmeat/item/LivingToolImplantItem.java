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

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.item.PneumaticMotorImplant;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.util.LivingItemStorage;
import com.neep.meatlib.transfer.ContainerItemContext;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LivingToolImplantItem extends ItemImplantItem
{
    public LivingToolImplantItem(RegistrationContext ctx, String registryName, int lines, ResourceLocation implantId, Properties settings)
    {
        super(ctx, registryName, lines, implantId, settings);
    }

    public static boolean applicable(Item item)
    {
//        return item instanceof ToolItem || item instanceof ArmorItem || item instanceof LivingToolApplicable;
        return item.canBeDepleted() && !item.builtInRegistryHolder().is(NMTags.TOOL_ORGANISM_EXCLUDE) || item instanceof LivingToolApplicable;
    }

    @Nullable
    public static Storage<FluidVariant> getStorage(ItemStack stack, ContainerItemContext context)
    {
        return new LivingItemStorage(stack, context);
    }

    @Override
    public boolean install(ItemStack stack, Context context)
    {
        if (applicable(stack.getItem()))
        {
            @Nullable ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);

            if (manager != null)
            {
                manager.replace(PneumaticMotorImplant.ID, context::acceptByproduct);

                return manager.installImplant(implantId);
            }
        }
        return false;
    }
}
