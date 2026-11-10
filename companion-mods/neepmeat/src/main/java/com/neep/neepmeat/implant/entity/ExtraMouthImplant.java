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

package com.neep.neepmeat.implant.entity;

import com.google.common.collect.Lists;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.FoodCartonItem;
import com.neep.neepmeat.item.filter.FilterList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class ExtraMouthImplant implements EntityImplant
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "extra_mouth");
    protected static final int MAX_FOOD = 20;
    protected final Player player;
    protected final FilterList filterList;

    public ExtraMouthImplant(Entity entity)
    {
        this.player = (Player) entity;
        filterList = new FilterList(1, () -> {});
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {
        if (player.level().getGameTime() % 20 != 0) return;

        FoodData hungerManager = player.getFoodData();
        if (hungerManager.needsFood())
        {
            int foodLevel = hungerManager.getFoodLevel();
            IntObjectPair<ItemStack> stack = getFood(MAX_FOOD - foodLevel, hungerManager.getSaturationLevel(), player.getMaxHealth() - player.getHealth());
            if (stack != null)
            {
                eatFood(player, stack.value(), stack.keyInt());
            }
        }
    }

    @Nullable
    protected IntObjectPair<ItemStack> getFood(int emptyHunger, float saturation, float emptyHearts)
    {
        Container inventory = player.getInventory();
        List<IntObjectPair<ItemStack>> foodStacks = Lists.newArrayList();
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack stack = inventory.getItem(i);
            if (
                    stack.getRarity() == Rarity.COMMON  // Avoid eating golden apples and expensive things
                    && stack.has(net.minecraft.core.component.DataComponents.FOOD)
                    && stack.getFoodProperties(null) != null
                    && filterList.matches(ItemVariant.of(stack)))

            {
                foodStacks.add(IntObjectPair.of(i, stack));
            }
        }

        if (foodStacks.isEmpty())
            return null;

        if (emptyHearts > 3)
        {
            // Prioritise high saturation if damaged
            foodStacks.sort(Comparator.<IntObjectPair<ItemStack>>comparingDouble(s -> s.value().getFoodProperties(null).getSaturationModifier()).reversed());

            return foodStacks.get(0);
        }
        else
        {
            foodStacks.sort(Comparator.<IntObjectPair<ItemStack>>comparingInt(s -> s.value().getFoodProperties(null).getNutrition()).reversed());

            for (var pair : foodStacks)
            {
                if (emptyHunger >= pair.value().getFoodProperties(null).getNutrition())
                {
                    return pair;
                }
            }
        }

        // If no small items are available, wait for empty hunger to drop below half of the smallest item's hunger.
        IntObjectPair<ItemStack> smallest = foodStacks.get(foodStacks.size() - 1);
        if (emptyHunger > smallest.value().getFoodProperties(null).getNutrition() / 2)
            return smallest;

        return null;
    }

    protected static void eatFood(Player player, ItemStack stack, int slot)
    {
        Item item = stack.getItem();

        // Hopefully nobody notices this.
        if (item instanceof FoodCartonItem meatCarton)
        {
            meatCarton.eatFood(player, player.level(), stack);
        }
        else
        {
            ItemStack remainder = stack.finishUsingItem(player.level(), player);
            if (remainder != stack)
            {
                player.getInventory().setItem(slot, remainder);
            }
        }
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        filterList.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        filterList.readNbt(nbt);
    }

    public FilterList getFilters()
    {
        return filterList;
    }
}
