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

package com.neep.neepmeat.client.fluid;

import com.neep.meatlib.fluid.BuiltFluid;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMFluids;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.resources.ResourceLocation;

@Environment(value= EnvType.CLIENT)
public class NMFluidsClient
{
    public static final ResourceLocation BLOOD_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/blood_flowing");
    public static final ResourceLocation BLOOD = new ResourceLocation(NeepMeat.NAMESPACE, "block/blood_still");
//    public static final Identifier CHARGED_WORK_FLUID_FLOWING = new Identifier(NeepMeat.NAMESPACE, "block/charged_work_fluid_flowing");
//    public static final Identifier CHARGED_WORK_FLUID= new Identifier(NeepMeat.NAMESPACE, "block/charged_work_fluid_still");
    public static final ResourceLocation WORK_FLUID_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/work_fluid_flowing");
    public static final ResourceLocation WORK_FLUID = new ResourceLocation(NeepMeat.NAMESPACE, "block/work_fluid_still");
    public static final ResourceLocation ETHEREAL_FUEL_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/ethereal_fuel_flowing");
    public static final ResourceLocation ETHEREAL_FUEL = new ResourceLocation(NeepMeat.NAMESPACE, "block/ethereal_fuel_still");
    public static final ResourceLocation ELDRITCH_ENZYMES_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/eldritch_enzymes_flowing");
    public static final ResourceLocation ELDRITCH_ENZYMES = new ResourceLocation(NeepMeat.NAMESPACE, "block/eldritch_enzymes_still");
    public static final ResourceLocation DIRTY_ORE_FAT_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/dirty_ore_fat_flowing");
    public static final ResourceLocation DIRTY_ORE_FAT= new ResourceLocation(NeepMeat.NAMESPACE, "block/dirty_ore_fat_still");
    public static final ResourceLocation CLEAN_ORE_FAT_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/clean_ore_fat_flowing");
    public static final ResourceLocation CLEAN_ORE_FAT= new ResourceLocation(NeepMeat.NAMESPACE, "block/clean_ore_fat_still");
    public static final ResourceLocation MEAT = new ResourceLocation(NeepMeat.NAMESPACE, "block/meat_still");
    public static final ResourceLocation MEAT_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/meat_flowing");
    public static final ResourceLocation RECONSTITUTED_FOOD = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/reconstituted_food_still");
    public static final ResourceLocation RECONSTITUTED_FOOD_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/reconstituted_food_flowing");

    public static final ResourceLocation TISSUE_SLURRY_STILL = new ResourceLocation(NeepMeat.NAMESPACE, "block/tissue_slurry_still");
    public static final ResourceLocation TISSUE_SLURRY_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/tissue_slurry_flowing");
    public static final ResourceLocation MILK = new ResourceLocation(NeepMeat.NAMESPACE, "block/milk_still");
    public static final ResourceLocation MILK_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/milk_flowing");
    public static final ResourceLocation PASTEURISED_MILK = new ResourceLocation(NeepMeat.NAMESPACE, "block/pasteurised_milk_still");
    public static final ResourceLocation PASTEURISED_MILK_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/pasteurised_milk_flowing");
    public static final ResourceLocation FEED = new ResourceLocation(NeepMeat.NAMESPACE, "block/animal_feed_still");
    public static final ResourceLocation FEED_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/animal_feed_flowing");
    public static final ResourceLocation PINKDRINK = new ResourceLocation(NeepMeat.NAMESPACE, "block/pinkdrink_still");
    public static final ResourceLocation PINKDRINK_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/pinkdrink_flowing");
    public static final ResourceLocation COMPRESSED_AIR = new ResourceLocation(NeepMeat.NAMESPACE, "block/compressed_air_still");
    public static final ResourceLocation COMPRESSED_AIR_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/compressed_air_flowing");
    public static final ResourceLocation SLUDGE_STILL = new ResourceLocation(NeepMeat.NAMESPACE, "block/sludge_still");
    public static final ResourceLocation SLUDGE_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/sludge_flowing");

