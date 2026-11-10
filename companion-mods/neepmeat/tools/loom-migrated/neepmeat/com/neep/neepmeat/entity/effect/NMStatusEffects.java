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

package com.neep.neepmeat.entity.effect;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class NMStatusEffects
{
    public static MobEffect ASH_PEPARATION =  new AshPreparationStatusEffect(MobEffectCategory.NEUTRAL, 0x194D33);

    public static void init()
    {
        ASH_PEPARATION = register(NeepMeat.NAMESPACE, "ash_preparation", ASH_PEPARATION);
    }

    private static MobEffect register(String namespace, String id, MobEffect entry)
    {
        return Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(namespace, id), entry);
    }
}
