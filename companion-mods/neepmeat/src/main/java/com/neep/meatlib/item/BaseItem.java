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
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public class BaseItem extends Item implements MeatlibItem {
    private final TooltipSupplier tooltip;

    public BaseItem(ItemSettings settings) {
        super(new Item.Properties().stacksTo(settings.maxCount));
        this.tooltip = settings.tooltip();
    }

    public static DeferredItem<BaseItem> register(RegistrationContext context, String path, ItemSettings settings) {
        return context.item(path, () -> new BaseItem(settings));
    }

    @Override
    public TooltipSupplier meatlibTooltip() {
        return tooltip;
    }
}
