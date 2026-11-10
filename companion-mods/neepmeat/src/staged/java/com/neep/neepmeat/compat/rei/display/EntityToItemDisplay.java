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

package com.neep.neepmeat.compat.rei.display;

import com.google.common.collect.Lists;
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.plc.recipe.EntityToItemRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.List;

public class EntityToItemDisplay extends ManufactureDisplay<EntityRecipeInput>
{
//    public static final EntryType<EntityType<?>> ENTITY_TYPE_ENTRY_TYPE = EntryType.deferred(new Identifier(NeepMeat.NAMESPACE, "entity_type"));

    private final List<EntryIngredient> outputs;
    private final List<EntryIngredient> inputs;

    public EntityToItemDisplay(EntityToItemRecipe recipe)
    {
        super(recipe.getBase(), recipe.getSteps());
        this.outputs = Collections.singletonList(EntryIngredients.ofItems(List.of(recipe.getRecipeOutput().resource()), (int) recipe.getRecipeOutput().minAmount()));

        this.inputs = Lists.newArrayList();
//        inputs.add(EntryIngredients.of(recipe.getBase()));
        appendStepIngredients(steps, inputs);
    }

    @Override
    public List<EntryIngredient> getInputEntries()
    {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries()
    {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.ENTITY_TO_ITEM;
    }

//    public static EntryStack<?> entityToStack(EntityType<?> entityType)
//    {
//        return EntryStack.of(ENTITY_TYPE_ENTRY_TYPE, entityType);
//    }
}
