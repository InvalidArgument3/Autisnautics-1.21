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

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RecipeOutputImpl;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class ItemManufactureRecipe extends AbstractItemManufactureRecipe
{
    protected final RecipeOutput<Item> output;

    public ItemManufactureRecipe(ResourceLocation id, Item base, RecipeOutput<Item> output, List<ManufactureStep<?>> steps)
    {
        super(id, base, steps);
        this.output = output;
    }

    @Override
    public boolean ejectOutputs(MutateInPlace<ItemStack> context, TransactionContext transaction)
    {
        ItemStack outputStack = new ItemStack(output.resource(), Math.toIntExact(output.randomAmount(0)));
        outputStack.setTag(output.getNbt());
        context.setResult(1, outputStack);
        return true;
    }

    public RecipeOutput<Item> getRecipeOutput()
    {
        return output;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return PLCRecipes.MANUFACTURE;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return PLCRecipes.MANUFACTURE_SERIALISER;
    }

    public static class Serialiser implements MeatRecipeSerialiser<ItemManufactureRecipe>
    {
        private final Codec<RecipeFunction<ItemManufactureRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").fieldOf("base").forGetter(MeatLibCodecs::notImplemented),
                ManufactureStep.LIST_CODEC.fieldOf("steps").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.ITEM.fieldOf("result").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (b, s, r) -> id -> new ItemManufactureRecipe(id, b, r, s)));

        @Override
        public Codec<RecipeFunction<ItemManufactureRecipe>> codec()
        {
            return codec;
        }

        public static void writeSteps(List<ManufactureStep<?>> steps, FriendlyByteBuf buf)
        {
            buf.writeInt(steps.size());
            for (var step : steps)
            {
                buf.writeNbt(step.toNbt());
                buf.writeId(ManufactureStep.REGISTRY, ManufactureStep.REGISTRY.get(step.getId()));
            }
        }

        public static List<ManufactureStep<?>> readSteps(FriendlyByteBuf buf)
        {
            int size = buf.readInt();
            List<ManufactureStep<?>> steps = Lists.newArrayList();
            for (int i = 0; i < size; ++i)
            {
                CompoundTag data = buf.readNbt();

                var step = buf.readById(ManufactureStep.REGISTRY).create(data);
                steps.add(step);
            }
            return steps;
        }

        @Override
        public ItemManufactureRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            Item base = buf.readById(BuiltInRegistries.ITEM);

            List<ManufactureStep<?>> steps = readSteps(buf);

            RecipeOutput<Item> output = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            return new ItemManufactureRecipe(id, base, output, steps);
        }

        @Override
        public void write(FriendlyByteBuf buf, ItemManufactureRecipe recipe)
        {
            buf.writeId(BuiltInRegistries.ITEM, recipe.base);

            writeSteps(recipe.getSteps(), buf);

            recipe.output.write(buf);
        }
    }
}
