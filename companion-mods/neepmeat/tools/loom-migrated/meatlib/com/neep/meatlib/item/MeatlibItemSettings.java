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

package com.neep.meatlib.item;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import java.util.Set;

// HAHAHHAHAHAHA I'M BRINGING BACK Settings::group! YOU CAN'T STOP ME!
public class MeatlibItemSettings extends FabricItemSettings
{
    public static final Set<TagKey<Item>> DEFAULT_TAGS = Set.of();

    public CreativeModeTab group = null;
    public boolean supportsGuideLookup = true; // For minimal extra boilerplate in item declarations, this is the default behaviour.
    private Set<TagKey<Item>> tags = DEFAULT_TAGS;

    public Set<TagKey<Item>> getTags()
    {
        return tags;
    }

    // Mostly taken from FabricItemSettings. Modified to return MeatlibItemSettings.

    public MeatlibItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider)
    {
        FabricItemInternals.computeExtraData(this).equipmentSlot(equipmentSlotProvider);
        return this;
    }

    public MeatlibItemSettings customDamage(CustomDamageHandler handler)
    {
        FabricItemInternals.computeExtraData(this).customDamage(handler);
        return this;
    }

    // Overrides of vanilla methods

    @Override
    public MeatlibItemSettings food(FoodProperties foodComponent)
    {
        super.food(foodComponent);
        return this;
    }

    @Override
    public MeatlibItemSettings stacksTo(int maxCount)
    {
        super.stacksTo(maxCount);
        return this;
    }

    @Override
    public MeatlibItemSettings defaultDurability(int maxDamage)
    {
        super.defaultDurability(maxDamage);
        return this;
    }

    @Override
    public MeatlibItemSettings durability(int maxDamage)
    {
        super.durability(maxDamage);
        return this;
    }

    @Override
    public MeatlibItemSettings craftRemainder(Item recipeRemainder)
    {
        super.craftRemainder(recipeRemainder);
        return this;
    }

    @Override
    public MeatlibItemSettings rarity(Rarity rarity)
    {
        super.rarity(rarity);
        return this;
    }

    @Override
    public MeatlibItemSettings fireResistant()
    {
        super.fireResistant();
        return this;
    }

    @Override
    public MeatlibItemSettings requiredFeatures(FeatureFlag... features)
    {
        super.requiredFeatures(features);
        return this;
    }

//    public MeatlibItemSettings tag(TagKey<Item> tag)
//    {
//        this.tags.add(tag);
//        return this;
//    }

    // I would like to use varargs here, but it gives an ugly warning.
//    public MeatlibItemSettings tag(List<TagKey<Item>> tags)
//    {
//        this.tags.addAll(tags);
//        return this;
//    }

    public MeatlibItemSettings group(CreativeModeTab group)
    {
        this.group = group;
        return this;
    }

    // Could use varargs, but that may be unsafe.
    public MeatlibItemSettings tags()
    {
        this.tags = Set.of();
        return this;
    }

    public MeatlibItemSettings tags(TagKey<Item> tags)
    {
        this.tags = Set.of(tags);
        return this;
    }

    public MeatlibItemSettings tags(Set<TagKey<Item>> tags)
    {
        this.tags = Set.copyOf(tags);
        return this;
    }

    public MeatlibItemSettings noLookup()
    {
        this.supportsGuideLookup = false;
        return this;
    }
}
