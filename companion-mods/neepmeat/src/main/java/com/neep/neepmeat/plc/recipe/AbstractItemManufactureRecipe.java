package com.neep.neepmeat.plc.recipe;

import com.neep.meatlib.mixin.ItemStackMixin;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public abstract class AbstractItemManufactureRecipe implements ManufactureRecipe<MutateInPlace<ItemStack>, Item>
{
    protected final ResourceLocation id;
    protected final Item base;
    protected final List<ManufactureStep<?>> steps;

    public AbstractItemManufactureRecipe(ResourceLocation id, Item base, List<ManufactureStep<?>> steps)
    {
        this.id = id;
        this.base = base;
        this.steps = steps;
    }

    @Override
    public Item getBase()
    {
        return base;
    }

    @Override
    public List<ManufactureStep<?>> getSteps()
    {
        return steps;
    }

    @Override
    public boolean matches(MutateInPlace<ItemStack> context)
    {
        ItemStack stack = context.get();

        if (stack == null || !stack.is(base))
            return false;

        var workpiece = NMComponents.WORKPIECE.maybeGet(stack).orElse(null);
        if (workpiece != null)
        {
            var workSteps = workpiece.getSteps();

            if (workSteps.size() != steps.size())
                return false;

            for (int i = 0; i < workSteps.size(); ++i)
            {
                var workStep = workSteps.get(i);

                if (!ManufactureStep.matches(steps.get(i), workStep))
                {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean takeInputs(MutateInPlace<ItemStack> context, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }
}
