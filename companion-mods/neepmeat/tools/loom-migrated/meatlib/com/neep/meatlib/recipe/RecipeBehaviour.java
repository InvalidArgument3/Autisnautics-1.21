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

package com.neep.meatlib.recipe;

import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.NbtSerialisable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class RecipeBehaviour<T extends MeatlibRecipe<?>> implements NbtSerialisable
{
    @Nullable protected T currentRecipe;

    @Nullable
    public T getCurrentRecipe()
    {
        return currentRecipe;
    }

    public void setRecipe(@Nullable T recipe)
    {
        this.currentRecipe = recipe;
    }

    public abstract void startRecipe(T recipe);

    public abstract void interrupt();

    public abstract void finishRecipe();

    @Override
    public CompoundTag writeNbt(CompoundTag tag)
    {
        if (currentRecipe != null)
            tag.putString("recipe", currentRecipe.getId().toString());

        return tag;
    }

    @Override
    public void readNbt(CompoundTag tag)
    {
        this.currentRecipe = MiscUtil.ifPresentOrNull(tag, "recipe", s ->
                (T) MeatlibRecipes.getInstance().get(ResourceLocation.tryParse(s)).orElse(null));
    }
}
