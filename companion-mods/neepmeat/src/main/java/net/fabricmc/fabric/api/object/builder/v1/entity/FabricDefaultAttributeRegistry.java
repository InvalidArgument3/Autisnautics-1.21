/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Recorded attributes are applied on EntityAttributeCreationEvent.
 */

package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "neepmeat")
public final class FabricDefaultAttributeRegistry {
    private static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> ATTRIBUTES = new ConcurrentHashMap<>();

    private FabricDefaultAttributeRegistry() {}

    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder) {
        ATTRIBUTES.put(type, builder.build());
    }

    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier supplier) {
        ATTRIBUTES.put(type, supplier);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        ATTRIBUTES.forEach(event::put);
    }
}
