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

package com.neep.neepmeat.plc.recipe;

import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.plc.component.MutateInPlace;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class EntityMutateRecipe implements ManufactureRecipe<MutateInPlace<Entity>, EntityRecipeInput>
{
    protected final ResourceLocation id;
    protected final EntityRecipeInput base;
    protected final List<ManufactureStep<?>> steps;

    protected EntityMutateRecipe(ResourceLocation id, EntityRecipeInput base, List<ManufactureStep<?>> steps)
    {
        this.id = id;
        this.base = base;
        this.steps = steps;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }
}
