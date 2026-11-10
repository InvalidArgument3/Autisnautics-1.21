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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class ItemToEntityRecipe extends AbstractItemManufactureRecipe
{
    private final EntityType<?> output;

    public ItemToEntityRecipe(ResourceLocation id, Item base, EntityType<?> output, List<ManufactureStep<?>> steps)
    {
        super(id, base, steps);
        this.output = output;
    }

    @Override
    public boolean ejectOutputs(MutateInPlace<ItemStack> context, TransactionContext transaction)
    {
        // TODO
//        ItemStack outputStack = new ItemStack(output.resource(), Math.toIntExact(output.randomAmount(0)));
//        outputStack.setNbt(output.getNbt());
        var entity = output.create(context.getWorld());
        if (entity != null)
        {
            Vec3 pos = context.getOutputPos().getCenter();
            entity.setPos(pos);
            entity.setPosRaw(pos.x, pos.y, pos.z);
            context.getWorld().addFreshEntity(entity);
        }
        context.setResult(1, ItemStack.EMPTY);
        return true;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return PLCRecipes.ITEM_TO_ENTITY_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return PLCRecipes.MANUFACTURE;
    }

    public EntityType<?> getOutput()
    {
        return output;
    }

    public static class Serialiser implements MeatRecipeSerialiser<ItemToEntityRecipe>
    {
        private static final Codec<RecipeFunction<ItemToEntityRecipe>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").fieldOf("base").forGetter(MeatLibCodecs::notImplemented),
                ManufactureStep.LIST_CODEC.fieldOf("steps").forGetter(MeatLibCodecs::notImplemented),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").fieldOf("result").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (base, steps, output) -> id -> new ItemToEntityRecipe(id, base, output, steps)));

        @Override
        public Codec<RecipeFunction<ItemToEntityRecipe>> codec()
        {
            return CODEC;
        }

        @Override
        public ItemToEntityRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            Item base = buf.readById(BuiltInRegistries.ITEM);
            List<ManufactureStep<?>> steps = ItemManufactureRecipe.Serialiser.readSteps(buf);
            EntityType<?> output = buf.readById(BuiltInRegistries.ENTITY_TYPE);

            return new ItemToEntityRecipe(id, base, output, steps);
        }

        @Override
        public void write(FriendlyByteBuf buf, ItemToEntityRecipe recipe)
        {
            buf.writeId(BuiltInRegistries.ITEM, recipe.base);
            ItemManufactureRecipe.Serialiser.writeSteps(recipe.steps, buf);
            buf.writeId(BuiltInRegistries.ENTITY_TYPE, recipe.output);
        }
    }
}