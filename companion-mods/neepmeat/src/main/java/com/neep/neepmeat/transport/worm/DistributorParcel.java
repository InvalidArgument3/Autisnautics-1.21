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

package com.neep.neepmeat.transport.worm;

import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public record DistributorParcel(List<ResourceAmount<ItemVariant>> items, List<ResourceAmount<FluidVariant>> fluids) {
    public static DistributorParcel fromNbt(CompoundTag compound) {
        return new DistributorParcel(readItems(compound.getList("items", Tag.TAG_COMPOUND)), readFluids(compound.getList("fluids", Tag.TAG_COMPOUND)));
    }

    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty();
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        ListTag itemList = new ListTag();
        for (ResourceAmount<ItemVariant> amount : items) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", BuiltInRegistries.ITEM.getKey(amount.resource().getItem()).toString());
            entry.putLong("amount", amount.amount());
            itemList.add(entry);
        }
        ListTag fluidList = new ListTag();
        for (ResourceAmount<FluidVariant> amount : fluids) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", BuiltInRegistries.FLUID.getKey(amount.resource().fluid()).toString());
            entry.putLong("amount", amount.amount());
            fluidList.add(entry);
        }
        nbt.put("items", itemList);
        nbt.put("fluids", fluidList);
        return nbt;
    }

    private static List<ResourceAmount<ItemVariant>> readItems(ListTag list) {
        List<ResourceAmount<ItemVariant>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation id = ResourceLocation.parse(entry.getString("id"));
            result.add(new ResourceAmount<>(ItemVariant.of(new ItemStack(BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR))), entry.getLong("amount")));
        }
        return result;
    }

    private static List<ResourceAmount<FluidVariant>> readFluids(ListTag list) {
        List<ResourceAmount<FluidVariant>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(entry.getString("id"))).orElse(Fluids.EMPTY);
            result.add(new ResourceAmount<>(FluidVariant.of(fluid), entry.getLong("amount")));
        }
        return result;
    }
}
