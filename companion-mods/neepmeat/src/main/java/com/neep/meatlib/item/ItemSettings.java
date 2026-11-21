/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.item;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class ItemSettings {
    public int maxCount = 64;
    private TooltipSupplier tooltip = TooltipSupplier.BLANK;
    private MeatlibBlock.ItemFactory factory = BaseBlockItem::new;

    public static ItemSettings block() {
        return new ItemSettings();
    }

    public static ItemSettings item() {
        return new ItemSettings().factory((block, ctx, settings) -> {
            throw new IllegalStateException("item settings have no block factory");
        });
    }

    public ItemSettings maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public ItemSettings retainsInv() { return this; }
    public ItemSettings neepbus() { return this; }
    public ItemSettings plcActuator() { return this; }
    public ItemSettings plcInterrupt() { return this; }
    public ItemSettings plc() { return this; }
    public ItemSettings requiresMotor() { return this; }

    public ItemSettings tooltip(TooltipSupplier tooltip) {
        this.tooltip = this.tooltip.append(tooltip);
        return this;
    }

    public TooltipSupplier tooltip() {
        return tooltip;
    }

    public ItemSettings factory(MeatlibBlock.ItemFactory factory) {
        this.factory = factory;
        return this;
    }

    public MeatlibBlock.ItemFactory factory() {
        return factory;
    }

    /** Loom sources call {@code getFactory()}. */
    public MeatlibBlock.ItemFactory getFactory() {
        return factory;
    }

    public BlockItem create(Block block, RegistrationContext context) {
        return factory.create(block, context, this);
    }
}
