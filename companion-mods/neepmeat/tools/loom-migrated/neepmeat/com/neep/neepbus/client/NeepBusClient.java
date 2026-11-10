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

package com.neep.neepbus.client;

import com.neep.meatlib.MeatLib;
import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.block.GaugeProvider;
import com.neep.neepbus.block.entity.GaugeBlockEntity;
import com.neep.neepbus.block.entity.MovementControlBlockEntity;
import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepbus.client.input.MovementControlHandler;
import com.neep.neepbus.client.item.NetworkingToolClient;
import com.neep.neepbus.client.screen.*;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class NeepBusClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        NetworkingToolClient.init();
        InputInterceptorsClient.init();

        ClientTickEvents.START_CLIENT_TICK.register(client ->
        {
            HitResult hitResult = client.hitResult;

            if (client.level != null && hitResult instanceof BlockHitResult blockHitResult)
            {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = client.level.getBlockState(blockHitResult.getBlockPos());
                if (state.getBlock() instanceof GaugeProvider gaugeProvider)
                {
                    GaugeBlockEntity gauge = gaugeProvider.getBlockEntity(client.level, pos);
                    MutableComponent text = Component.empty();

                    @Nullable String name = gauge.getName();
                    if (name != null && !name.isEmpty())
                    {
                        text.append(name);
                        text.append(": ");
                    }

//                    text.append(NeepBus.formatFloat(gauge.getValue(), true));
                    text.append(MeatLib.formatEngineering(gauge.getValue(), 2, gauge.getUsePrefix(), gauge.getUnit()));

                    client.player.displayClientMessage(text, true);
                }
            }
        });

        // IDE says this ↓ is unnecessary, but the compiler disagrees.
        MenuScreens.<NeepBusConfigScreenHandler, NeepBusConfigScreen<NeepBusConfigScreenHandler>>register(NeepBusScreenHandlers.NEEPBUS_CONFIG, NeepBusConfigScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.NEEPBUS_RANGE_CONFIG, NeepBusRangeConfigScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.SLIDER, SliderScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.MOVEMENT_CONTROL, MovementControllerScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.GAUGE, GaugeConfigScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.PID_CONTROLLER, PIDControllerScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.DELAY, DelayScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.BUFFER, NeepBusBufferScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.BUTTON_CONFIG, ButtonConfigScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.INTEGRATOR, IntegratorScreen::new);
        MenuScreens.register(NeepBusScreenHandlers.MOUTH, MouthScreen::new);

        InputInterceptorsClient.register(MovementControlBlockEntity.ID, MovementControlHandler::new);
    }
}
