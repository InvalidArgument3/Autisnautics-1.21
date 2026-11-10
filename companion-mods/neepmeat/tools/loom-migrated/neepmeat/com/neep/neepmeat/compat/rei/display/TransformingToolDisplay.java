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

import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.plc.recipe.TransformingToolRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import java.util.List;

public class TransformingToolDisplay implements Display
{
    private final TransformingToolRecipe recipe;

    public TransformingToolDisplay(TransformingToolRecipe recipe)
    {
        this.recipe = recipe;
    }

    public static DisplaySerializer<TransformingToolDisplay> serializer()
    {
        return new Serializer();
    }

    @Override
    public List<EntryIngredient> getInputEntries()
    {
        // Jank!
        return List.of(
                EntryIngredients.of(NMItems.TRANSFORMING_TOOL_BASE),
                EntryIngredients.of(NMFluids.WORK_FLUID.still(), FluidConstants.BUCKET));
    }

    @Override
    public List<EntryIngredient> getOutputEntries()
    {
        return List.of();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.TRANSFORMING_TOOL;
    }

    public Item getBase()
    {
        return recipe.getBase();
    }

    public ManufactureStep<?> getStep()
    {
        return recipe.getSteps().get(0);
    }

    static class Serializer implements DisplaySerializer<TransformingToolDisplay>
    {
        @Override
        public CompoundTag save(CompoundTag tag, TransformingToolDisplay display)
        {
            return tag;
        }

        @Override
        public TransformingToolDisplay read(CompoundTag tag)
        {
            return new TransformingToolDisplay(TransformingToolRecipe.getInstance());
        }
    }
}