    public static final ResourceLocation OPAQUE_WATER_STILL = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/opaque_water_still");
    public static final ResourceLocation OPAQUE_WATER_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/opaque_water_flow");
    public static final ResourceLocation SMOOTH_WATER_STILL = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/smooth_water_still");
    public static final ResourceLocation SMOOTH_WATER_FLOWING = new ResourceLocation(NeepMeat.NAMESPACE, "block/fluid/smooth_water_flow");

    public static void registerFluidHandler(BuiltFluid<?, ?> builtFluid, FluidRenderHandler handler)
    {
        FluidRenderHandlerRegistry.INSTANCE.register(builtFluid.still(), builtFluid.flowing(), handler);
    }

    public static void registerFluidRenderers()
    {
        // Fluid textures
        registerFluidHandler(NMFluids.BLOOD, new SimpleFluidRenderHandler(
                BLOOD,
                BLOOD_FLOWING,
                0xFFFFFF
        ));

        registerFluidHandler(NMFluids.PATINA_TREATMENT, new SimpleFluidRenderHandler(
                new ResourceLocation("minecraft:block/water_still"),
                new ResourceLocation("minecraft:block/water_flow"),
                0x4db99a
        ));

        registerFluidHandler(NMFluids.WORK_FLUID, new SimpleFluidRenderHandler(
                WORK_FLUID,
                WORK_FLUID_FLOWING
        ));

        registerFluidHandler(NMFluids.ETHEREAL_FUEL, new SimpleFluidRenderHandler(
                ETHEREAL_FUEL,
                ETHEREAL_FUEL_FLOWING,
                0xFFFFFF
        ));

        registerFluidHandler(NMFluids.ELDRITCH_ENZYMES, new SimpleFluidRenderHandler(
                ELDRITCH_ENZYMES,
                ELDRITCH_ENZYMES_FLOWING,
                0x3657a2
        ));

        registerFluidHandler(NMFluids.SLUDGE, new SimpleFluidRenderHandler(
                SLUDGE_STILL,
                SLUDGE_FLOWING,
                0xffffff
        ));

//        FluidRenderHandlerRegistry.INSTANCE.register(NMFluids.STILL_BLOOD, NMFluids.FLOWING_BLOOD, new SimpleFluidRenderHandler(, DIRTY_ORE_FAT_FLOWING, 0x3657a2));
        FluidRenderHandlerRegistry.INSTANCE.register(NMFluids.STILL_DIRTY_ORE_FAT, NMFluids.FLOWING_DIRTY_ORE_FAT, new SimpleFluidRenderHandler(DIRTY_ORE_FAT, DIRTY_ORE_FAT_FLOWING, 0x3657a2));
        FluidRenderHandlerRegistry.INSTANCE.register(NMFluids.STILL_CLEAN_ORE_FAT, NMFluids.FLOWING_CLEAN_ORE_FAT, new SimpleFluidRenderHandler(CLEAN_ORE_FAT, CLEAN_ORE_FAT_FLOWING, 0x3657a2));
        registerFluidHandler(NMFluids.MEAT, new SimpleFluidRenderHandler(MEAT, MEAT_FLOWING));
        registerFluidHandler(NMFluids.FOOD, new SimpleFluidRenderHandler(RECONSTITUTED_FOOD, RECONSTITUTED_FOOD_FLOWING));
        registerFluidHandler(NMFluids.TISSUE_SLURRY, new SimpleFluidRenderHandler(TISSUE_SLURRY_STILL, TISSUE_SLURRY_FLOWING));
//        FluidRenderHandlerRegistry.INSTANCE.register(NMFluids.STILL_MILK, NMFluids.FLOWING_MILK, new SimpleFluidRenderHandler(MILK, MILK_FLOWING, 0xecebe1));
        registerFluidHandler(NMFluids.P_MILK, new SimpleFluidRenderHandler(PASTEURISED_MILK, PASTEURISED_MILK_FLOWING, 0xFFFFFF));

        registerFluidHandler(NMFluids.ANIMAL_FEED, new SimpleFluidRenderHandler(FEED, FEED_FLOWING));
        registerFluidHandler(NMFluids.PINKDRINK, new SimpleFluidRenderHandler(PINKDRINK, PINKDRINK_FLOWING, 0xFFf089a8));
        registerFluidHandler(NMFluids.COMPRESSED_AIR, new SimpleFluidRenderHandler(COMPRESSED_AIR, COMPRESSED_AIR_FLOWING));
        registerFluidHandler(NMFluids.FAT, new SimpleFluidRenderHandler(OPAQUE_WATER_STILL, OPAQUE_WATER_FLOWING, 0xFFfadaa3));
        registerFluidHandler(NMFluids.LIQUID_CLAY, new SimpleFluidRenderHandler(SMOOTH_WATER_STILL, SMOOTH_WATER_FLOWING, 0xFFa1a7b1));
        registerFluidHandler(NMFluids.OIL_OF_BROME, SimpleFluidRenderHandler.coloredWater(0x372d27));

        FluidVariantRendering.register(NMFluids.STILL_DIRTY_ORE_FAT, new OreFatFluidVariantRenderHandler());
        FluidVariantRendering.register(NMFluids.FLOWING_DIRTY_ORE_FAT, new OreFatFluidVariantRenderHandler());
        FluidVariantAttributes.register(NMFluids.STILL_DIRTY_ORE_FAT, new OreFatAttributeHandler());
        FluidVariantAttributes.register(NMFluids.FLOWING_DIRTY_ORE_FAT, new OreFatAttributeHandler());

        FluidVariantRendering.register(NMFluids.STILL_CLEAN_ORE_FAT, new OreFatFluidVariantRenderHandler());
        FluidVariantRendering.register(NMFluids.FLOWING_CLEAN_ORE_FAT, new OreFatFluidVariantRenderHandler());
        FluidVariantAttributes.register(NMFluids.STILL_CLEAN_ORE_FAT, new OreFatAttributeHandler());
        FluidVariantAttributes.register(NMFluids.FLOWING_CLEAN_ORE_FAT, new OreFatAttributeHandler());

        // TODO: move all attribute handlers to common
        FluidVariantRendering.register(NMFluids.FOOD.still(), new CoarseMeatVariantRenderHandler());
        FluidVariantRendering.register(NMFluids.FOOD.flowing(), new CoarseMeatVariantRenderHandler());
        FluidVariantAttributes.register(NMFluids.FOOD.still(), new MeatAttribtuteHandler());
        FluidVariantAttributes.register(NMFluids.FOOD.flowing(), new MeatAttribtuteHandler());

        // 1.19
//        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) ->
//        {
//            registry.register(WORK_FLUID);
//            registry.register(WORK_FLUID_FLOWING);
//            registry.register(CHARGED_WORK_FLUID);
//            registry.register(CHARGED_WORK_FLUID_FLOWING);
//            registry.register(ETHEREAL_FUEL);
//            registry.register(ETHEREAL_FUEL_FLOWING);
//            registry.register(ELDRITCH_ENZYMES);
//            registry.register(ELDRITCH_ENZYMES_FLOWING);
//            registry.register(DIRTY_ORE_FAT);
//            registry.register(DIRTY_ORE_FAT_FLOWING);
//            registry.register(CLEAN_ORE_FAT);
//            registry.register(CLEAN_ORE_FAT_FLOWING);
//            registry.register(BLOOD);
//            registry.register(BLOOD_FLOWING);
//            registry.register(MEAT);
//            registry.register(MEAT_FLOWING);
//            registry.register(TISSUE_SLURRY_STILL);
//            registry.register(TISSUE_SLURRY_FLOWING);
//            registry.register(MILK);
//            registry.register(MILK_FLOWING);
//            registry.register(FEED);
//            registry.register(FEED_FLOWING);
//            registry.register(PINKDRINK);
//            registry.register(PINKDRINK_FLOWING);
//        });

    }
}
