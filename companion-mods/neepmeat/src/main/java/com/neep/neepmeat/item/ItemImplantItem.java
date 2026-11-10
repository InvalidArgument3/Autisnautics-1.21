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
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.implant.item.ItemImplantInstaller;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.init.NMComponents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A mundane item that conveniently stores an externally registered implant.
 * Useful if you want to make an item that represents a specific implant type.
 * This must be registered under {@link ItemImplantInstaller} elsewhere.
 * A JSON recipe is required for the referenced implant to be installed on an entity.
 */
public class ItemImplantItem extends BaseItem implements ItemImplantInstaller
{
    protected final ResourceLocation implantId;

    public ItemImplantItem(RegistrationContext ctx, String registryName, int lines, ResourceLocation implantId, Properties settings)
    {
        super(new ImplantTooltipSupplier(ctx.namespace(), registryName, lines), settings);
        this.implantId = implantId;
    }

    @Override
    public boolean install(ItemStack stack, Context context)
    {
        @Nullable ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);

        if (manager != null && !manager.isInstalled(implantId))
        {
            return manager.installImplant(implantId);
        }
        return false;
    }

    public static class ImplantTooltipSupplier implements TooltipSupplier
    {
        private final String namespace;
        private final String name;
        private final int lines;

        public ImplantTooltipSupplier(String namespace, String name, int lines)
        {
            this.namespace = namespace;
            this.name = name;
            this.lines = lines;
        }

        @Override
        public void apply(ItemStack stack, List<Component> tooltip)
        {
            tooltip.add(Component.translatable("message." + namespace + ".item_implant.title").setStyle(PLCCols.EMPHASIS_TEXT.withBold(true)));
            for (int i = 0; i < lines; ++i)
            {
                var txt = Component.translatable("implant." + namespace + "." + name + ".desc_" + i).setStyle(PLCCols.NORMAL_TEXT);
//                var txt1 = Text.translatable("implant." + namespace + "." + name + ".desc_" + i).formatted(Formatting.GOLD);
                TooltipSupplier.wrapLines(tooltip, txt);
//                TooltipSupplier.wrapLines(tooltip, txt1);
            }
        }
    }
}
