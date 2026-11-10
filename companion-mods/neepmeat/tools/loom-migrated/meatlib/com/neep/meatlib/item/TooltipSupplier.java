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

import com.neep.meatlib.MeatLib;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface TooltipSupplier
{
    TooltipSupplier BLANK = (i, t) -> {};

    void apply(ItemStack stack, List<Component> tooltip);

    default TooltipSupplier append(TooltipSupplier other)
    {
        return TooltipSupplier.combine(this, other);
    }

    static TooltipSupplier blank()
    {
        return BLANK;
    }

    static TooltipSupplier simple(int lines)
    {
        return (stack, list) ->
        {
            for (int lineNumber = 0; lineNumber < lines; ++lineNumber)
            {
                var txt = Component.translatable(stack.getDescriptionId() + ".lore_" + lineNumber).withStyle(ChatFormatting.GRAY);
                wrapLines(list, txt);
            }
        };
    }

    static List<Component> wrapLines(List<Component> list, Component text)
    {
        Font textRenderer = Minecraft.getInstance().font;

        List<FormattedCharSequence> newText = textRenderer.split(text, 200);

        for (var line : newText)
        {
            MutableComponent textLine = Component.empty();

            line.accept((index, style, codePoint) ->
            {
                textLine.append(Component.literal(Character.toString(codePoint)).setStyle(style));
                return true;
            });

            list.add(textLine);
        }

        return list;
    }

    static TooltipSupplier hidden(int lines)
    {
        return new ShiftHidden(lines, ChatFormatting.GRAY);
    }

    static TooltipSupplier combine(TooltipSupplier supplier, TooltipSupplier supplier1)
    {
        return (stack, tooltip) ->
        {
            supplier.apply(stack, tooltip);
            supplier1.apply(stack, tooltip);
        };
    }

    static void shiftForInfo(List<Component> tooltip)
    {
        tooltip.add(Component.translatable("tooltip.%s.shift_for_info".formatted(MeatLib.NAMESPACE)).withStyle(ChatFormatting.GRAY));
    }

    class ShiftHidden implements TooltipSupplier
    {
        private final int lines;
        private final ChatFormatting formatting;

        public ShiftHidden(int lines, ChatFormatting formatting)
        {
            this.lines = lines;
            this.formatting = formatting;
        }

        @Override
        public void apply(ItemStack stack, List<Component> tooltip)
        {
            if (lines != 0 && Screen.hasShiftDown())
            {
                var txt = Component.translatable(stack.getDescriptionId() + ".lore_0").withStyle(formatting);
                wrapLines(tooltip, txt);
            }
            else
            {
                shiftForInfo(tooltip);
            }
        }
    }
}
