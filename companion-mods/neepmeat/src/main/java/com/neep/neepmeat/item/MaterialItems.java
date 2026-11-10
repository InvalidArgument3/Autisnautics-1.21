/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the simple crafting items in NMItems. Food values match NMFoodComponents.
 * Tools, weapons, implants, and other behaviour items are not registered here.
 */
package com.neep.neepmeat.item;

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public final class MaterialItems {
    public static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final List<DeferredItem<Item>> ALL = new ArrayList<>();

    public static final DeferredItem<Item> ANIMAL_HEART = item("animal_heart", new Item.Properties());
    public static final DeferredItem<Item> REANIMATED_HEART = item("reanimated_heart", new Item.Properties());
    public static final DeferredItem<Item> COPPER_COIL = item("copper_coil", new Item.Properties());
    public static final DeferredItem<Item> BIOELECTRIC_ORGAN = item("bioelectric_organ", new Item.Properties());
    public static final DeferredItem<Item> STATOR = item("stator", new Item.Properties());
    public static final DeferredItem<Item> FLYWHEEL = item("flywheel", new Item.Properties());
    public static final DeferredItem<Item> REFRACTORY_BRICK = item("refractory_brick", new Item.Properties());
    public static final DeferredItem<Item> WHISPER_BRASS_INGOT = item("whisper_brass_ingot", new Item.Properties());
    public static final DeferredItem<Item> MEAT_STEEL_INGOT = item("meat_steel_ingot", new Item.Properties());
    public static final DeferredItem<Item> BLOOD_BUBBLE = item("blood_bubble", new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2f).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1), 1f).build()));
    public static final DeferredItem<Item> MEAT_STEEL_COMPONENT = item("meat_steel_component", new Item.Properties());
    public static final DeferredItem<Item> MEAT_STEEL_BALL = item("meat_steel_ball", new Item.Properties());
    public static final DeferredItem<Item> CONTROL_UNIT = item("control_unit", new Item.Properties());
    public static final DeferredItem<Item> MEAT_SCRAP = item("meat_scrap", new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).build()));
    public static final DeferredItem<Item> RAW_MEAT_BRICK = item("raw_meat_brick", new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.7f).build()));
    public static final DeferredItem<Item> COOKED_MEAT_BRICK = item("cooked_meat_brick", new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationModifier(0.8f).build()));
    public static final DeferredItem<Item> INTERNAL_COMPONENTS = item("internal_components", new Item.Properties());
    public static final DeferredItem<Item> CONTRACTILE_ACTUATOR = item("contractile_actuator", new Item.Properties());
    public static final DeferredItem<Item> BRAIN_ROUGH = item("brain_rough", new Item.Properties());
    public static final DeferredItem<Item> ENLIGHTENED_BRAIN = item("enlightened_brain", new Item.Properties().food(
            new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).alwaysEdible()
                    .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 1), 1f).build()));
    public static final DeferredItem<Item> OPEN_EYE = item("open_eye", new Item.Properties());
    public static final DeferredItem<Item> DIVINE_ORGAN = item("divine_organ", new Item.Properties());
    public static final DeferredItem<Item> HUNGRY_HETEROSTRUCTURE = item("hungry_heterostructure", new Item.Properties());
    public static final DeferredItem<Item> CRUSHED_ENDER_PEARL = item("crushed_ender_pearl", new Item.Properties());
    public static final DeferredItem<Item> INTEGRATION_CHARGE_CRUDE = item("integration_charge_crude", new Item.Properties());
    public static final DeferredItem<Item> BIOMASS = item("biomass", new Item.Properties());
    public static final DeferredItem<Item> WHISPER_WHEAT = item("whisper_wheat", new Item.Properties());
    public static final DeferredItem<Item> WHISPER_FLOUR = item("whisper_flour", new Item.Properties());
    public static final DeferredItem<Item> CARTON = item("carton", new Item.Properties());
    public static final DeferredItem<Item> TRANSFORMING_TOOL_BASE = item("transforming_tool_base", new Item.Properties());
    public static final DeferredItem<Item> ASBESTOS_DUST = item("asbestos_dust", new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1f).effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1), 1f).build()));
    public static final DeferredItem<Item> ASBESTOS_FABRIC = item("asbestos_fabric", new Item.Properties());
    public static final DeferredItem<Item> CRUSHED_BROWNE_ORE = item("crushed_browne_ore", new Item.Properties());
    public static final DeferredItem<Item> BROWNE_ORE_DUST = item("browne_ore_dust", new Item.Properties());
    public static final DeferredItem<Item> PACKED_BROWNE = item("packed_browne", new Item.Properties());
    public static final DeferredItem<Item> BROWNE_INGOT = item("browne_ingot", new Item.Properties());
    public static final DeferredItem<Item> CRUSHED_ORE = item("crushed_ore", new Item.Properties());
    public static final DeferredItem<Item> ORE_DUST = item("ore_dust", new Item.Properties());

    private MaterialItems() {}

    private static DeferredItem<Item> item(String id, Item.Properties properties) {
        DeferredItem<Item> registered = CONTEXT.item(id, () -> new Item(properties));
        ALL.add(registered);
        return registered;
    }

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        bus.addListener(MaterialItems::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            for (DeferredItem<Item> item : ALL) {
                event.accept(item.get());
            }
        }
    }
}
