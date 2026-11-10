/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat;

import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.client.renderer.RoboticArmRenderer;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.machine.alloy.AlloyKilnScreen;
import com.neep.neepmeat.machine.transport.FilterPipeScreen;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class NeepMeatClient {
    private NeepMeatClient() {}

    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        for (BuildingBlocks.Tint tint : BuildingBlocks.TINTS) {
            int rgb = tint.color().getFireworkColor();
            event.register((state, level, pos, index) -> rgb, tint.block().get());
        }
    }

    public static void onClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return 0xFF5A0A14;
            }
        }, BloodFluids.BLOOD_TYPE.get());
    }

    public static void onScreens(RegisterMenuScreensEvent event) {
        event.register(NeepMachines.ALLOY_MENU.get(), AlloyKilnScreen::new);
        event.register(NeepMachines.FILTER_MENU.get(), FilterPipeScreen::new);
    }

    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NeepMachines.ROBOTIC_ARM_BE.get(), RoboticArmRenderer::new);
    }

    public static void onExtraModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/spinny_bit")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/segment_1")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/segment_2")));
    }

    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        for (BuildingBlocks.Tint tint : BuildingBlocks.TINTS) {
            int rgb = tint.color().getFireworkColor();
            event.register((stack, index) -> rgb, tint.item().get());
        }
    }
}
