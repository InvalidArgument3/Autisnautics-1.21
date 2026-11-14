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
import com.neep.neepbus.block.entity.SignalIntegrator;
import net.minecraft.world.entity.player.Player;

public class IntegratorConfigHandler extends ConfigWidgetHandler<IntegratorConfigHandler.Values>
{
    private final SignalIntegrator integrator;

    public IntegratorConfigHandler(Player player, SignalIntegrator integrator)
    {
        super(player, Values.PARAM_CODEC);
        this.integrator = integrator;
    }

    @Override
    protected Values getValues()
    {
        return new Values(integrator.loopTime(), integrator.timeFactor());
    }

    @Override
    protected void setValues(Values values)
    {
        integrator.setValues(values.loopTime, values.timeFactor);
    }

    public record Values(int loopTime, float timeFactor)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class).param(ParamCodec.INT).param(ParamCodec.FLOAT).build();
    }
}
