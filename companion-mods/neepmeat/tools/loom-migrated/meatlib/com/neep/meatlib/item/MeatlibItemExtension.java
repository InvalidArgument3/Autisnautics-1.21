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

import com.neep.meatlib.block.MeatlibBlockExtension;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface MeatlibItemExtension
{
    // Not sure if the namespace prefix is necessary.
    // I've forgotten which document described best practices.
    @Nullable
    default CreativeModeTab meatlib$getItemGroup()
    {
        return null;
    }

    default boolean meatlib$supportsGuideLookup()
    {
        return false;
    }

    default void meatlib$appendTags(MeatlibBlockExtension.TagConsumer<Item> consumer) {}
}
