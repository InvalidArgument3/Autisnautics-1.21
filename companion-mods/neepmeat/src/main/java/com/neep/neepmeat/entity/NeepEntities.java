/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.entity;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeepEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, NeepMeat.ID);
    public static final DeferredHolder<EntityType<?>, EntityType<MachineWormEntity>> MACHINE_WORM = ENTITIES.register("machine_worm",
            () -> EntityType.Builder.of(MachineWormEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).build("machine_worm"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.neep.neepmeat.entity.skyworm.SkywormEntity>> SKYWORM = ENTITIES.register("skyworm",
            () -> EntityType.Builder.of(com.neep.neepmeat.entity.skyworm.SkywormEntity::new, MobCategory.MISC).sized(1.0F, 4.0F).build("skyworm"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.neep.neepmeat.machine.phage_ray.PhageRayEntity>> PHAGE_RAY = ENTITIES.register("phage_ray",
            () -> EntityType.Builder.<com.neep.neepmeat.machine.phage_ray.PhageRayEntity>of(com.neep.neepmeat.machine.phage_ray.PhageRayEntity::new, MobCategory.MISC).sized(0.8F, 0.8F).build("phage_ray"));

    private NeepEntities() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(NeepEntities::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(MACHINE_WORM.get(), MachineWormEntity.attributes());
    }
}
