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

package com.neep.meatlib.recipe.ingredient.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class TypeEntityInput extends EntityRecipeInput
{
    private final EntityType<?> type;

    public TypeEntityInput(EntityType<?> type)
    {
        this.type = type;
    }

    @Override
    public boolean test(Entity entity)
    {
        return entity != null && type.equals(entity.getType());
    }

    public EntityType<?> getEntityType()
    {
        return type;
    }
}
