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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.block.entity.MouthBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class MouthScreenHandler extends BasicScreenHandler
{
    public final MouthConfigPanelHandler handler;
    public ChannelManager<Runnable> openConfigC2S;
    @Nullable
    private final MouthBlockEntity be;

    public MouthScreenHandler(Inventory playerInventory, int syncId, @Nullable MouthBlockEntity be)
    {
        super(NeepBusScreenHandlers.MOUTH, playerInventory, syncId);
        this.be = be;
        this.handler = addWidgetHandler(new MouthConfigPanelHandler(playerInventory.player, be));

        this.openConfigC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "mouth_open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player);
        
        if (!playerInventory.player.level().isClientSide())
            openConfigC2S.receiver(this::openNeepBus);
    }

    private void openNeepBus()
    {
        if (be != null)
            playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
    }

    public MouthScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        openConfigC2S.close();
    }

    public static class MouthConfigPanelHandler extends ConfigWidgetHandler<Values>
    {
        private final MouthBlockEntity be;
        public final ChannelManager<Runnable> sayC2S;
        public final ChannelManager<Consumer<String>> errorS2C;
        String prevError = "";

        protected MouthConfigPanelHandler(Player player, MouthBlockEntity be)
        {
            super(player, Values.PARAM_CODEC);
            this.be = be;

            sayC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "mouth_say"),
                    ChannelFormat.builder(Runnable.class).build(), player);

            errorS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "mouth_error"),
                    ChannelFormat.builder(ParamCodecs.<Consumer<String>>as(Consumer.class))
                            .param(ParamCodec.STRING).build(), player);

            if (!player.level().isClientSide())
                sayC2S.receiver(be::trigger);
        }

        @Override
        public void sendUpdates()
        {
            super.sendUpdates();
            if (!prevError.equals(be.getError()))
            {
                prevError = be.getError();
                errorS2C.emitter().accept(prevError);
            }
        }

        @Override
        protected Values getValues()
        {
            return be.getValues();
        }

        @Override
        protected void setValues(Values values)
        {
            be.setValues(values.message, values.autoSay, values.phonetic, values.radius);
        }

        @Override
        public void close()
        {
            super.close();
            sayC2S.close();
            errorS2C.close();
        }
    }

    public record Values(String message, boolean autoSay, boolean phonetic, int radius)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.STRING)
                .param(ParamCodec.BOOLEAN)
                .param(ParamCodec.BOOLEAN)
                .param(ParamCodec.INT)
                .build();
    }
}
