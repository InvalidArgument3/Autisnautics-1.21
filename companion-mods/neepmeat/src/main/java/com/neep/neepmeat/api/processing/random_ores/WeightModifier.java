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

package com.neep.neepmeat.api.processing.random_ores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface WeightModifier
{
    Registry<Codec<? extends WeightModifier>> REGISTRY = new MappedRegistry<>(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "random_ore_modifiers")), Lifecycle.stable());

    Codec<Codec<? extends WeightModifier>> CODEC = REGISTRY.byNameCodec();

    float apply(float base, Level world, BlockPos pos);

    Codec<? extends WeightModifier> getCodec();
}
