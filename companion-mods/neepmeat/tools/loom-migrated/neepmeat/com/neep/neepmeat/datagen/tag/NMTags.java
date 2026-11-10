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

package com.neep.neepmeat.datagen.tag;

import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class NMTags
{
    public static final TagKey<Item> CHARNEL_COMPACTOR = registerItem(NeepMeat.NAMESPACE, "charnel_substrate");
    public static final TagKey<Item> BLOOD_BUBBLE_LOGS = registerItem(NeepMeat.NAMESPACE, "blood_bubble_logs");
    public static final TagKey<Item> FLUID_PIPES = registerItem(NeepMeat.NAMESPACE, "fluid_pipes");
    public static final TagKey<Item> GRAINS = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "grains"));
    public static final TagKey<Item> RAW_MEAT = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "raw_meat"));
    public static final TagKey<Item> RAW_FISH = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "raw_fish"));
    public static final TagKey<Item> METAL_SCAFFOLDING = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "metal_scaffolding"));
    public static final TagKey<Item> ROUGH_CONCRETE = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("neepmeat", "rough_concrete"));
    public static final TagKey<Item> SMOOTH_TILE = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("neepmeat", "smooth_tile"));
    public static final TagKey<Item> PAINTED_CORRUGATED_ASBESTOS = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("neepmeat", "painted_corrugated_asbestos"));
    public static final TagKey<Item> GUIDE_LOOKUP = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(NeepMeat.NAMESPACE, "guide_lookup"));
    public static final TagKey<Item> DOSIMETER = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(NeepMeat.NAMESPACE, "dosimeter"));
    public static final TagKey<Item> WRENCH = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "wrenches"));
    public static final TagKey<Item> TOOL_ORGANISM_EXCLUDE = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(NeepMeat.NAMESPACE, "tool_organism_exclude"));

    public static final TagKey<Block> FARMLAND = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("c", "farmland"));

    public static final TagKey<Block> PHAGE_RAY_OVERRIDE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "phage_ray_override_hardness"));
    public static final TagKey<Block> WRENCHABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "wrenchable"));

    public static final TagKey<Block> BLOCK_CRUSHING_INPUTS = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "block_crushing_inputs"));
    public static final TagKey<Block> DROPS_WITH_DRILL = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "drops_with_drill"));
    public static final TagKey<Item> BLOCK_CRUSHING_OUTPUTS = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(NeepMeat.NAMESPACE, "block_crushing_outputs"));
    public static final TagKey<Item> BLOCK_CRUSHING_OUTPUTS_EXCLUDE = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(NeepMeat.NAMESPACE, "block_crushing_outputs_exclude"));

    public static final TagKey<Block> CHARNEL_PUMP_OUTPUT_ORES = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "charnel_pump_output_ores"));
    public static final TagKey<Block> WRITHING_EARTH_REPLACABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "writhing_earth_replacable"));
    public static final TagKey<Block> CONTAMINATED_RUBBLE_REPLACABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "contaminated_rubble_replacable"));

    public static final TagKey<Block> ROCK_DRILL_MINEABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "rock_drill_mineable"));

    public static final TagKey<Block> WORM_ALTAR = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(NeepMeat.NAMESPACE, "worm_altar"));

    public static final TagKey<Fluid> BLOOD = TagKey.create(BuiltInRegistries.FLUID.key(), new ResourceLocation("c", "blood"));
    public static final TagKey<Block> MOPPABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "moppable"));
    public static final TagKey<Block> TREE_VACUUM_HARVESTABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "tree_vacuum_harvestable"));
    public static final TagKey<Block> PIPE_CLIMBABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "pipe_climbable"));
    public static final TagKey<Block> LADDERS = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "ladders"));

    public static final TagKey<Block> BROWNE_ORES = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "browne_ores"));
    public static final TagKey<Item> BROWNE_ORES_ITEM = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("neepmeat", "browne_ores"));

    public static final TagKey<Block> CONTAMINATED_DIRT = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("neepmeat", "contaminated_dirt"));

    public static final TagKey<EntityType<?>> CLONEABLE = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), new ResourceLocation(NeepMeat.NAMESPACE, "cloneable"));

    public static final TagKey<MeatgunModule.Type<? extends MeatgunModule>> ORE_BLOCK_DROPPING = TagKey.create(MeatgunModules.REGISTRY.key(), new ResourceLocation(NeepMeat.NAMESPACE, "ore_block_dropping"));

    public static final TagKey<Biome> HAS_ORE_ASBESTOS = TagKey.create(Registries.BIOME, new ResourceLocation(NeepMeat.NAMESPACE, "has_asbestos"));
    public static final TagKey<Biome> HAS_GRAVE_BROME = TagKey.create(Registries.BIOME, new ResourceLocation(NeepMeat.NAMESPACE, "has_grave_brome"));
    public static final TagKey<Biome> HAS_ORE_BROWNE = TagKey.create(Registries.BIOME, new ResourceLocation(NeepMeat.NAMESPACE, "has_browne_ore"));

    private static TagKey<Item> registerItem(String namespace, String id)
    {
        return TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(namespace, id));
    }

    public static void init()
    {

    }
}
