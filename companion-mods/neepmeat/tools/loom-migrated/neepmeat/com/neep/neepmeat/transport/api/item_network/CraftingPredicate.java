package com.neep.neepmeat.transport.api.item_network;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import java.util.function.Predicate;

public interface CraftingPredicate extends Predicate<ItemVariant>
{
    CompoundTag writeNbt(CompoundTag nbt);

    static Predicate<ItemVariant> ingredient(net.minecraft.world.item.crafting.Ingredient ingredient)
    {
        return new Ingredient(ingredient);
    }

    static Predicate<ItemVariant> equals(ItemVariant variant)
    {
        return new Equals(variant);
    }

    /// Ingredients can't be safely serialised to NBT, so only Equals is supported,
    static CraftingPredicate fromNbt(CompoundTag nbt)
    {
        var predicate = new Equals(ItemVariant.fromNbt(nbt));
        if (predicate.resource.isBlank())
        {
            // Oh no
        }

        return predicate;
    }

    record Ingredient(net.minecraft.world.item.crafting.Ingredient ingredient) implements CraftingPredicate
    {
        @Override
        public boolean test(ItemVariant variant)
        {
            return ingredient.test(variant.toStack());
        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            // NOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
            // I should throw an unsupported operation exception here, but I've learned not to do this
            return new CompoundTag();
        }
    }

    record Equals(ItemVariant resource) implements CraftingPredicate
    {
        @Override
        public boolean test(ItemVariant variant)
        {
            return resource.equals(variant);
        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            return resource.toNbt();
        }
    }

    static String format(Predicate<ItemVariant> predicate)
    {
        if (predicate instanceof Ingredient ingredient)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            var matchingStacks = ingredient.ingredient.getItems();
            for (int i = 0; i < matchingStacks.length; i++)
            {
                var matchingStack = matchingStacks[i];

                sb.append(BuiltInRegistries.ITEM.getKey(matchingStack.getItem()));

                if (i != matchingStacks.length - 1)
                    sb.append(",");

                if (i >= 2)
                {
                    sb.append("...");
                    break;
                }

            }

            sb.append("]");

            return sb.toString();
        }
        else if (predicate instanceof Equals equals)
        {
            return BuiltInRegistries.ITEM.getKey(equals.resource.getItem()).toString();
        }

        return "unknown matcher";
    }
}
