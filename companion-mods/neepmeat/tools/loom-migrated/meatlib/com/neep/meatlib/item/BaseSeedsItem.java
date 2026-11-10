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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BaseSeedsItem extends ItemNameBlockItem implements MeatlibItem
{
    protected int lore;

    public BaseSeedsItem(Block block, RegistrationContext ctx, int maxCount, int loreLines)
    {
        super(block, new MeatlibItemSettings().stacksTo(maxCount).group(ctx.getDefaultGroup()));
        this.lore = loreLines;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        for (int i = 0; i < lore; ++i)
        {
            tooltip.add(Component.translatable(getDescriptionId() + ".lore_" + i).withStyle(ChatFormatting.GRAY));
//            tooltip.add(Text.translatable(getTranslationKey() + ".lore_" + i).formatted(Formatting.GRAY));
        }
    }
}
