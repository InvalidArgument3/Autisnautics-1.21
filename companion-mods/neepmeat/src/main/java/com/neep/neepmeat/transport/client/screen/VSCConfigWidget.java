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

package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.EnumSelectorWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCMode;
import com.neep.neepmeat.transport.screen.widget.VSCConfigWidgetHandler;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import static com.neep.meatlib.theme.PLCCols.ENTRY_H;

public class VSCConfigWidget extends ConfigPanel<VSCConfigWidgetHandler.Values>
{
    private static final Component POWER_TEXT = Component.translatable("screen." + NeepMeat.NAMESPACE + ".vsc.text.power");
    private static final Component PRIORITY_TEXT = Component.translatable("screen.neepmeat.vascular.priority");
    private static final Component BALANCE_TEXT = Component.translatable("screen.neepmeat.vsc.text.mode");

    private final IntNumberTextField target;
    private final IntNumberTextField priority;

    private VSCConfigWidgetHandler.Values values = new VSCConfigWidgetHandler.Values(0, 0, true, VSCMode.NORMAL);
    private VSCMode setMode = VSCMode.NORMAL;
    boolean cutOff;

    public VSCConfigWidget(ConfigWidgetHandler<VSCConfigWidgetHandler.Values> handler)
    {
        super(handler);

        this.target = addTrackedField(new IntNumberTextField(textRenderer, x, y, w, ENTRY_H, POWER_TEXT, c -> update(), 0, Integer.MAX_VALUE, 10, 100));
        addTrackedField(new CheckboxWidget(100, ENTRY_H, () -> cutOff, Component.nullToEmpty(I18n.get("screen.neepmeat.vsc.cut_off")), (button, toggled) -> cutOff = toggled));
        addTrackedField(new EnumSelectorWidget<>(0, 0, 100, ENTRY_H, VSCMode.values(), BALANCE_TEXT, () -> values.mode(), this::setMode)
                .tooltip(VSCMode::wrappedText));
        this.priority = addTrackedField(new IntNumberTextField(textRenderer, x, y, w, ENTRY_H, PRIORITY_TEXT, c -> update(), -Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 10));
    }

    private void setMode(EnumSelectorWidget<VSCMode> vscModeEnumSelectorWidget, VSCMode mode)
    {
        this.setMode = mode;
        update();
    }

    @Override
    protected VSCConfigWidgetHandler.Values createValues()
    {
        return new VSCConfigWidgetHandler.Values(target.parse(), priority.parse(), cutOff, setMode);
    }

    @Override
    protected void updateParams(VSCConfigWidgetHandler.Values values)
    {
        target.setNumberText(values.power());
        priority.setNumberText(values.priority());
        setMode = values.mode();
        cutOff = values.cutOff();
        this.values = values;
    }
}
