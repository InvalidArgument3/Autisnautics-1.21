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

import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.auto.SimplePanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.SolidColProgressBar;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.screen_handler.widget.DistributorCooldownWidgetHandler;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DistributorCooldownWidget extends SimplePanel implements Rectangle.Mutable
{
    private final DistributorCooldownWidgetHandler handler;
    private final SolidColProgressBar cooldownBar;

    private int cooldown;
    private int maxCooldown;
    private boolean powered;

    public DistributorCooldownWidget(Font textRenderer, DistributorCooldownWidgetHandler handler)
    {
        this.handler = handler;

        handler.syncCooldownS2C.receiver(this::receiveCooldown);

        cooldownBar = new SolidColProgressBar(
                SplitPanel.Split.HORIZONTAL, textRenderer, PLCCols.BORDER.col, () ->
                maxCooldown == 0 ? 0 : cooldown / (float) maxCooldown,
                () -> Component.nullToEmpty(text()));

        solidW();
        solidH();
    }

    private String text()
    {
        if (!powered)
            return "INVALID MACHINE";

        if (cooldown <= 0)
            return "READY";

        return "";
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.renderTooltipLayer(context, mouseX, mouseY, delta);
        if (isWithin(mouseX, mouseY))
        {
            if (!powered)
            {
                TooltipHelper.get().draw(context, mouseX, mouseY, List.of(
                        Component.nullToEmpty("Add a machine controller and item/fluid ports.")
                ));
            }
        }
    }

    private void receiveCooldown(int cooldown, int maxCooldown, boolean powered)
    {
        this.cooldown = cooldown;
        this.maxCooldown = maxCooldown;
        this.powered = powered;
    }

    @Override
    protected ClickableWidget initMainElement()
    {
        setDims(cooldownBar, x, y, w, h);
        return cooldownBar;
    }
}
