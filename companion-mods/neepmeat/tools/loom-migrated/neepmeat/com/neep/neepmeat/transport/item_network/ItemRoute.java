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

package com.neep.neepmeat.transport.item_network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collector;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;

public class ItemRoute extends ObjectArrayList<Direction>
{
    public static final ItemRoute EMPTY = new ItemRoute();

    public ItemRoute()
    {

    }

    public ItemRoute(Stack<Direction> route)
    {
        super(route);
    }

    public ItemRoute(List<Direction> route)
    {
        super(route);
    }

    public ItemRoute(ItemRoute route)
    {
        super(route);
    }

    public static Collector<? super Direction, ?, ItemRoute> collector()
    {
        return Collector.of(ItemRoute::new, ObjectArrayList::add, (ItemRoute l, ItemRoute r) ->
        {
            l.addAll(r);
            return l;
        }, Collector.Characteristics.IDENTITY_FINISH);
    }

    public static ItemRoute fromByteArray(ByteArrayTag byteArray)
    {
        return byteArray.stream().map(b -> Direction.from3DDataValue(b.getAsByte())).collect(ItemRoute.collector());
    }

    public byte[] write()
    {
        byte[] bytes = new byte[size()];
        for (int i = 0; i < size(); ++i)
        {
            bytes[i] = (byte) get(i).get3DDataValue();
        }
        return bytes;
    }

    public static ItemRoute read(ByteArrayTag byteArray)
    {
        if (byteArray == null)
            return ItemRoute.EMPTY;

        return ItemRoute.fromByteArray(byteArray);
    }
}
