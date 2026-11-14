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

package com.neep.meatweapons.particle;

import com.neep.meatlib.registry.ParticleRegistry;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class MWParticles
{
    public static SimpleParticleType PLASMA_PARTICLE = FabricParticleTypes.simple();
    public static SimpleParticleType LONG_BOI_HIT = FabricParticleTypes.simple();
    public static SimpleParticleType PHAGE_RAY_SPLODGE = FabricParticleTypes.simple();
    public static SimpleParticleType SHOCK_STAFF = FabricParticleTypes.simple();
    public static SimpleParticleType BLOODTHROWER_SPLASH = FabricParticleTypes.simple();
//    public static BloodthrowerParticleType BLOODTHROWER_SPLASH = new BloodthrowerParticleType(true,
//            BloodthrowerParticleType.createCodec(MWParticles.BLOODTHROWER_SPLASH));
    public static ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> NORMAL_MUZZLE_FLASH = new MuzzleFlashParticleType(true,
            MuzzleFlashParticleType.createCodec(MWParticles.NORMAL_MUZZLE_FLASH));
    public static ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> LONG_BOI_MUZZLE_FLASH = new MuzzleFlashParticleType(true,
            MuzzleFlashParticleType.createCodec(MWParticles.LONG_BOI_MUZZLE_FLASH));
    public static ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> BLOB_MUZZLE_FLASH = new MuzzleFlashParticleType(true,
            MuzzleFlashParticleType.createCodec(MWParticles.BLOB_MUZZLE_FLASH));
    public static ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> BOSHER_MUZZLE_FLASH = new MuzzleFlashParticleType(true,
            MuzzleFlashParticleType.createCodec(MWParticles.BOSHER_MUZZLE_FLASH));
    public static ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> BLOOD_MUZZLE_FLASH = new MuzzleFlashParticleType(true,
            MuzzleFlashParticleType.createCodec(MWParticles.BLOOD_MUZZLE_FLASH));

    // Not registered
    public static SimpleParticleType BONE_STEALER = FabricParticleTypes.simple();

    public static void init()
    {
        PLASMA_PARTICLE = ParticleRegistry.register(MeatWeapons.NAMESPACE, "plasma", PLASMA_PARTICLE);
        LONG_BOI_HIT = ParticleRegistry.register(MeatWeapons.NAMESPACE, "long_boi_hit", LONG_BOI_HIT);
        PHAGE_RAY_SPLODGE = ParticleRegistry.register(MeatWeapons.NAMESPACE, "phage_ray_splodge", PHAGE_RAY_SPLODGE);
        SHOCK_STAFF = ParticleRegistry.register(MeatWeapons.NAMESPACE, "shock_staff", SHOCK_STAFF);
        BLOODTHROWER_SPLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "bloodthrower_splash", BLOODTHROWER_SPLASH);
        NORMAL_MUZZLE_FLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "normal_muzzle_flash", NORMAL_MUZZLE_FLASH);
        LONG_BOI_MUZZLE_FLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "long_boi_muzzle_flash", LONG_BOI_MUZZLE_FLASH);
        BLOB_MUZZLE_FLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "blob_muzzle_flash", BLOB_MUZZLE_FLASH);
        BOSHER_MUZZLE_FLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "bosher_muzzle_flash", BOSHER_MUZZLE_FLASH);
        BLOOD_MUZZLE_FLASH = ParticleRegistry.register(MeatWeapons.NAMESPACE, "blood_muzzle_flash", BLOOD_MUZZLE_FLASH);

        BONE_STEALER = ParticleRegistry.register(MeatWeapons.NAMESPACE, "bone_stealer", BONE_STEALER);
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient()
    {
//        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((atlasTexture, registry) ->
//        {
//            registry.register(new Identifier(MeatWeapons.NAMESPACE, "particle/plasma"));
//        }));

        ParticleFactoryRegistry.getInstance().register(PLASMA_PARTICLE, FlameParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(LONG_BOI_HIT, LongBoiHitParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHAGE_RAY_SPLODGE, PhageRaySplodgeParticle.Factory::horribleJank);
        ParticleFactoryRegistry.getInstance().register(SHOCK_STAFF, FlameParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(BLOODTHROWER_SPLASH, BloodthrowerParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(NORMAL_MUZZLE_FLASH, MuzzleFlashParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(LONG_BOI_MUZZLE_FLASH, MuzzleFlashParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(BLOB_MUZZLE_FLASH, MuzzleFlashParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(BOSHER_MUZZLE_FLASH, MuzzleFlashParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(BLOOD_MUZZLE_FLASH, ExpandingMuzzleFlashParticleFactory::new);

        // A bit of an experiment
        ParticleFactoryRegistry.getInstance().register(BONE_STEALER, spriteProvider ->
        {
            BoneStealerGraphicsEffect.PROVIDER = spriteProvider;
            return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> null;
        });
    }
}
