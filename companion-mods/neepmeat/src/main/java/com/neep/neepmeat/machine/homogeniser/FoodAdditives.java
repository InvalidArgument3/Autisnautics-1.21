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

package com.neep.neepmeat.machine.homogeniser;

import com.google.common.collect.Maps;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import com.neep.neepmeat.init.NMFluids;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class FoodAdditives
{
    private static final FoodAdditives INSTANCE = new FoodAdditives();

    public static FoodAdditives getInstance()
    {
        return INSTANCE;
    }

    private final Map<Item, Entry> map = Maps.newHashMap();

    public static void register(Item item, Entry entry)
    {
        getInstance().map.put(item, entry);
    }

    public Entry get(Item item)
    {
        return map.get(item);
    }

    public static void init()
    {
        register(Items.SUGAR, new HungerAdditive(1, FluidConstants.BUCKET));
    }

    public interface Entry
    {
        long getAmount();
        boolean canApply(FluidVariant variant);

        FluidVariant apply(FluidVariant variant);
    }

    public static class HungerAdditive implements Entry
    {
        private final float hunger;
        private final long amount;

        public HungerAdditive(float hunger, long amount)
        {
            this.hunger = hunger;
            this.amount = amount;
        }

        @Override
        public long getAmount()
        {
            return amount;
        }

        @Override
        public boolean canApply(FluidVariant variant)
        {
            return variant.isOf(NMFluids.FOOD.still());
        }

        @Override
        public FluidVariant apply(FluidVariant variant)
        {
            CompoundTag newNbt = new CompoundTag();
            if (MeatFluidUtil.getRoot(variant) != null)
            {
                MeatFluidUtil.copyRoot(variant.getNbt(), newNbt);
            }

            MeatFluidUtil.setHunger(newNbt, MeatFluidUtil.getHunger(variant) + hunger);


            return FluidVariant.of(variant.getFluid(), newNbt);
        }
    }

}
