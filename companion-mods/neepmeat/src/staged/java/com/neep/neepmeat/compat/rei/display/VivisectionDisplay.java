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

import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.recipe.VivisectionRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import java.util.Collections;
import java.util.List;

public class VivisectionDisplay extends BasicDisplay
{
    protected final List<Component> entities;

    public VivisectionDisplay(VivisectionRecipe recipe)
    {
        this(List.of(recipe.getEntityType()),
                recipe.getRecipeOutput().stream().map(o -> EntryIngredients.ofItems(List.of(o.resource()), (int) o.minAmount())).toList(),
                null);
    }

    // AAAAAAAAAAAAAAAAAAAAAAAAAA
    public VivisectionDisplay(List<EntityType<?>> entities, List<EntryIngredient> output, Void argh)
    {
        super(Collections.emptyList(), output);
        this.entities = entities.stream().map(e -> (Component) Component.translatable(e.getDescriptionId())).toList();
    }

    public VivisectionDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs)
    {
        super(inputs, outputs);
        this.entities = Collections.emptyList();
    }

    public static VivisectionDisplay of(List<EntityType<?>> inputs, List<EntryIngredient> output)
    {
        return new VivisectionDisplay(inputs, output, null);
    }

    public static VivisectionDisplay of(Item item, List<EntryIngredient> output)
    {
        return new VivisectionDisplay(Collections.singletonList(EntryIngredients.of(item)), output);
    }

    public static Serializer<VivisectionDisplay> serializer()
    {
        return Serializer.ofSimpleRecipeLess(VivisectionDisplay::new);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.VIVISECTION;
    }

    public List<Component> getEntities()
    {
        return entities;
    }
}
