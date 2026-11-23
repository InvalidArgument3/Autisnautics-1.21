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

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.entity.EntityImplantInstaller;
import com.neep.neepmeat.init.NMComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class PlayerImplantItem extends BaseItem implements EntityImplantInstaller
{
    protected final ResourceLocation implantId;

    public PlayerImplantItem(RegistrationContext ctx, String registryName, int lines, ResourceLocation implantId, Properties settings)
    {
        super(new ImplantTooltipSupplier(registryName, lines), settings);
        this.implantId = implantId;
    }

    @Override
    public void install(Entity entity)
    {
        NMComponents.IMPLANT_MANAGER.maybeGet(entity).ifPresent(manager ->
        {
            manager.installImplant(implantId);
        });
    }

    public static class ImplantTooltipSupplier implements TooltipSupplier
    {
        private final String name;
        private final int lines;
        public ImplantTooltipSupplier(String name, int lines)
        {
            this.name = name;
            this.lines = lines;
        }

        @Override
        public void apply(ItemStack stack, List<Component> tooltip)
        {
            tooltip.add(Component.translatable("message." + NeepMeat.NAMESPACE + ".implant.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < lines; ++i)
            {
                var txt = Component.translatable("implant."
                        + stack.getItemHolder().unwrapKey().get().location().getNamespace() + "." + name + ".desc_" + i).withStyle(ChatFormatting.GOLD);
                TooltipSupplier.wrapLines(tooltip, txt);
            }
        }
    }
}
