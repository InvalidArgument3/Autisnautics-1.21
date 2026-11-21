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
import com.neep.meatlib.screen.NumberListConfigHandler;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.network.chat.Component;

public class MovementControlConfigWidget extends FloatListConfigWidget
{
    public MovementControlConfigWidget(int x, int y, int w, int h, NumberListConfigHandler<Float, FloatList> handler)
    {
        super(x, y, w, h, handler);
    }

    @Override
    protected void createTextFields()
    {
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Forward value")));
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Back value")));
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Left value")));
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Right value")));
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Up value")));
        addTextField(new TextField(textRenderer, 0, 0, 100, 8, Component.nullToEmpty("Down value")));
    }
}
