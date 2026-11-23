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
import com.neep.neepbus.part.Indicator;
import net.minecraft.world.entity.player.Player;

public class GaugeConfigHandler extends ConfigWidgetHandler<GaugeConfigHandler.Values>
{
    private final Indicator indicator;

    public GaugeConfigHandler(Player player, Indicator indicator)
    {
        super(player, Values.PARAM_CODEC);
        this.indicator = indicator;
    }

    @Override
    protected Values getValues()
    {
        return new Values(indicator.getValue(),
                indicator.getMinValue(),
                indicator.getMaxValue(),
                indicator.getName(),
                indicator.getUsePrefix(),
                indicator.getUnit()
        );
    }

    @Override
    protected void setValues(Values objects)
    {
        indicator.setValues(objects.value, objects.min, objects.max, objects.name, objects.usePrefix, objects.unit);
    }

    public record Values(float value, float min, float max, String name, boolean usePrefix, String unit)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.FLOAT).param(ParamCodec.FLOAT).param(ParamCodec.FLOAT).param(ParamCodec.STRING)
                .param(ParamCodec.BOOLEAN).param(ParamCodec.STRING)
                .build();
    }
}
