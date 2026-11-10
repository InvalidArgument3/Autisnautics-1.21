/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Coloured pipes only connect to the same colour, to an uncoloured pipe, or to a
 * fluid handler. That is the upstream PipeCol rule.
 */

package com.neep.neepmeat.machine.transport;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BaseEntityBlock;

public class ColoredFluidPipeBlock extends FluidPipeBlock {
    private final DyeColor color;
    private final MapCodec<ColoredFluidPipeBlock> codec;

    public ColoredFluidPipeBlock(DyeColor color) {
        this.color = color;
        this.codec = simpleCodec(properties -> new ColoredFluidPipeBlock(color));
    }

    @Override
    public DyeColor color() {
        return color;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }
}
