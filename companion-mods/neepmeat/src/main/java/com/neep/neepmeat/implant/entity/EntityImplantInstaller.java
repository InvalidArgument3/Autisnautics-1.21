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

import com.mojang.serialization.Codec;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMComponents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * An implant installer that operates on an Entity.
 */
public interface EntityImplantInstaller
{
    MappedRegistry<EntityImplantInstaller> REGISTRY = FabricRegistryBuilder.createSimple(
            EntityImplantInstaller.class, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "implant_installer")).buildAndRegister();

    Codec<EntityImplantInstaller> CODEC = REGISTRY.byNameCodec().fieldOf("id").codec();

    void install(Entity entity);

    static EntityImplantInstaller simple(ResourceLocation implant)
    {
        return entity ->
        {
            ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(entity);
            if (manager != null)
            {
                manager.installImplant(implant);
            }
        };
    }
}
