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

package com.neep.meatlib.client.screen.auto;

import com.neep.meatlib.client.screen.widget.ClickableWidget;
import net.minecraft.client.gui.GuiGraphics;
import com.neep.meatlib.client.screen.GUIUtil;

/**
 * Yet another version of {@link com.neep.neepmeat.client.screen.util.HashedBackground}.
 * This one is part of the auto gui layout thingamy.
 */
public class HashedBackgroundPanel extends SimplePanel
{
    public HashedBackgroundPanel(ClickableWidget element)
    {
        this(element, 5);
    }

    public HashedBackgroundPanel(ClickableWidget element, int padding)
    {
        this.element = element;
        this.padding = padding;
    }

    @Override
    protected ClickableWidget initMainElement()
    {
        setDims();
        return element;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        GUIUtil.drawHashedScreenBorder(context, x, y, w, h);
        element.render(context, mouseX, mouseY, delta);
    }
}
