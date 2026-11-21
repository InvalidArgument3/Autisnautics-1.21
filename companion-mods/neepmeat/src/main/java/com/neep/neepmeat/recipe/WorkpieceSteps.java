/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record WorkpieceSteps(List<ManufactureRecipe.ManufactureStepSpec> steps) {
    public static final Codec<WorkpieceSteps> CODEC = ManufactureRecipe.ManufactureStepSpec.CODEC.listOf()
            .xmap(WorkpieceSteps::new, WorkpieceSteps::steps);
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkpieceSteps> STREAM_CODEC =
            ManufactureRecipe.ManufactureStepSpec.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(WorkpieceSteps::new, WorkpieceSteps::steps);

    public static WorkpieceSteps on(ItemStack stack) {
        WorkpieceSteps steps = stack.get(NeepComponents.WORKPIECE.get());
        return steps == null ? new WorkpieceSteps(List.of()) : steps;
    }
}
