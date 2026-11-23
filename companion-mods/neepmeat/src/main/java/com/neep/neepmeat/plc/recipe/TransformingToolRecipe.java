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
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.item.util.TransformingTools;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

public class TransformingToolRecipe implements ManufactureRecipe<MutateInPlace<ItemStack>, Item>
{
    private static final TransformingToolRecipe INSTANCE = new TransformingToolRecipe();

    public static TransformingToolRecipe getInstance()
    {
        return INSTANCE;
    }

    private final Item base = NMItems.TRANSFORMING_TOOL_BASE;
    private final ManufactureStep<?> step = new InjectStep(NMFluids.WORK_FLUID.variant());
    private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "transforming_tool");

    @Override
    public boolean matches(MutateInPlace<ItemStack> context)
    {
        ItemStack stack = context.get();

        if (stack != null && stack.is(NMItems.TRANSFORMING_TOOL_BASE))
        {
            var workpiece = NMComponents.WORKPIECE.getNullable(stack);
            if (workpiece != null)
            {
                if (workpiece.getSteps().size() != 3)
                    return false;

                boolean b0 = workpiece.getSteps().get(0) instanceof CombineStep;
                boolean b1 = workpiece.getSteps().get(1) instanceof CombineStep;
                boolean b2 = workpiece.getSteps().get(2) instanceof InjectStep inject
                        && inject.fluid().getObject().isSame(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.WORK_FLUID));

                return (b0 && b1 && b2);
            }
        }
        return false;
    }

    @Override
    public boolean takeInputs(MutateInPlace<ItemStack> context, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public boolean ejectOutputs(MutateInPlace<ItemStack> context, TransactionContext transaction)
    {
        Workpiece workpiece = NMComponents.WORKPIECE.get(context.get());

        ItemVariant variant0 = ((CombineStep) workpiece.getSteps().get(0)).item();
        ItemVariant variant1 = ((CombineStep) workpiece.getSteps().get(1)).item();

        ItemVariant combined = TransformingTools.combine(variant0, variant1);
        if (combined == null)
        {
            return false;
        }

        context.setResult(1, combined.toStack(1));
        return true;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        throw new NotImplementedException();
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        throw new NotImplementedException();
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public Item getBase()
    {
        return base;
    }

    @Override
    public List<ManufactureStep<?>> getSteps()
    {
        return List.of(step);
    }
}
