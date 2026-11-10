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

package com.neep.neepmeat.compat.rei;

import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.neepmeat.compat.rei.display.*;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.Item;
import java.util.ArrayList;

public class NMCommonPlugin implements REIServerPlugin, NMREIPlugin
{
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry)
    {
        registry.register(GRINDING, GrindingDisplay.serializer(GRINDING));
        registry.register(ADVANCED_CRUSHING, GrindingDisplay.serializer(ADVANCED_CRUSHING));
        registry.register(COMPACTING, CompactingDisplay.serializer());
        registry.register(MIXING, MixingDisplay.serializer());
        registry.register(ALLOY_SMELTING, AlloySmeltingDisplay.serializer());
        registry.register(VIVISECTION, VivisectionDisplay.serializer());
        registry.register(ENLIGHTENING, EnlighteningDisplay.serializer());
        registry.register(PRESSING, PressingDisplay.serializer());
        registry.registerNotSerializable(ITEM_MANUFACTURE);
        registry.registerNotSerializable(ENTITY_TO_ITEM);
        registry.register(TRANSFORMING_TOOL, TransformingToolDisplay.serializer());
        registry.register(TROMMEL, TrommelDisplay.serializer());
    }

    public static EntryIngredient inputToIngredient(RegistryRecipeInput<Item> input)
    {
        return EntryIngredients.ofItems(new ArrayList<>(input.getAll()), (int) input.amount());
    }
}
