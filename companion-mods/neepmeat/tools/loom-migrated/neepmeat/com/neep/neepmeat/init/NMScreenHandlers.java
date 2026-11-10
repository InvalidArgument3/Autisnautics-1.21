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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.fabricator.FabricatorScreenHandler;
import com.neep.neepmeat.machine.separator.SeparatorScreenHandler;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorScreenHandler;
import com.neep.neepmeat.plc.screen.BuilderScreenHandler;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import com.neep.neepmeat.plc.screen.ProgramCabinetScreenHandler;
import com.neep.neepmeat.plc.screen.ProgramCardScreenHandler;
import com.neep.neepmeat.screen_handler.*;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import com.neep.neepmeat.transport.screen.TransportScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

import static com.neep.meatlib.screen.ScreenHandlerRegistry.registerExtended;
import static com.neep.meatlib.screen.ScreenHandlerRegistry.register;

public class NMScreenHandlers
{
    public static MenuType<BufferScreenHandler> BUFFER_SCREEN_HANDLER;
    public static MenuType<ContentDetectorScreenHandler> CONTENT_DETECTOR_SCREEN_HANDLER;
    public static MenuType<RouterScreenHandler> ROUTER;
    public static MenuType<AdvancedRouterScreenHandler> ADVANCED_ROUTER;
    public static MenuType<StirlingEngineScreenHandler> STIRLING_ENGINE;
    public static MenuType<AlloyKilnScreenHandler> ALLOY_KILN;
    public static MenuType<GuideScreenHandler> GUIDE;
    public static MenuType<ImplantManagerScreenHandler> UPGRADE_MANAGER;
    public static ExtendedScreenHandlerType<FluidRationerScreenHandler> FLUID_RATIONER = new ExtendedScreenHandlerType<>(FluidRationerScreenHandler::new);
    public static MenuType<SeparatorScreenHandler> SEPARATOR;
    public static ExtendedScreenHandlerType<DisplayPlateScreenHandler> DISPLAY_PLATE;
    public static MenuType<FabricatorScreenHandler> FABRICATOR;
    public static MenuType<SmallCompressorScreenHandler> SMALL_COMPRESSOR;
    public static MenuType<DeployerScreenHandler> DEPLOYER;
    public static MenuType<VascularCondenserScreenHandler> VASCULAR_CONDENSER;

    public static MenuType<ItemOutputScreenHandler> ITEM_OUTPUT;
    public static MenuType<ItemInputScreenHandler> ITEM_INPUT;
    public static MenuType<DistributorPointScreenHandler> DISTRIBUTOR_POINT;

    public static MenuType<FilterScreenHandler> FILTER;

    public static MenuType<PLCScreenHandler> PLC;
    public static MenuType<ProgramCabinetScreenHandler> PROGRAM_CABINET;
    public static MenuType<ProgramCardScreenHandler> PROGRAM_CARD;
    public static MenuType<BuilderScreenHandler> BUILDER;
    public static ExtendedScreenHandlerType<ImplanterScreenHandler> IMPLANTER;
    public static MenuType<PhageRayScreenHandler> PHAGE_RAY;

    public static MenuType<LivingMachineScreenHandler> LIVING_MACHINE;

    public static void registerScreenHandlers()
    {
        BUFFER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new ResourceLocation(NeepMeat.NAMESPACE, "buffer_screen"), BufferScreenHandler::new);
        CONTENT_DETECTOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new ResourceLocation(NeepMeat.NAMESPACE, "content_detector"), ContentDetectorScreenHandler::new);
        ROUTER = ScreenHandlerRegistry.registerSimple(new ResourceLocation(NeepMeat.NAMESPACE, "router"), RouterScreenHandler::new);
        ADVANCED_ROUTER = register(NeepMeat.NAMESPACE, "advanced_router", AdvancedRouterScreenHandler::new);
        STIRLING_ENGINE = ScreenHandlerRegistry.registerSimple(new ResourceLocation(NeepMeat.NAMESPACE, "stirling_engine"), StirlingEngineScreenHandler::new);
        ALLOY_KILN = ScreenHandlerRegistry.registerSimple(new ResourceLocation(NeepMeat.NAMESPACE, "alloy_kiln"), AlloyKilnScreenHandler::new);
        GUIDE = register(NeepMeat.NAMESPACE, "guide", GuideScreenHandler::new);
        UPGRADE_MANAGER = registerExtended(NeepMeat.NAMESPACE, "upgrade_manager", ImplantManagerScreenHandler::new);
        FLUID_RATIONER = Registry.register(BuiltInRegistries.MENU, new ResourceLocation(NeepMeat.NAMESPACE, "fluid_rationer"), FLUID_RATIONER);
        SEPARATOR = register(NeepMeat.NAMESPACE, "separator", SeparatorScreenHandler::new);
        DISPLAY_PLATE = registerExtended(NeepMeat.NAMESPACE, "display_plate", DisplayPlateScreenHandler::new);
        FABRICATOR = registerExtended(NeepMeat.NAMESPACE, "fabricator", FabricatorScreenHandler::new);
        SMALL_COMPRESSOR = register(NeepMeat.NAMESPACE, "small_compressor", SmallCompressorScreenHandler::new);
        FILTER = register(NeepMeat.NAMESPACE, "filter", FilterScreenHandler::new);
        DEPLOYER = register(NeepMeat.NAMESPACE, "deployer", DeployerScreenHandler::new);
        VASCULAR_CONDENSER = register(NeepMeat.NAMESPACE, "vascular_condenser", VascularCondenserScreenHandler::new);

        LIVING_MACHINE = registerExtended(NeepMeat.NAMESPACE, "living_machine", LivingMachineScreenHandler::new);
        ITEM_OUTPUT = register(NeepMeat.NAMESPACE, "item_output", ItemOutputScreenHandler::new);
        ITEM_INPUT = register(NeepMeat.NAMESPACE, "item_input", ItemInputScreenHandler::new);
        DISTRIBUTOR_POINT = register(NeepMeat.NAMESPACE, "distributor_point", DistributorPointScreenHandler::new);
        PHAGE_RAY = register(NeepMeat.NAMESPACE, "phage_ray", PhageRayScreenHandler::new);

        PLC = registerExtended(NeepMeat.NAMESPACE, "plc", PLCScreenHandler::new);
        PROGRAM_CABINET = register(NeepMeat.NAMESPACE, "program_cabinet", ProgramCabinetScreenHandler::new);
        PROGRAM_CARD = register(NeepMeat.NAMESPACE, "program_card", ProgramCardScreenHandler::new);
        BUILDER = register(NeepMeat.NAMESPACE, "builder", BuilderScreenHandler::new);
        IMPLANTER = registerExtended(NeepMeat.NAMESPACE, "implanter", ImplanterScreenHandler::new);

        TransportScreenHandlers.registerScreenHandlers();
    }

}
