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

package com.neep.meatlib.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BaseBlock extends Block implements MeatlibBlock
{
    public final BlockItem blockItem;

    public BaseBlock(RegistrationContext ctx, Properties settings)
    {
        this(ctx, ItemSettings.block(), settings);
//        super(settings);
//        this.blockItem = new BaseBlockItem(this, registryName, ItemSettings.block());
//        this.registryName = registryName;
//        addTags();
    }

    public BaseBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        this.blockItem = itemSettings.getFactory().create(this, ctx, itemSettings);
    }

    public Item getBlockItem()
    {
        return blockItem;
    }
}
