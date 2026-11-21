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

package com.neep.neepmeat.api.processing;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMFluids;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// All of this code is very insane. Hopefully I'll make it not insane one day.
@SuppressWarnings("UnstableApiUsage")
public class MeatFluidUtil
{
    public static final String KEY_ROOT = NeepMeat.NAMESPACE + ":food";
    protected static final String KEY_HUNGER = "hunger";
    protected static final String KEY_SATURATION = "saturation";
    public static final int MAX_HUNGER = 10;

    public static final int EMPTY_COL = 0x301c05;
    public static final int FULL_COL = 0xffc596;

    public static float getHunger(FluidVariant variant)
    {
        CompoundTag root = getRoot(variant);
        if (root != null)
        {
            return Mth.clamp(root.getFloat(KEY_HUNGER), 0, MAX_HUNGER);
        }
        return 0;
    }

    public static float getHunger(@Nullable CompoundTag nbt)
    {
        if (nbt != null)
        {
            CompoundTag root = getRoot(nbt);
            return root.getFloat(KEY_HUNGER);
        }
        return 0;
    }

    public static void setHunger(@NotNull CompoundTag nbt, float hunger)
    {
        CompoundTag root = getOrCreateRoot(nbt);
        root.putFloat(KEY_HUNGER, hunger);
    }

    public static void setSaturation(@Nullable CompoundTag nbt, float saturation)
    {
        if (nbt != null)
        {
            CompoundTag root = getOrCreateRoot(nbt);
            root.putFloat(KEY_SATURATION, saturation);
        }
    }

    public static float getSaturation(@Nullable FluidVariant variant)
    {
        CompoundTag root = getRoot(variant);
        if (root != null)
        {
            return  root.getFloat(KEY_SATURATION);
        }
        return 0;
    }

    public static float getSaturation(@Nullable CompoundTag nbt)
    {
        if (nbt != null)
        {
            CompoundTag root = getOrCreateRoot(nbt);
            return root.getFloat(KEY_SATURATION);
        }
        return 0;
    }


    @Nullable
    public static CompoundTag getRoot(@NotNull FluidVariant variant)
    {
        if (variant.hasNbt())
            return variant.getNbt().getCompound(KEY_ROOT);
        return new CompoundTag();
    }

    public static CompoundTag getRoot(@NotNull CompoundTag nbt)
    {
        if (nbt.contains(KEY_ROOT, CompoundTag.TAG_COMPOUND))
        {
            return nbt.getCompound(KEY_ROOT);
        }
        return new CompoundTag();
    }

    @NotNull
    public static CompoundTag getRootOrNew(@Nullable CompoundTag nbt)
    {
        CompoundTag root;
        if (nbt != null && (root = getRoot(nbt)) != null)
        {
            return root;
        }
        return new CompoundTag();
    }

    @Contract(mutates = "param1")
    protected static CompoundTag getOrCreateRoot(CompoundTag nbt)
    {
        if (nbt.contains(KEY_ROOT, CompoundTag.TAG_COMPOUND))
        {
            return nbt.getCompound(KEY_ROOT);
        }
        else
        {
            CompoundTag newRoot = new CompoundTag();
            nbt.put(KEY_ROOT, newRoot);
            return newRoot;
        }
    }

    public static int getColour(FluidVariant variant)
    {
        float hunger = getHunger(variant);
        float f = hunger / MAX_HUNGER;
        int r = (int) org.joml.Math.lerp((EMPTY_COL >> 16) & 0xFF, (FULL_COL >> 16) & 0xFF, f);
        int g = (int) org.joml.Math.lerp((EMPTY_COL >> 8) & 0xFF, (FULL_COL >> 8) & 0xFF, f);
        int b = (int) org.joml.Math.lerp((EMPTY_COL) & 0xFF, (FULL_COL) & 0xFF, f);
        return (r << 16) | (g << 8) | b;
    }

    public static FluidVariant getVariant(FoodProperties food)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(KEY_HUNGER, food.getNutrition());
        nbt.putFloat(KEY_SATURATION, food.getSaturationModifier());
        CompoundTag root = new CompoundTag();
        root.put(KEY_ROOT, nbt);
        return FluidVariant.of(com.neep.neepmeat.fluid.FluidForms.flowing(NMFluids.FOOD), root);
    }

    private static float floor(float f, int n)
    {
        return (float) (Math.floor(f * Math.pow(10f, n)) / (float) Math.pow(10, n));
    }

    /** Copies the root tag from the old tag to the new one without touching unrelated entries.
     * Intended for transferring meat information from a FluidVariant to an ItemVariant.
     *
     * Hunger and saturation are rounded down to the nearest 1 and 0.1 respectively.
     */
    public static CompoundTag copyRootRounded(CompoundTag nbt, CompoundTag oldNbt)
    {
        if (oldNbt != null)
        {
            CompoundTag root = oldNbt.getCompound(KEY_ROOT).copy();
            root.putFloat(KEY_HUNGER, floor(root.getFloat(KEY_HUNGER), 0));
            root.putFloat(KEY_SATURATION, floor(root.getFloat(KEY_SATURATION), 1));
            nbt.put(KEY_ROOT, root);
        }
        return nbt;
    }

    public static void copyRoot(CompoundTag from, CompoundTag to)
    {
        CompoundTag root = getRoot(from);
        if (root != null)
        {
            to.put(KEY_ROOT, root.copy());
        }
        else
        {
            to.put(KEY_ROOT, new CompoundTag());
        }
    }
}
