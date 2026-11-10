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
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.plc.recipe.ItemManufactureRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.SimpleDisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.Collections;
import java.util.List;

public class ItemManufactureDisplay extends ManufactureDisplay<Item>
{
    private final List<EntryIngredient> outputs;
    private final List<EntryIngredient> inputs;

    public ItemManufactureDisplay(ItemManufactureRecipe recipe)
    {
        super(recipe.getBase(), recipe.getSteps());
        this.outputs = Collections.singletonList(EntryIngredients.ofItems(List.of(recipe.getRecipeOutput().resource()), (int) recipe.getRecipeOutput().minAmount()));

        this.inputs = Lists.newArrayList();
        inputs.add(EntryIngredients.of(recipe.getBase()));
        appendStepIngredients(steps, inputs);
    }

    public ItemManufactureDisplay(Item base, List<ManufactureStep<?>> steps, List<EntryIngredient> output)
    {
        super(base, steps);
        this.outputs = output;

        this.inputs = Lists.newArrayList();
        inputs.add(EntryIngredients.of(base));
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
        return NMREIPlugin.ITEM_MANUFACTURE;
    }

    public static DisplaySerializer<ItemManufactureDisplay> getSerializer()
    {
        // TODO: Work out why this is null and why it doesn't matter
        return null;
    }

    private class Serializer implements SimpleDisplaySerializer<ItemManufactureDisplay>
    {
        @Override
        public CompoundTag save(CompoundTag tag, ItemManufactureDisplay display)
        {
            tag.put("output", EntryIngredients.save(getOutputIngredients(display)));
            tag.putString("base", BuiltInRegistries.ITEM.getKey(ItemManufactureDisplay.this.getBase()).toString());
            ListTag steps = new ListTag();

            for (var step : display.getSteps())
            {
                CompoundTag stepNbt = new CompoundTag();
                stepNbt.putString("id", step.getId().toString());
                stepNbt.put("step", step.toNbt());
            }

            tag.put("steps", steps);
            return tag;
        }

        @Override
        public CompoundTag saveExtra(CompoundTag tag, ItemManufactureDisplay display)
        {
            return tag;
        }

        @Override
        public ItemManufactureDisplay read(CompoundTag tag)
        {
            List<EntryIngredient> output = EntryIngredients.read(tag.getList("output", Tag.TAG_COMPOUND));

            Item base = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("base")));

            List<ManufactureStep<?>> steps = Lists.newArrayList();
            ListTag nbtSteps = tag.getList("steps", Tag.TAG_COMPOUND);
            for (int i = 0; i < nbtSteps.size(); ++i)
            {
                CompoundTag stepNbt = nbtSteps.getCompound(i);
                ResourceLocation id = ResourceLocation.tryParse(stepNbt.getString("id"));
                if (id == null)
                    throw new IllegalStateException();

                ManufactureStep<?> step = ManufactureStep.REGISTRY.get(id).create(stepNbt.getCompound("step"));
                steps.add(step);
            }

            return new ItemManufactureDisplay(base, steps, output);
        }
    }
}
