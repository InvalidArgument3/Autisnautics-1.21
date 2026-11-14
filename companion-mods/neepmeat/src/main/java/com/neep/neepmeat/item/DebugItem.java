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
import com.neep.neepmeat.api.processing.random_ores.RandomOres;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public class DebugItem extends BaseItem
{
    public DebugItem(Properties settings)
    {
        super(settings);
    }

    public InteractionResult useOn(UseOnContext context)
    {
        var provider = RandomOres.INSTANCE.makeProvider(context.getLevel(), context.getClickedPos(), RandomSource.create());

        System.out.println(provider.print());

        return InteractionResult.SUCCESS;
    }
}
