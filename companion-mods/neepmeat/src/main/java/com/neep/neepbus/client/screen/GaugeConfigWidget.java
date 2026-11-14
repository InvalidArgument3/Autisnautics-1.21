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

package com.neep.neepbus.client.screen;

import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.FloatNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigWidget;
import com.neep.meatlib.client.screen.widget.config.StringConfigField;
import com.neep.neepbus.screen.GaugeConfigHandler;
import net.minecraft.network.chat.Component;

public class GaugeConfigWidget extends ConfigWidget<GaugeConfigHandler.Values>
{
    private final FloatNumberTextField value;
    private final FloatNumberTextField min;
    private final FloatNumberTextField max;
    private final StringConfigField name;
    private final StringConfigField unit;

    private boolean usePrefix;

    public GaugeConfigWidget(int x, int y, int w, int h, GaugeConfigHandler handler)
    {
        super(x, y, w, h, handler);
        this.value = addTrackedField(new FloatNumberTextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Value: "), c -> update(), -Float.MAX_VALUE, Float.MAX_VALUE));
        this.min = addTrackedField(new FloatNumberTextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Min: "), c -> update(), -Float.MAX_VALUE, Float.MAX_VALUE));
        this.max = addTrackedField(new FloatNumberTextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Max: "), c -> update(), -Float.MAX_VALUE, Float.MAX_VALUE));
        this.name = addTrackedField(new StringConfigField(textRenderer, x, y, w, 8, Component.nullToEmpty("Name: "), c -> update()));
        this.unit = addTrackedField(new StringConfigField(textRenderer, x, y, w, 8, Component.nullToEmpty("Unit: "), c -> update()));
        addTrackedField(new CheckboxWidget(w, 8, () -> usePrefix, Component.nullToEmpty("Use metric prefix"), (wi, v) ->
        {
            this.usePrefix = v;
            update();
        }));
    }

    @Override
    protected GaugeConfigHandler.Values createValues()
    {
        return new GaugeConfigHandler.Values(value.parse(), min.parse(), max.parse(), name.parse(), usePrefix, unit.parse());
    }

    @Override
    protected void updateParams(GaugeConfigHandler.Values values)
    {
        value.setNumberText(values.value());
        min.setNumberText(values.min());
        max.setNumberText(values.max());
        name.setTextSilent(values.name());
        usePrefix = values.usePrefix();
        unit.setTextSilent(values.unit());
    }
}
