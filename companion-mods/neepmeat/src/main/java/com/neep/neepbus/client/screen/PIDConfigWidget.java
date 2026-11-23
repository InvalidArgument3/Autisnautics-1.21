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

import com.neep.meatlib.client.screen.widget.config.FloatListConfigWidget;
import com.neep.neepbus.screen.PIDConfigHandler;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.gui.screens.Screen;

public class PIDConfigWidget extends FloatListConfigWidget
{
    public PIDConfigWidget(int x, int y, int w, int h, PIDConfigHandler handler)
    {
        super(x, y, w, h, handler);
    }

    @Override
    protected float scrollMultiplier()
    {
        return Screen.hasShiftDown() ? 0.1f : 1;
    }

    @Override
    protected void createTextFields()
    {
        addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "pid_controller.dt")));
        addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "pid_controller.kp")));
        addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "pid_controller.ki")));
        addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "pid_controller.kd")));
        addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "pid_controller.windup_limit")));
    }
}
