/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.item;

import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BaseBlockItem extends BlockItem implements MeatlibItem {
    private final TooltipSupplier tooltip;

    public BaseBlockItem(Block block, ItemSettings settings) {
        super(block, new Item.Properties().stacksTo(settings.maxCount));
        this.tooltip = settings.tooltip();
    }

    public BaseBlockItem(Block block, RegistrationContext context, ItemSettings settings) {
        this(block, settings);
    }

    @Override
    public TooltipSupplier meatlibTooltip() {
        return tooltip;
    }
}
