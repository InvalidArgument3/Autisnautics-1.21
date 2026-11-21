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
 *
 * Upstream extended Fabric's item settings. This is Item.Properties, so it can be passed where 1.21 expects Properties.
 */

package com.neep.meatlib.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Set;

public class MeatlibItemSettings extends Item.Properties {
    public static final Set<TagKey<Item>> DEFAULT_TAGS = Set.of();

    public CreativeModeTab group;
    public boolean supportsGuideLookup = true;
    private Set<TagKey<Item>> tags = DEFAULT_TAGS;

    public Set<TagKey<Item>> getTags() {
        return tags;
    }

    @Override
    public MeatlibItemSettings food(FoodProperties food) {
        super.food(food);
        return this;
    }

    @Override
    public MeatlibItemSettings stacksTo(int maxCount) {
        super.stacksTo(maxCount);
        return this;
    }

    @Override
    public MeatlibItemSettings durability(int maxDamage) {
        super.durability(maxDamage);
        return this;
    }

    @Override
    public MeatlibItemSettings craftRemainder(Item remainder) {
        super.craftRemainder(remainder);
        return this;
    }

    @Override
    public MeatlibItemSettings rarity(Rarity rarity) {
        super.rarity(rarity);
        return this;
    }

    @Override
    public MeatlibItemSettings fireResistant() {
        super.fireResistant();
        return this;
    }

    public MeatlibItemSettings group(CreativeModeTab group) {
        this.group = group;
        return this;
    }

    public MeatlibItemSettings tags(TagKey<Item> tag) {
        this.tags = Set.of(tag);
        return this;
    }

    public MeatlibItemSettings tags(Set<TagKey<Item>> tags) {
        this.tags = Set.copyOf(tags);
        return this;
    }

    public MeatlibItemSettings noLookup() {
        this.supportsGuideLookup = false;
        return this;
    }
}
