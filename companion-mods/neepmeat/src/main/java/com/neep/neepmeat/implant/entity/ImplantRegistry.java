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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.PlayerImplantItem;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ImplantRegistry
{
    public static final MappedRegistry<Constructor> REGISTRY = FabricRegistryBuilder.createSimple(
            Constructor.class, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "implant")).buildAndRegister();

    private static final Map<ResourceLocation, ItemLike> IMPLANT_TO_ITEM = new HashMap<>();

    public static void register()
    {

    }

    public static void register(ResourceLocation id, Constructor type, ItemLike item)
    {
        Registry.register(REGISTRY, id, type);
        registerItem(id, item);
    }

    public static void registerItem(ResourceLocation id, ItemLike item)
    {
        IMPLANT_TO_ITEM.put(id, item);
    }

    @Nullable
    public static ItemLike getItem(ResourceLocation id)
    {
        return IMPLANT_TO_ITEM.get(id);
    }

    @FunctionalInterface
    public interface Constructor
    {
        EntityImplant create(Entity player);
    }
}
