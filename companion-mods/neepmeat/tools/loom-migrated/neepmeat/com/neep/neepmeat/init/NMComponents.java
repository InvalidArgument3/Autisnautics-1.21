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

package com.neep.neepmeat.init;

import com.neep.meatlib.MeatLib;
import com.neep.meatweapons.component.MeatgunComponentImpl;
import com.neep.meatweapons.init.MWComponents;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.component.*;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.entity.MobEggEntity;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.entity.MobImplantManager;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.implant.item.ItemImplantManager;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.recipe.EntityWorkpiece;
import com.neep.neepmeat.plc.recipe.ItemWorkpiece;
import com.neep.neepmeat.plc.recipe.PlayerWorkpiece;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.item.ProcessCardComponent;
import com.neep.neepmeat.transport.worm.DistributorRouting;
import com.neep.neepmeat.transport.worm.DistributorRoutingComponent;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NMComponents implements EntityComponentInitializer, ItemComponentInitializer, ChunkComponentInitializer, WorldComponentInitializer
{
    public static final ComponentKey<ImplantManager> IMPLANT_MANAGER =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "implant_manager"),
                    ImplantManager.class);

    public static final ComponentKey<Workpiece> WORKPIECE =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "workpiece"),
                    Workpiece.class);

    public static final ComponentKey<EnlightenmentManager> ENLIGHTENMENT_MANAGER =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "enlightenment_manager"),
                    EnlightenmentManager.class);

    public static final ComponentKey<PowerResourceComponent> COMPRESSED_AIR =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "compressed_air"),
                    PowerResourceComponent.class);

    public static final ComponentKey<ChunkEnlightenmentComponent> CHUNK_ENLIGHTENMENT =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "chunk_enlightenment"),
                    ChunkEnlightenmentComponent.class);

    public static final ComponentKey<PressureWasherComponent> PRESSURE_WASHER =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "pressure_washer"),
                    PressureWasherComponent.class);

    public static final ComponentKey<ProgramStorageComponent> PROGRAM_STORAGE =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "program_storage"),
                    ProgramStorageComponent.class);

    public static final ComponentKey<ProcessCardComponent> PROCESS_STORAGE =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "process_storage"),
                    ProcessCardComponent.class);

    public static final ComponentKey<ItemIdentityComponent> UNIQUE_ID =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(MeatLib.NAMESPACE, "unique_id"),
                    ItemIdentityComponent.class);

//    public static final ComponentKey<PressureWasherComponent> POWER =
//            ComponentRegistry.getOrCreate(
//                    new Identifier(NeepMeat.NAMESPACE, "power"),
//                    PressureWasherComponent.class);

    public static final ComponentKey<DistributorRoutingComponent> DISTRIBUTOR =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(NeepMeat.NAMESPACE, "distributor_routing"),
                    DistributorRoutingComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
    {
        registry.beginRegistration(Player.class, IMPLANT_MANAGER).impl(PlayerImplantManager.class).respawnStrategy(RespawnCopyStrategy.CHARACTER).end(PlayerImplantManager::new);
        registry.beginRegistration(Mob.class, IMPLANT_MANAGER).impl(MobImplantManager.class).end(MobImplantManager::new);
        registry.beginRegistration(Player.class, ENLIGHTENMENT_MANAGER).impl(PlayerEnlightenmentManager.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PlayerEnlightenmentManager::new);
//        registry.registerFor(CowEntity.class, WORKPIECE, MobWorkpiece::new);
//        registry.registerFor(ZombieEntity.class, WORKPIECE, MobWorkpiece::new);
        registry.registerFor(Mob.class, WORKPIECE, EntityWorkpiece::new);
        registry.registerFor(MobEggEntity.class, WORKPIECE, EntityWorkpiece::new);
        registry.registerFor(Player.class, WORKPIECE, PlayerWorkpiece::new);
        registry.beginRegistration(Player.class, COMPRESSED_AIR).impl(PlayerCompressedAirComponent.class).end(PlayerCompressedAirComponent::new);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry)
    {
        registry.register(item -> true, WORKPIECE, ItemWorkpiece::new);
        registry.register(item -> true, IMPLANT_MANAGER, ItemImplantManager::new);
        registry.register(NMItems.PRESSURE_WASHER, PRESSURE_WASHER, ItemPressureWasherComponent::new);
        registry.register(PLCBlocks.PROGRAM_CARD, PROGRAM_STORAGE, ProgramStorageComponent::new);
        registry.register(ItemTransport.PROCESS_CARD, PROCESS_STORAGE, ProcessCardComponent::new);
        registry.register(NMItems.DRILL_CHASSIS, MWComponents.MEATGUN, stack -> new MeatgunComponentImpl(stack, MWComponents.MEATGUN));
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry)
    {
        registry.register(CHUNK_ENLIGHTENMENT, ChunkEnlightenmentComponentImpl::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry worldComponentFactoryRegistry)
    {
        worldComponentFactoryRegistry.register(DISTRIBUTOR, DistributorRouting.class, DistributorRouting::new);
    }
}
