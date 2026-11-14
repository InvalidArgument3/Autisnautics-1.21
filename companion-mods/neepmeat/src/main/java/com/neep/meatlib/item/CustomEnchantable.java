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


import java.util.Set;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Marker interface for items that will use extended enchantment behaviour.
 * This 'fixes' Minecraft's inconsistent use of Enchantment::isAcceptableItem and EnchantmentTarget::isAcceptableItem
 */
public interface CustomEnchantable
{
    /**
     * @return A list of Vanilla enchantments that can also be applied to this item, regardless of their checks.
     */
    default Set<Enchantment> injectVanilla()
    {
        return Set.of();
    }
}
