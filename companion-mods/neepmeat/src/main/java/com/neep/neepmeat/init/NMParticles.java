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

import com.mojang.serialization.Codec;
import com.neep.meatlib.registry.ParticleRegistry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.OreDustRegistry;
import com.neep.neepmeat.client.particle.BloodDripParticleFactory;
import com.neep.neepmeat.client.particle.ColouredItemStackParticleFactory;
import com.neep.neepmeat.client.particle.FluidDripParticle;
import com.neep.neepmeat.client.particle.PressureWashParticle;
import com.neep.neepmeat.item.DynamicOreDustItem;
import com.neep.neepmeat.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class NMParticles
{
    public static final ParticleType<SwirlingParticleEffect> BLOCK_SWIRL = ParticleRegistry.register(NeepMeat.NAMESPACE, "block_swirl", SwirlingParticleEffect.PARAMETERS_FACTORY, SwirlingParticleEffect::createCodec);

    public static SimpleParticleType MEAT_SPLASH = FabricParticleTypes.simple();
    public static SimpleParticleType MEAT_FOUNTAIN = FabricParticleTypes.simple();
    public static SimpleParticleType MEAT_BIT = FabricParticleTypes.simple();
    public static SimpleParticleType MILK_SPLASH = FabricParticleTypes.simple();
    public static SimpleParticleType BODY_COMPOUND_SHOWER = FabricParticleTypes.simple();
    public static SimpleParticleType BLOOD_DRIP = FabricParticleTypes.simple();
    public static SimpleParticleType BLOOD_SHOWER = FabricParticleTypes.simple();

    public static PressureWashParticleType PRESSURE_WASH_BEAM = new PressureWashParticleType(true);
    public static ParticleType<BlockParticleOption> FLUID_BITS = FabricParticleTypes.complex(BlockParticleOption.DESERIALIZER);
    public static PressureWashParticleType FLUID_DRIP = new PressureWashParticleType(true);

    public static ParticleType<ItemParticleOption> COLOURED_ITEM = new ParticleType<>(false, ItemParticleOption.DESERIALIZER)
    {
        @Override
        public Codec<ItemParticleOption> codec()
        {
            return ItemParticleOption.codec(COLOURED_ITEM);
        }
    };

    public static void init()
    {
        MEAT_SPLASH = ParticleRegistry.register(NeepMeat.NAMESPACE, "meat_splash", MEAT_SPLASH);
        MEAT_FOUNTAIN = ParticleRegistry.register(NeepMeat.NAMESPACE, "meat_fountain", MEAT_FOUNTAIN);
        MEAT_BIT = ParticleRegistry.register(NeepMeat.NAMESPACE, "meat_bit", MEAT_BIT);
        MILK_SPLASH = ParticleRegistry.register(NeepMeat.NAMESPACE, "milk_splash", MILK_SPLASH);
        BODY_COMPOUND_SHOWER = ParticleRegistry.register(NeepMeat.NAMESPACE, "body_compound_shower", BODY_COMPOUND_SHOWER);
        BLOOD_DRIP = ParticleRegistry.register(NeepMeat.NAMESPACE, "blood_drip", BLOOD_DRIP);
        BLOOD_SHOWER = ParticleRegistry.register(NeepMeat.NAMESPACE, "blood_shower", BLOOD_SHOWER);
        PRESSURE_WASH_BEAM = ParticleRegistry.register(NeepMeat.NAMESPACE, "pressure_wash_beam", PRESSURE_WASH_BEAM);
        FLUID_BITS = ParticleRegistry.register(NeepMeat.NAMESPACE, "fluid_bits", FLUID_BITS);
        FLUID_DRIP = ParticleRegistry.register(NeepMeat.NAMESPACE, "fluid_drip", FLUID_DRIP);
        COLOURED_ITEM = ParticleRegistry.register(NeepMeat.NAMESPACE, "coloured_item", COLOURED_ITEM);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {
//            ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((atlasTexture, registry) ->
//            {
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_splash_0"));
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_splash_1"));
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_splash_2"));
//
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_bit_0"));
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_bit_1"));
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_bit_2"));
//                registry.register(new Identifier(NeepMeat.NAMESPACE, "particle/meat_bit_3"));
//            }));

            ParticleFactoryRegistry.getInstance().register(BLOCK_SWIRL, new SwirlingParticle.Factory());
            ParticleFactoryRegistry.getInstance().register(MEAT_SPLASH, FlameParticle.Provider::new);
            ParticleFactoryRegistry.getInstance().register(MEAT_FOUNTAIN, LavaParticle.Provider::new);
            ParticleFactoryRegistry.getInstance().register(MEAT_BIT, PortalParticle.Provider::new);
            ParticleFactoryRegistry.getInstance().register(MILK_SPLASH, SplashParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(BODY_COMPOUND_SHOWER, FallingParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(BLOOD_DRIP, BloodDripParticleFactory::new);
            ParticleFactoryRegistry.getInstance().register(BLOOD_SHOWER, FallingParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(PRESSURE_WASH_BEAM, PressureWashParticle.Factory::new);
//            ParticleFactoryRegistry.getInstance().register(MEAT_DROP, FlameParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(FLUID_BITS, FluidParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(FLUID_DRIP, FluidDripParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(COLOURED_ITEM, new ColouredItemStackParticleFactory(s ->
                    DynamicOreDustItem.getTint(OreDustRegistry.CRUSHED_ORE, s, 1)));
        }
    }
}
