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

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * A parameter container similar to Item.Settings for use in Block and BlockItem constructors.
 * This exists to ease the addition of new general functionality to every block without having to change
 * constructor arguments.
 * <p>
 * (This is slightly redundant now that I've decided to extend FabricItemSettings, but this functionality still needs to be
 * moved over.)
 */
public class ItemSettings
{
    public int maxCount;
    protected TooltipSupplier tooltipSupplier;
    protected MeatlibBlock.ItemFactory factory;

    protected ItemSettings()
    {
        this.maxCount = 64;
        this.tooltipSupplier = TooltipSupplier.BLANK;
    }

    public static ItemSettings block()
    {
        return new ItemSettings().factory(BaseBlockItem::new);
    }

    public static ItemSettings item()
    {
        return new ItemSettings();
    }

    public ItemSettings maxCount(int maxCount)
    {
        this.maxCount = maxCount;
        return this;
    }

    public ItemSettings tooltip(TooltipSupplier tooltipSupplier)
    {
        if (tooltipSupplier == TooltipSupplier.BLANK)
            this.tooltipSupplier = tooltipSupplier;
        else
            this.tooltipSupplier = this.tooltipSupplier.append(tooltipSupplier);
        return this;
    }

    public ItemSettings factory(MeatlibBlock.ItemFactory factory)
    {
        this.factory = factory;
        return this;
    }

    public MeatlibBlock.ItemFactory getFactory()
    {
        return factory;
    }

    public BlockItem create(Block block, RegistrationContext ctx)
    {
        return factory.create(block, ctx, this);
    }
}
