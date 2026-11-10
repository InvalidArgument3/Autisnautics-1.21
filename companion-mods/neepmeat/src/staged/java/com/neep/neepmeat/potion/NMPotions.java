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

package com.neep.neepmeat.potion;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.entity.effect.NMStatusEffects;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.mixin.BrewingRecipeRegistryAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public class NMPotions
{
    public static final Potion ASH_PREPARATION = register(NeepMeat.NAMESPACE, "ash_preparation", new Potion(new MobEffectInstance(NMStatusEffects.ASH_PEPARATION, 3600)));

    public static void init()
    {
        BrewingRecipeRegistryAccessor.callRegisterPotionRecipe(Potions.AWKWARD, NMItems.ENLIGHTENED_BRAIN, ASH_PREPARATION);
    }

    private static Potion register(String namespace, String name, Potion potion)
    {
        return Registry.register(BuiltInRegistries.POTION, ResourceLocation.fromNamespaceAndPath(namespace, name), potion);
    }
}
