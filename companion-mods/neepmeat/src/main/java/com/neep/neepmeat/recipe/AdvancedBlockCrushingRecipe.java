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

package com.neep.neepmeat.recipe;

import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AdvancedBlockCrushingRecipe extends BlockCrushingRecipe
{
    public static final AdvancedBlockCrushingRecipe INSTANCE = new AdvancedBlockCrushingRecipe(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "advanced_block_crushing/instance"),
            1, 1, 0.7f);

    public AdvancedBlockCrushingRecipe(ResourceLocation id, long mainAmount, long extraAmount, float outputChance)
    {
        super(id, mainAmount, extraAmount, outputChance);
    }

    @Nullable
    @Override
    protected BlockCrushingRegistry.Entry getFromInput(ItemVariant input)
    {
        return BlockCrushingRegistry.INSTANCE.getFromInputAdvanced(input);
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.ADVANCED_CRUSHING;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.ADVANCED_BLOCK_CRUSHING_SERIALIZER;
    }
}
