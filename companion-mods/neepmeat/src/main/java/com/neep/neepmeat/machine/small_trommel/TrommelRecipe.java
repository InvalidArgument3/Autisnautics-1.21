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

package com.neep.neepmeat.machine.small_trommel;

import com.neep.meatlib.recipe.RecipeContextMatch;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface TrommelRecipe extends RecipeContextMatch {
    int INPUT_AMOUNT = 9000;

    boolean matches(TrommelStorage inventory);

    @Override
    default boolean matches(Object input, @Nullable Level level) {
        return input instanceof TrommelStorage storage && matches(storage);
    }

    boolean takeInputs(TrommelStorage storage, TransactionContext transaction);

    boolean ejectOutputs(TrommelStorage storage, TransactionContext transaction);

}
