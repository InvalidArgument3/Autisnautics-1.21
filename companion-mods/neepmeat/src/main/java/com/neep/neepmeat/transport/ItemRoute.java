/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the upstream route list. Directions are stored in order and
 * consumed from the front as an item passes each pipe.
 */

package com.neep.neepmeat.transport;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;

import java.util.List;

public class ItemRoute extends ObjectArrayList<Direction> {
    public static final ItemRoute EMPTY = new ItemRoute();

    public ItemRoute() {}

    public ItemRoute(List<Direction> route) {
        super(route);
    }

    public ItemRoute(ItemRoute route) {
        super(route);
    }

    public byte[] write() {
        byte[] bytes = new byte[size()];
        for (int i = 0; i < size(); i++) {
            bytes[i] = (byte) get(i).get3DDataValue();
        }
        return bytes;
    }

    public static ItemRoute read(ByteArrayTag tag) {
        if (tag == null) {
            return EMPTY;
        }
        ItemRoute route = new ItemRoute();
        for (byte value : tag.getAsByteArray()) {
            route.add(Direction.from3DDataValue(value));
        }
        return route;
    }

    public Direction pop() {
        return isEmpty() ? null : remove(0);
    }
}
