/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Direct recipe test used by MeatlibRecipes.getFirstMatch.
 */

package com.neep.meatlib.recipe;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface RecipeContextMatch {
    boolean matches(Object input, @Nullable Level level);
}
