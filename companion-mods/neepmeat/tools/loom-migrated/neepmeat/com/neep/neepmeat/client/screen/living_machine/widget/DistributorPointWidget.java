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

package com.neep.neepmeat.client.screen.living_machine.widget;

import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.EnumSelectorWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.client.screen.widget.config.StringConfigField;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import com.neep.neepmeat.screen_handler.widget.DistributorPointWidgetHandler;
import net.minecraft.network.chat.Component;

import static com.neep.meatlib.theme.PLCCols.ENTRY_H;

public class DistributorPointWidget extends ConfigPanel<DistributorPointWidgetHandler.Values>
{
    private final StringConfigField addressField;
    private final IntNumberTextField cooldownField;

    private DistributorPointBlockEntity.Mode currentMode = DistributorPointBlockEntity.Mode.SEND;
    private boolean autoSend;

    public DistributorPointWidget(DistributorPointWidgetHandler handler)
    {
        super(handler);

        addressField = addTrackedField(new StringConfigField(textRenderer, 0, 0, 100, ENTRY_H,
                NeepMeat.translationKey("screen", "distributor_point.address"), f -> update()));
        addTrackedField(new EnumSelectorWidget<>(
                0, 0, 100, ENTRY_H, DistributorPointBlockEntity.Mode.values(),
                Component.nullToEmpty("Mode"), () -> currentMode, (w, m) ->
        {
            this.currentMode = m;
            update();
        }));

        this.cooldownField = addTrackedField(new IntNumberTextField(textRenderer, 0, 0, 100, ENTRY_H,
                NeepMeat.translationKey("screen", "distributor_point.max_cooldown"), f -> update(), DistributorPointBlockEntity.MIN_COOLDOWN, 10000, 5, 40)
        );

        addTrackedField(new CheckboxWidget(100, ENTRY_H, () -> autoSend, NeepMeat.translationKey("screen", "distributor_point.auto_send"), (button, toggled) ->
        {
            this.autoSend = toggled;
            update();
        }));
    }

    @Override
    protected DistributorPointWidgetHandler.Values createValues()
    {
        return new DistributorPointWidgetHandler.Values(addressField.parse(), currentMode, cooldownField.parse(), autoSend);
    }

    @Override
    protected void updateParams(DistributorPointWidgetHandler.Values values)
    {
        addressField.setTextSilent(values.addr());
        currentMode = values.mode();
        cooldownField.setNumberText(values.maxCooldown());
        autoSend = values.autoSend();
    }
}
