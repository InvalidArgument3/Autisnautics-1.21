/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Upstream storage bus: an item pipe that connects to inventories and exposes
 * them to the routing network. It does not simplify, so it stays a vertex.
 */

package com.neep.neepmeat.machine.transport;

import com.mojang.serialization.MapCodec;

public class StorageBusBlock extends ItemPipeBlock {
    public static final MapCodec<StorageBusBlock> CODEC = simpleCodec(properties -> new StorageBusBlock());

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return CODEC;
    }
}
