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

import com.neep.meatlib.registry.EntityRegistry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.entity.*;
import com.neep.neepmeat.entity.bovine_horror.AcidSprayEntity;
import com.neep.neepmeat.entity.bovine_horror.BovineHorrorEntity;
import com.neep.neepmeat.entity.compressor_scutter.CompressorScutterEntity;
import com.neep.neepmeat.entity.follower.FollowerEntity;
import com.neep.neepmeat.entity.path_legion.PathSoldierEntity;
import com.neep.neepmeat.entity.skyworm.SkywormEntity;
import com.neep.neepmeat.entity.smudge.SmudgeEntity;
import com.neep.neepmeat.entity.hound.HoundEntity;
import com.neep.neepmeat.entity.keeper.KeeperEntity;
import com.neep.neepmeat.entity.repeater.RepeaterEntity;
import com.neep.neepmeat.entity.scutter.FarmingScutter;
import com.neep.neepmeat.entity.worm.WormEntity;
import com.neep.neepmeat.machine.phage_ray.PhageRayEntity;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorMinecart;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class NMEntities
{
    public static EntityType<HoundEntity> HOUND;
    public static EntityType<GlomeEntity> GLOME;
    public static EntityType<MobEggEntity> EGG;

    public static EntityType<WormEntity> WORM;
    public static EntityType<BovineHorrorEntity> BOVINE_HORROR;
    public static EntityType<AcidSprayEntity> ACID_SPRAY;
    public static EntityType<RepeaterEntity> REPEATER;
    public static EntityType<WormEntity.WormSegment> WORM_SEGMENT;
    public static EntityType<LimbEntity> LIMB;

    public static EntityType<FarmingScutter> FARMING_SCUTTER;
    public static EntityType<CompressorScutterEntity> COMPRESSOR_SCUTTER;

    public static EntityType<PhageRayEntity> PHAGE_RAY;
    public static EntityType<SmallCompressorMinecart> SMALL_COMPRESSOR_MINECART;

    public static EntityType<FollowerEntity> FOLLOWER;
    public static EntityType<SmudgeEntity> SMUDGE;

    public static EntityType<MachineWormEntity> MACHINE_WORM;

    public static EntityType<SkywormEntity> SKYWORM;

    public static EntityType<ActiveWasteFallingBlockEntity> FALLING_ACTIVE_WASTE;

    public static EntityType<PressureWashEntity> PRESSURE_WASH;


    public static EntityType<KeeperEntity> KEEPER;
    public static EntityType<PathSoldierEntity> BOG_FOLLOWER;

    public static void initialise()
    {
//        TANK_MINECART = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "tank_minecart", FabricEntityTypeBuilder.create(SpawnGroup.MISC, TankMinecartEntity::new)
//                .dimensions(EntityDimensions.fixed(0.98f, 0.7f)).trackedUpdateRate(8).trackedUpdateRate(1).build());

        GLOME = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "glome", FabricEntityTypeBuilder.<GlomeEntity>create(MobCategory.MISC, GlomeEntity::new)
                .dimensions(EntityDimensions.fixed(0.7f, 0.7f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(GLOME, GlomeEntity.createMobAttributes());

        EGG = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "mob_egg", FabricEntityTypeBuilder.<MobEggEntity>create(MobCategory.MISC, MobEggEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(8).trackedUpdateRate(1).build());

        WORM = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "god_worm", FabricEntityTypeBuilder.<WormEntity>create(MobCategory.MISC, WormEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(WORM, WormEntity.createLivingAttributes());

        SKYWORM = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "skyworm", FabricEntityTypeBuilder.<SkywormEntity>create(MobCategory.MISC, SkywormEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(WORM, WormEntity.createLivingAttributes());

        HOUND = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "hound", FabricEntityTypeBuilder.create(MobCategory.MISC, HoundEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(HOUND, HoundEntity.createLivingAttributes());

        BOVINE_HORROR = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "bovine_horror", FabricEntityTypeBuilder.create(MobCategory.MISC, BovineHorrorEntity::new)
//                .dimensions(EntityDimensions.fixed(2.7f, 3.5f)).trackedUpdateRate(1).build());
                .dimensions(EntityDimensions.fixed(2.7f, 3.5f)).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(BOVINE_HORROR, BovineHorrorEntity.createLivingAttributes());
        ACID_SPRAY = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "acid_spray", FabricEntityTypeBuilder.create(MobCategory.MISC, AcidSprayEntity::new)
                .dimensions(EntityDimensions.fixed(1f, 1f)).trackedUpdateRate(8).build());
        REPEATER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "repeater", FabricEntityTypeBuilder.create(MobCategory.MISC, RepeaterEntity::new)
                .dimensions(EntityDimensions.fixed(1.5f, 1.5f)).trackedUpdateRate(1).build());

        PHAGE_RAY = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "phage_ray", FabricEntityTypeBuilder.create(MobCategory.MISC, PhageRayEntity::new)
                .dimensions(EntityDimensions.fixed(2.8f, 2.8f)).trackedUpdateRate(2).build());

        SMALL_COMPRESSOR_MINECART = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "small_compressor_minecart",
//                FabricEntityTypeBuilder.<SmallCompressorMinecart>create(SpawnGroup.MISC, SmallCompressorMinecart::new).dimensions(EntityDimensions.fixed(0.98F, 0.7F)).trackedUpdateRate(1).build());
        EntityType.Builder.<SmallCompressorMinecart>of(SmallCompressorMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build("small_compressor_minecart"));

        FOLLOWER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "follower", FabricEntityTypeBuilder.create(MobCategory.MISC, FollowerEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(3).build());
        FabricDefaultAttributeRegistry.register(FOLLOWER, FollowerEntity.createFollowerAttributes());
        SMUDGE = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "smudge", FabricEntityTypeBuilder.create(MobCategory.MISC, SmudgeEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(3).build());

        MACHINE_WORM = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "machine_worm", FabricEntityTypeBuilder.create(MobCategory.MISC, MachineWormEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.9f)).trackedUpdateRate(3).build());
        FabricDefaultAttributeRegistry.register(MACHINE_WORM, MachineWormEntity.createAttributes());

        LIMB = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "limb", FabricEntityTypeBuilder.<LimbEntity>create(MobCategory.MISC, LimbEntity::new)
                .dimensions(EntityDimensions.fixed(0.9f, 0.6f)).trackedUpdateRate(1).build());

        FARMING_SCUTTER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "farming_scutter", FabricEntityTypeBuilder.create(MobCategory.MISC, FarmingScutter::new)
                .dimensions(EntityDimensions.fixed(0.8f, 0.5f)).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(FARMING_SCUTTER, FarmingScutter.createMobAttributes());

        COMPRESSOR_SCUTTER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "compressor_scutter", FabricEntityTypeBuilder.create(MobCategory.MISC, CompressorScutterEntity::new)
                .dimensions(EntityDimensions.fixed(0.8f, 0.8f)).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(COMPRESSOR_SCUTTER, CompressorScutterEntity.createMobAttributes());

        FALLING_ACTIVE_WASTE = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "falling_active_waste", FabricEntityTypeBuilder.create(MobCategory.MISC, ActiveWasteFallingBlockEntity::new)
                .dimensions(EntityDimensions.fixed(0.98f, 0.98f)).trackedUpdateRate(10).trackedUpdateRate(20).build());

        PRESSURE_WASH = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "pressure_wash", FabricEntityTypeBuilder.create(MobCategory.MISC, PressureWashEntity::new)
                .dimensions(EntityDimensions.fixed(0.3f, 0.3f)).trackedUpdateRate(10).trackedUpdateRate(20).build());


        KEEPER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "keeper", FabricEntityTypeBuilder.create(MobCategory.MISC, KeeperEntity::new)
                .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(KEEPER, KeeperEntity.createLivingAttributes());

        BOG_FOLLOWER = EntityRegistry.registerEntity(NeepMeat.NAMESPACE, "bog_follower", FabricEntityTypeBuilder.create(MobCategory.MONSTER, PathSoldierEntity::new)
                .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).trackedUpdateRate(8).trackedUpdateRate(1).build());
        FabricDefaultAttributeRegistry.register(BOG_FOLLOWER, PathSoldierEntity.createLivingAttributes());
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, BOG_FOLLOWER, 4, 1, 3);
        SpawnPlacements.register(BOG_FOLLOWER, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, PathSoldierEntity::canSpawnInDark1);
    }
}
