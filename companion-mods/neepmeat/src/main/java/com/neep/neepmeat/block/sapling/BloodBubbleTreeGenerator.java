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
 *
 * 1.21 replaced AbstractTreeGrower with the TreeGrower record.
 */

package com.neep.neepmeat.block.sapling;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public final class BloodBubbleTreeGenerator {
    public static final ResourceKey<ConfiguredFeature<?, ?>> FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "blood_bubble_tree"));
    public static final TreeGrower GROWER = new TreeGrower("blood_bubble", Optional.empty(), Optional.of(FEATURE), Optional.empty());

    private BloodBubbleTreeGenerator() {}
}
