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
import com.neep.neepmeat.plc.recipe.CombineStep;
import com.neep.neepmeat.plc.recipe.ImplantStep;
import com.neep.neepmeat.plc.recipe.InjectStep;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public abstract class ManufactureDisplay<T> implements Display
{
    private final T base;
    protected final List<ManufactureStep<?>> steps;

    public ManufactureDisplay(T base, List<ManufactureStep<?>> steps)
    {
        this.base = base;
        this.steps = steps;
    }

    public T getBase()
    {
        return base;
    }

    protected static void appendStepIngredients(List<ManufactureStep<?>> steps, List<EntryIngredient> ingredients)
    {
        for (var step : steps)
        {
            if (step instanceof CombineStep combineStep)
            {
                ingredients.add(EntryIngredients.of(combineStep.getItem()));
            }
            else if (step instanceof InjectStep injectStep)
            {
                ingredients.add(EntryIngredients.of(injectStep.fluid().getFluid()));
            }
            else if (step instanceof ImplantStep implantStep)
            {
                ingredients.add(EntryIngredients.of(implantStep.getItem()));
            }
        }
    }

    public List<ManufactureStep<?>> getSteps()
    {
        return steps;
    }
}
