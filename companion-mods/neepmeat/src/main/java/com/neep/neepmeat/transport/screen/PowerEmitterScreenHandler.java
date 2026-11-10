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
import com.neep.neepmeat.transport.block.energy_transport.entity.PowerEmitterBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PowerEmitterScreenHandler extends BasicScreenHandler
{
    public final PowerEmitterConfigWidgetHandler handler;

    public PowerEmitterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public PowerEmitterScreenHandler(Inventory playerInventory, int syncId, @Nullable PowerEmitterBlockEntity be)
    {
        super(TransportScreenHandlers.POWER_EMITTER, playerInventory, null, syncId, null);
        this.handler = addWidgetHandler(new PowerEmitterConfigWidgetHandler(playerInventory.player, be));
    }

    public static class PowerEmitterConfigWidgetHandler extends ConfigWidgetHandler<PowerEmitterConfigWidgetHandler.Values>
    {
        private final PowerEmitterBlockEntity be;

        protected PowerEmitterConfigWidgetHandler(Player player, PowerEmitterBlockEntity be)
        {
            super(player, Values.PARAM_CODEC);
            this.be = be;
        }

        @Override
        protected Values getValues()
        {
            return new Values(be.getOutputPower());
        }

        @Override
        protected void setValues(Values values)
        {
            be.setPower(values.powerOutput);
        }

        public record Values(int powerOutput)
        {
            public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class).param(ParamCodec.INT).build();
        }
    }
}
