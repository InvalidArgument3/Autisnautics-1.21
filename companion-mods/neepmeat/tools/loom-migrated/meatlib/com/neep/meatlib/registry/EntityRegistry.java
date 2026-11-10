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

package com.neep.meatlib.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityRegistry
{
    public static <T extends Entity> EntityType<T> registerEntity(String namespace, String id, EntityType<?> type)
    {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(namespace, id), (EntityType<T>) type);
//                        .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
//                        .trackRangeBlocks(4).trackedUpdateRate(10)
//                        .build());
    }
}
