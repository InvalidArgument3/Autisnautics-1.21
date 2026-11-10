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

import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.recipe.RecipeRegistry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.plc.component.MutateInPlace;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.Entity;

public class PLCRecipes
{
    public static final MeatRecipeSerialiser<ItemManufactureRecipe> MANUFACTURE_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "manufacture", new ItemManufactureRecipe.Serialiser());
    public static final MeatRecipeType<AbstractItemManufactureRecipe> MANUFACTURE = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "manufacture");
    public static final MeatRecipeSerialiser<ItemToEntityRecipe> ITEM_TO_ENTITY_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "item_to_entity", new ItemToEntityRecipe.Serialiser());
    public static final MeatRecipeType<ItemToEntityRecipe> ITEM_TO_ENTITY = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "item_to_entity");
    public static final MeatRecipeSerialiser<EntityImplantRecipe> ENTITY_MANUFACTURE_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "entity_manufacture", new EntityImplantRecipe.Serialiser());
    public static final MeatRecipeType<EntityImplantRecipe> ENTITY_MANUFACTURE = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "entity_manufacture");
    public static final MeatRecipeSerialiser<EntityToItemRecipe> ENTITY_TO_ITEM_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "entity_to_item", new EntityToItemRecipe.Serialiser());
    public static final MeatRecipeType<EntityToItemRecipe> ENTITY_TO_ITEM = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "entity_to_item");

    public static final ManufactureStep.Provider<?> COMBINE = ManufactureStep.register(CombineStep.ID, ManufactureStep.Provider.of(CombineStep::get, CombineStep.CODEC));
    public static final ManufactureStep.Provider<?> INJECT = ManufactureStep.register(InjectStep.ID, ManufactureStep.Provider.of(InjectStep::new, InjectStep.CODEC));
    public static final ManufactureStep.Provider<?> IMPLANT = ManufactureStep.register(ImplantStep.ID, ManufactureStep.Provider.of(ImplantStep::new, ImplantStep.CODEC));

    public static <B, C, T extends ManufactureRecipe<C, B>> boolean isValidStep(MeatRecipeType<T> type, Workpiece workpiece, ManufactureStep<?> nextStep, B base)
    {
        // TODO: Somehow remove jank
        if (base == NMItems.TRANSFORMING_TOOL_BASE)
            return true;

        List<ManufactureStep<?>> steps = workpiece.getSteps();
        for (var recipe : MeatlibRecipes.getInstance().getAllOfTypeSafe(type).values())
        {
            if (!Objects.equals(recipe.getBase(), base))
                continue;

            List<ManufactureStep<?>> recipeSteps = recipe.getSteps();
            int nextStepIdx = steps.size();
            if (recipeSteps.size() < steps.size() + 1)
                continue;

            if (ManufactureStep.matches(recipeSteps.get(nextStepIdx), nextStep))
                return true;
        }

        return false;
    }

    @Nullable
    public static <T extends EntityMutateRecipe> T getPartialMatch(MeatRecipeType<T> type, Workpiece workpiece, ManufactureStep<?> nextStep, MutateInPlace<Entity> mip)
    {
        List<ManufactureStep<?>> steps = workpiece.getSteps();
        for (var recipe : MeatlibRecipes.getInstance().getAllOfTypeSafe(type).values())
        {
            if (!recipe.getBase().test(mip.get()))
                continue;

            List<ManufactureStep<?>> recipeSteps = recipe.getSteps();
            int nextStepIdx = steps.size();
            if (recipeSteps.size() < steps.size() + 1)
                continue;

            if (ManufactureStep.matches(recipeSteps.get(nextStepIdx), nextStep))
                return recipe;
        }

        return null;
    }

    ///  Checks both entity manufacture and implanting
    @Nullable
    public static EntityMutateRecipe matchEntityMutate(MutateInPlace<Entity> mip)
    {
        EntityMutateRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(PLCRecipes.ENTITY_MANUFACTURE, mip).orElse(null);

        if (recipe != null)
            return recipe;

        recipe = MeatlibRecipes.getInstance().getFirstMatch(PLCRecipes.ENTITY_TO_ITEM, mip).orElse(null);

        return recipe;
    }

//    public static <B, C, T extends ManufactureRecipe<C>> boolean isValidStepSuffix(MeatRecipeType<T> type, Workpiece workpiece, ManufactureStep<?> nextStep, B base)
//    {
//        List<ManufactureStep<?>> steps = workpiece.getSteps();
//        for (var recipe : MeatRecipeManager.getInstance().getAllOfType(type).values())
//        {
//            if (!Objects.equals(recipe.getBase(), base))
//                continue;
//
//            List<ManufactureStep<?>> recipeSteps = recipe.getSteps();
//            int nextStepIdx = steps.size();
//            if (recipeSteps.size() < steps.size() + 1)
//                continue;
//
//            if (ManufactureStep.equals(recipeSteps.get(nextStepIdx), nextStep))
//                return true;
//        }
//
//        return false;
//    }

    public static void init()
    {

//        MutateInPlace.ANY.registerFallback((world, pos, state, blockEntity, context) ->
//        {
//
//        });
    }
}
