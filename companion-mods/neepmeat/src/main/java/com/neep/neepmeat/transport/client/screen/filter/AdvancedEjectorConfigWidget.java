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

package com.neep.neepmeat.transport.client.screen.filter;

import com.neep.meatlib.client.screen.auto.CloseListener;
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.widget.EnumSelectorWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.neepmeat.transport.screen.ItemEjectorWidgetHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AdvancedEjectorConfigWidget extends VStackPanel implements CloseListener
{
    private final ItemEjectorWidgetHandler handler;

    private boolean roundRobin;
    private RedstoneMode redstoneMode = RedstoneMode.ACTIVE_WITH;
    private final IntNumberTextField amount;
    private final IntNumberTextField period;
    private final EnumSelectorWidget<RedstoneMode> redstoneModeWidget;

    public AdvancedEjectorConfigWidget(ItemEjectorWidgetHandler handler, int padding, int elementPadding, boolean centre)
    {
        super(padding, elementPadding, centre);
        this.handler = handler;

        Font textRenderer = Minecraft.getInstance().font;
        addChildIn(new CheckboxWidget(60, textRenderer.lineHeight + 4, () -> roundRobin,
            NeepMeat.translationKey("screen", "advanced_ejector.round_robin"), (button, toggled) -> roundRobin = toggled));
        amount = (IntNumberTextField) addChildIn(new IntNumberTextField(textRenderer, 0, 0, 60, textRenderer.lineHeight + 4,
            NeepMeat.translationKey("screen", "advanced_ejector.amount"),
            this::update, 0, 64, 1, 16).drawFancyBackground(false));
        period = addChildIn((IntNumberTextField) new IntNumberTextField(textRenderer, 0, 0, 60, textRenderer.lineHeight + 4,
            NeepMeat.translationKey("screen", "advanced_ejector.period"),
            this::update, 10, 100, 1, 10).drawFancyBackground(false));
        redstoneModeWidget = addChildIn(new EnumSelectorWidget<>(0, 0, 60, textRenderer.lineHeight + 4, RedstoneMode.values(),
                NeepMeat.translationKey("screen", "advanced_ejector.redstone_mode"), () -> redstoneMode, (b, t) -> redstoneMode = t));

        handler.updateC2SS2C.receiver(this::updateFromServer);
    }

    private void updateFromServer(boolean roundRobin, int amount, int period, RedstoneMode redstoneMode)
    {
        this.roundRobin = roundRobin;
        this.amount.setNumberText(amount);
        this.period.setNumberText(period);
        this.redstoneMode = redstoneMode;
    }

    private void update(IntNumberTextField field)
    {
        handler.updateC2SS2C.emitter().update(
                roundRobin,
                amount.parse(),
                period.parse(),
                redstoneMode
        );

        amount.dirty = false;
        period.dirty = false;
    }

    @Override
    public void close()
    {
        update(null);
    }
}
