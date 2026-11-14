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
import com.neep.neepmeat.client.particle.BloodDripParticleFactory;
import com.neep.neepmeat.client.particle.FluidDripParticle;
import com.neep.neepmeat.client.particle.PressureWashParticle;
import com.neep.neepmeat.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class NMParticles
{
    public static ParticleType<SwirlingParticleEffect> BLOCK_SWIRL;
    public static SimpleParticleType MEAT_SPLASH;
    public static SimpleParticleType MEAT_FOUNTAIN;
    public static SimpleParticleType MEAT_BIT;
    public static SimpleParticleType MILK_SPLASH;
    public static SimpleParticleType BODY_COMPOUND_SHOWER;
    public static SimpleParticleType BLOOD_DRIP;
    public static SimpleParticleType BLOOD_SHOWER;

    public static PressureWashParticleType PRESSURE_WASH_BEAM;
    public static ParticleType<BlockParticleOption> FLUID_BITS;
    public static PressureWashParticleType FLUID_DRIP;
    public static ParticleType<ItemParticleOption> COLOURED_ITEM;

    public static void register(net.neoforged.bus.api.IEventBus bus)
    {
        bus.addListener((net.neoforged.neoforge.registries.RegisterEvent event) ->
        {
            if (event.getRegistryKey() != net.minecraft.core.registries.Registries.PARTICLE_TYPE)
            {
                return;
            }
            MEAT_SPLASH = registerSimple(event, "meat_splash");
            MEAT_FOUNTAIN = registerSimple(event, "meat_fountain");
            MEAT_BIT = registerSimple(event, "meat_bit");
            MILK_SPLASH = registerSimple(event, "milk_splash");
            BODY_COMPOUND_SHOWER = registerSimple(event, "body_compound_shower");
            BLOOD_DRIP = registerSimple(event, "blood_drip");
            BLOOD_SHOWER = registerSimple(event, "blood_shower");
            PRESSURE_WASH_BEAM = new PressureWashParticleType(true);
            event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "pressure_wash_beam"), () -> PRESSURE_WASH_BEAM);
            FLUID_BITS = fluidBits();
            event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "fluid_bits"), () -> FLUID_BITS);
            FLUID_DRIP = new PressureWashParticleType(true);
            event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "fluid_drip"), () -> FLUID_DRIP);
            BLOCK_SWIRL = blockSwirl();
            event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block_swirl"), () -> BLOCK_SWIRL);
            COLOURED_ITEM = colouredItem();
            event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "coloured_item"), () -> COLOURED_ITEM);
        });
    }

    private static SimpleParticleType registerSimple(net.neoforged.neoforge.registries.RegisterEvent event, String name)
    {
        SimpleParticleType type = new SimpleParticleType(false);
        event.register(net.minecraft.core.registries.Registries.PARTICLE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, name), () -> type);
        return type;
    }

    private static ParticleType<BlockParticleOption> fluidBits()
    {
        return new ParticleType<>(false)
        {
            @Override
            public com.mojang.serialization.MapCodec<BlockParticleOption> codec()
            {
                return BlockParticleOption.codec(FLUID_BITS);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, BlockParticleOption> streamCodec()
            {
                return BlockParticleOption.streamCodec(FLUID_BITS);
            }
        };
    }

    private static ParticleType<SwirlingParticleEffect> blockSwirl()
    {
        return new ParticleType<>(false)
        {
            @Override
            public com.mojang.serialization.MapCodec<SwirlingParticleEffect> codec()
            {
                return SwirlingParticleEffect.createMapCodec(BLOCK_SWIRL);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, SwirlingParticleEffect> streamCodec()
            {
                return SwirlingParticleEffect.streamCodec(BLOCK_SWIRL);
            }
        };
    }

    private static ParticleType<ItemParticleOption> colouredItem()
    {
        return new ParticleType<>(false)
        {
            @Override
            public com.mojang.serialization.MapCodec<ItemParticleOption> codec()
            {
                return ItemParticleOption.codec(COLOURED_ITEM);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, ItemParticleOption> streamCodec()
            {
                return ItemParticleOption.streamCodec(COLOURED_ITEM);
            }
        };
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
            ParticleFactoryRegistry.getInstance().register(MILK_SPLASH, SplashParticle.Provider::new);
            ParticleFactoryRegistry.getInstance().register(BODY_COMPOUND_SHOWER, FallingParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(BLOOD_DRIP, BloodDripParticleFactory::new);
            ParticleFactoryRegistry.getInstance().register(BLOOD_SHOWER, FallingParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(PRESSURE_WASH_BEAM, PressureWashParticle.Factory::new);
//            ParticleFactoryRegistry.getInstance().register(MEAT_DROP, FlameParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(FLUID_BITS, FluidParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(FLUID_DRIP, FluidDripParticle.Factory::new);
            ParticleFactoryRegistry.getInstance().register(COLOURED_ITEM, sprites -> new net.minecraft.client.particle.BreakingItemParticle.Provider());
        }
    }
}
