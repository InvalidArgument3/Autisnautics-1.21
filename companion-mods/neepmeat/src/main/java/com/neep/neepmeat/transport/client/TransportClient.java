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

package com.neep.neepmeat.transport.client;

import com.neep.neepmeat.client.renderer.ItemPipeRenderer;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.client.screen.*;
import com.neep.neepmeat.transport.client.screen.filter.ProcessInterfaceScreen;
import com.neep.neepmeat.transport.screen.TransportScreenHandlers;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class TransportClient
{
    public static void init()
    {
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.ITEM_REQUESTER_HANDLER, ManualRequesterScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.LIMITER_VALVE, LimiterValveScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.VSC, VSCScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.PIPE_PRIORITY, PipePriorityScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.ADVANCED_EJECTOR, AdvancedEjectorScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.POWER_EMITTER, PowerEmitterScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.PIPE_DRIVER, PipeDriverScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.REQUESTER, RequesterScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.ITEM_REQUEST_LISTENER, ProcessInterfaceScreen::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(TransportScreenHandlers.PROCESS_CARD, ProcessCardScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.ITEM_REQUESTER);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(ItemTransport.ITEM_REQUESTER_BE, ItemPipeRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.FLUID_GAUGE, FluidGaugeBlockRenderer::new);
    }
}
