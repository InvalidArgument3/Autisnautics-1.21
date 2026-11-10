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

package com.neep.meatlib.item;

import com.neep.meatlib.registry.RegistrationContext;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BaseBlockItem extends BlockItem implements MeatlibItem
{
    private final TooltipSupplier tooltipSupplier;

    public BaseBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
    {
        this(block, ctx, itemSettings, new MeatlibItemSettings().stacksTo(itemSettings.maxCount).group(ctx.getDefaultGroup()));
    }

    public BaseBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(block, settings);
        this.tooltipSupplier = itemSettings.tooltipSupplier;
        ctx.append(block, this);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        tooltipSupplier.apply(itemStack, tooltip);
        this.getBlock().appendHoverText(itemStack, world, tooltip, tooltipContext);
//        for (int i = 0; i < loreLines; ++i)
//        {
//            tooltip.add(new TranslatableText(getTranslationKey() + ".lore_" + i).formatted(Formatting.GRAY));
//        }
    }

    public String getRegistryName()
    {
        return "";
    }
}
