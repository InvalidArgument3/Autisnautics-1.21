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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.SlotType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import static com.neep.meatweapons.meatgun.AmmunitionProvider.F;

public class AttributeContainer
{
    public final EnumMap<MeatgunModule.Attribute, Float> map = new EnumMap<>(MeatgunModule.Attribute.class);
    public final EnumSet<SlotType> applicable = EnumSet.copyOf(SlotType.ALL_SLOTS);

    public AttributeContainer add(MeatgunModule.Attribute attribute, float value)
    {
        map.put(attribute, value);
        return this;
    }

    public AttributeContainer complexity(int value)
    {
        map.put(MeatgunModule.Attribute.COMPLEXITY, (float) value);
        return this;
    }

    public AttributeContainer cooldown(int value)
    {
        map.put(MeatgunModule.Attribute.COOLDOWN, (float) value);
        return this;
    }

    public AttributeContainer attackDamage(float value)
    {
        map.put(MeatgunModule.Attribute.ATTACK_DAMAGE, (float) value);
        return this;
    }

    /// @param value amount in relative units
    public AttributeContainer consume(int value)
    {
        map.put(MeatgunModule.Attribute.AMOUNT_PER_USE, (float) (F * value));
        return this;
    }

    /// @param value capacity in relative units
    public AttributeContainer capacity(int value)
    {
        map.put(MeatgunModule.Attribute.CAPACITY, F * (float) value);
        return this;
    }

    public AttributeContainer applicable(SlotType... types)
    {
        applicable.clear();
        applicable.addAll(Set.of(types));
        return this;
    }

    public float get(MeatgunModule.Attribute attribute)
    {
        return (int) map.getOrDefault(attribute, 0f).floatValue();
    }

    public boolean isApplicable(SlotType slotType)
    {
        return applicable.contains(slotType);
    }

    public float attackDamage()
    {
        return get(MeatgunModule.Attribute.ATTACK_DAMAGE);
    }
}
