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
import com.neep.neepbus.block.entity.NeepBusBufferBlockEntity;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.message.DataVariant;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class NeepBusBufferScreenHandler extends NeepBusConfigScreenHandler
{
    public final ValueWidgetHandler valueWidgetHandler;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config, NeepBusBufferBlockEntity buffer)
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
                return new NeepBusBufferScreenHandler(playerInventory, syncId, config, buffer);
            }
        };
    }

    protected NeepBusBufferScreenHandler(Inventory playerInventory, int syncId, NeepBusConfig config, @Nullable NeepBusBufferBlockEntity buffer)
    {
        super(NeepBusScreenHandlers.BUFFER, playerInventory, syncId, config);
        this.valueWidgetHandler = addWidgetHandler(new ValueWidgetHandler(playerInventory.player, buffer));
    }

    public NeepBusBufferScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(playerInventory, syncId, NeepBusConfig.EMPTY, null);
        configHandler.receiveOpeningData(buf);
    }

    public static class ValueWidgetHandler extends ConfigWidgetHandler<ValueWidgetHandler.Values>
    {
        private final NeepBusBufferBlockEntity buffer;

        protected ValueWidgetHandler(Player player, NeepBusBufferBlockEntity buffer)
        {
            super(player, Values.PARAM_CODEC);
            this.buffer = buffer;
        }

        @Override
        protected void setValues(Values list)
        {
            buffer.setValues(list.highValue);
        }

        @Override
        protected Values getValues()
        {
            return new Values(buffer.getValue());
        }

        public record Values(DataVariant highValue)
        {
            public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                    .param(DataVariant.PARAM_CODEC)
                    .build();
        }
    }
}
