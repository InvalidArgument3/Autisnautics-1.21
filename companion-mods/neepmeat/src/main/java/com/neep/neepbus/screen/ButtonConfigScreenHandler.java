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

package com.neep.neepbus.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.block.entity.Button;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.message.DataVariant;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ButtonConfigScreenHandler extends NeepBusConfigScreenHandler
{
    public final ValueWidgetHandler valueWidgetHandler;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config, Button button)
    {
        return new ExtendedScreenHandlerFactory()
        {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
            {
                NeepBusConfigScreenHandler.writeOpeningData(config, buf);
            }

            @Override
            public Component getDisplayName()
            {
                return Component.empty();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
            {
                return new ButtonConfigScreenHandler(playerInventory, syncId, config, button);
            }
        };
    }

    public ButtonConfigScreenHandler(Inventory playerInventory, int syncId, NeepBusConfig config, Button button)
    {
        super(NeepBusScreenHandlers.BUTTON_CONFIG, playerInventory, syncId, config);
        valueWidgetHandler = addWidgetHandler(new ValueWidgetHandler(playerInventory.player, button));
    }

    public ButtonConfigScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(playerInventory, syncId, NeepBusConfig.EMPTY, Button.EMPTY);
        configHandler.receiveOpeningData(buf);
    }

    public static class ValueWidgetHandler extends ConfigWidgetHandler<ValueWidgetHandler.Values>
    {
        private final Button button;

        protected ValueWidgetHandler(Player player, Button button)
        {
            super(player, Values.PARAM_CODEC);
            this.button = button;
        }

        @Override
        protected void setValues(Values list)
        {
            button.setValues(list.highValue, list.fallDelay);
        }

        @Override
        protected Values getValues()
        {
            return new Values(button.getHighValue(), button.getFallDelay());
        }

        public record Values(DataVariant highValue, int fallDelay)
        {
            public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                    .param(DataVariant.PARAM_CODEC)
                    .param(ParamCodec.INT)
                    .build();
        }
    }
}
