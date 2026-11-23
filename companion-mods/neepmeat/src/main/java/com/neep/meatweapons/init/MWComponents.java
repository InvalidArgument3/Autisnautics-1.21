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

package com.neep.meatweapons.init;

import com.neep.neepmeat.component.ItemIdentityComponentImpl;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.component.GunTickHandler;
import com.neep.meatweapons.component.AssaultDrillComponent;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.component.MeatgunComponentImpl;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class MWComponents implements ItemComponentInitializer, WorldComponentInitializer, EntityComponentInitializer
{
    public static final ComponentKey<MeatgunComponent> MEATGUN =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "meatgun"),
                    MeatgunComponent.class);

    public static final ComponentKey<AssaultDrillComponent> ASSAULT_DRILL =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "assault_drill"),
                    AssaultDrillComponent.class);

    public static final ComponentKey<GunTickHandler> GUN_TICK_HANDLER =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "gun_tick_handler"),
                    GunTickHandler.class);

    @Override
    public void registerItemComponentFactories(@NotNull ItemComponentFactoryRegistry registry)
    {
        registry.register(MWItems.BASIC_PISTOL, MEATGUN, stack -> new MeatgunComponentImpl(stack, MEATGUN));
        registry.register(MWItems.MEATGUN_PISTOL, MEATGUN, stack -> new MeatgunComponentImpl(stack, MEATGUN));
        registry.register(MWItems.MEATGUN_STAFF, MEATGUN, stack -> new MeatgunComponentImpl(stack, MEATGUN));
        registry.register(MWItems.LONG_GUN, MEATGUN, stack -> new MeatgunComponentImpl(stack, MEATGUN));

        ItemIdentityComponentImpl.register(MWItems.ASSAULT_DRILL, registry);
        registry.register(MWItems.ASSAULT_DRILL, ASSAULT_DRILL, AssaultDrillComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry)
    {
//        registry.register(MEATGUN_PARTICLES, MeatgunParticleManager::new);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
    {
        registry.registerFor(Player.class, GUN_TICK_HANDLER, GunTickHandler::new);
    }
}
