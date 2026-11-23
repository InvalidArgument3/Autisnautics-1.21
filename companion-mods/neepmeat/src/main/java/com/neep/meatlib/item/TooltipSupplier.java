/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface TooltipSupplier {
    TooltipSupplier BLANK = (stack, tooltip) -> {};

    void apply(ItemStack stack, List<Component> tooltip);

    default TooltipSupplier append(TooltipSupplier other) {
        return (stack, tooltip) -> {
            apply(stack, tooltip);
            other.apply(stack, tooltip);
        };
    }

    static TooltipSupplier blank() {
        return BLANK;
    }

    static TooltipSupplier hidden(int lines) {
        return simple(lines);
    }

    static TooltipSupplier combine(TooltipSupplier first, TooltipSupplier second) {
        if (first == null) {
            return second == null ? BLANK : second;
        }
        return second == null ? first : first.append(second);
    }

    static void shiftForInfo(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.meatlib.shift_for_info").withStyle(ChatFormatting.GRAY));
    }

    static void wrapLines(List<Component> tooltip, Component text) {
        if (tooltip != null && text != null) {
            tooltip.add(text);
        }
    }

    static TooltipSupplier simple(int lines) {
        return (stack, tooltip) -> {
            for (int line = 0; line < lines; line++) {
                tooltip.add(Component.translatable(stack.getDescriptionId() + ".lore_" + line).withStyle(ChatFormatting.GRAY));
            }
        };
    }
}
