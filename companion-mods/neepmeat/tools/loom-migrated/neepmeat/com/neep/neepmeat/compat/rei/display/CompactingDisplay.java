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
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import java.util.Arrays;
import java.util.List;

public class CompactingDisplay extends BasicDisplay
{
    private int page;

    public static CompactingDisplay of(List<Item> inputs, List<EntryIngredient> output, int page)
    {
        EntryIngredient[] inputIngredients = new EntryIngredient[inputs.size()];
        int i = 0;
        for (ItemLike entry : inputs)
        {
            inputIngredients[i] = EntryIngredients.of(entry);
            i++;
        }
        return new CompactingDisplay(Arrays.asList(inputIngredients), output, page);
    }

    public CompactingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, CompoundTag tag)
    {
        this(inputs, outputs, tag.getInt("page"));
    }

    public CompactingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, int page)
    {
        super(inputs, outputs);
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public static BasicDisplay.Serializer<CompactingDisplay> serializer()
    {
        return BasicDisplay.Serializer.ofRecipeLess(CompactingDisplay::new, (display, tag) ->
                tag.putInt("page", display.page));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.COMPACTING;
    }
}
