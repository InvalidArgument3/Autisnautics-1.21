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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidUnit;
import com.neep.neepmeat.transport.block.fluid_transport.entity.LimiterValveBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class LimiterValveScreenHandler extends BasicScreenHandler
{
    public final WidgetHandler widgetHandler;

    public LimiterValveScreenHandler(int syncId, Inventory playerInventory, @Nullable LimiterValveBlockEntity be)
    {
        super(TransportScreenHandlers.LIMITER_VALVE, playerInventory, null, syncId, null);
        this.widgetHandler = addWidgetHandler(new WidgetHandler(playerInventory.player, be));
    }

    public LimiterValveScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, null);
    }

    public record Values(int maxAmount, FluidUnit mode)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.INT)
                .param(ParamCodec.ofEnum(FluidUnit.class))
                .build();
    }

    public class WidgetHandler extends ConfigWidgetHandler<Values>
    {
        private final LimiterValveBlockEntity be;

        private Values buffered = new Values(0, FluidUnit.DROPLETS);

        protected WidgetHandler(Player player, LimiterValveBlockEntity be)
        {
            super(player, Values.PARAM_CODEC);
            this.be = be;

            updateParamsS2C.receiver(v -> buffered = v);
        }

        public void setMode(FluidUnit newMode)
        {
            buffered = new Values(buffered.maxAmount, newMode);
            receiveParamsC2S.emitter().update(buffered);
        }

        public void setAmount(int parsed)
        {
            buffered = new Values(parsed, buffered.mode);
            receiveParamsC2S.emitter().update(buffered);
        }

        @Override
        protected Values getValues()
        {
            return new Values(be.getMaxAmountInUnit(), be.getMode());
        }

        @Override
        protected void setValues(Values values)
        {
            be.setValues(values.maxAmount, values.mode);
        }
    }
}
