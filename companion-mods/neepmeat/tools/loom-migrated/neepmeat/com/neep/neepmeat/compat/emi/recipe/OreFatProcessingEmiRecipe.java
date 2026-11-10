package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.init.NMBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class OreFatProcessingEmiRecipe implements EmiRecipe
{
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final OreFatRegistry.Entry entry;

    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final ItemVariant output;

    public static OreFatProcessingEmiRecipe ofItemVariants(EmiRecipeCategory category, Collection<ItemVariant> input, OreFatRegistry.Entry entry)
    {
        return new OreFatProcessingEmiRecipe(category, input.stream().map(EmiIngredientHelper::variantToIngredient).toList(), entry);
    }

    public static OreFatProcessingEmiRecipe ofItems(EmiRecipeCategory category, Collection<Item> input, OreFatRegistry.Entry entry)
    {
        return new OreFatProcessingEmiRecipe(category, input.stream().map(i -> (EmiIngredient) EmiStack.of(i)).toList(), entry);
    }

    public OreFatProcessingEmiRecipe(EmiRecipeCategory category, List<EmiIngredient> inputs, OreFatRegistry.Entry entry)
    {
        this.category = category;
        this.entry = entry;

        this.inputs = inputs;
        this.output = entry.result();
        this.outputs = List.of(EmiStack.of(output.getItem(), output.getNbt()));

        this.id = new ResourceLocation(NeepMeat.NAMESPACE, "/" + output.getItem().builtInRegistryHolder().key().location().toString().replace(':', '_'));
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return outputs;
    }

    @Override
    public int getDisplayWidth()
    {
        return 160;
    }

    @Override
    public int getDisplayHeight()
    {
        return 70;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int w = 140;
        int startX = (getDisplayWidth() - w) / 2;
        int startY = 7;
        int fatX = startX + 54;
        int pressX = fatX + 20;
        int outX = pressX + 26;

        int cleanY = startY + 40;

        widgets.addSlot(EmiIngredient.of(inputs), startX, startY);
        FluidVariant dirty = entry.getDirty();
        FluidVariant clean = entry.getClean();

        addArrow(NMBlocks.CRUCIBLE, startX + 20, startY, widgets);

        widgets.addTexture(GUIUtil.WIDGETS, fatX, startY + 20, 18, 18, 0, 0, 18, 18, 128, 128);
        widgets.addSlot(EmiStack.of(NMBlocks.SMALL_TROMMEL), fatX, startY + 20).drawBack(false);

        widgets.addSlot(EmiStack.of(dirty.getFluid(), dirty.getNbt()), fatX, startY);
        addArrow(NMBlocks.HYDRAULIC_PRESS, pressX, startY, widgets);
        addOutputs(outputs.get(0), entry.renderingYield(), outX, startY, widgets);

        widgets.addSlot(EmiStack.of(clean.getFluid(), clean.getNbt()), fatX, cleanY);
        addArrow(NMBlocks.HYDRAULIC_PRESS, pressX, cleanY, widgets);
        addOutputs(outputs.get(0), entry.renderingYield() * entry.trommelYield(), outX, cleanY, widgets);

    }

    private void addArrow(ItemLike process, int x, int y, WidgetHolder widgets)
    {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y);
        widgets.addSlot(EmiStack.of(process), x + 3, y).drawBack(false);
    }

    private void addOutputs(EmiIngredient ingredient, float yield, int x, int y, WidgetHolder widgets)
    {
        long base = (long) Math.floor(yield);
        float extra = yield - base;
        widgets.addSlot(ingredient.copy().setAmount(base), x, y).recipeContext(this);
        if (extra > 0)
        {
            widgets.addSlot(ingredient.copy().setChance(extra), x + 20, y).recipeContext(this);
        }
    }
}
