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
import com.neep.meatlib.client.screen.GUIUtil;
import net.minecraft.client.gui.GuiGraphics;

import static com.neep.meatlib.client.screen.button.NMButtonWidget.NM_WIDGETS_TEXTURE;

public class BlackBackgroundPanel extends SimplePanel
{
    public BlackBackgroundPanel(ClickableWidget element)
    {
        this(element, 3);
    }

    public BlackBackgroundPanel(ClickableWidget element, int padding)
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
    public void setDims(int x, int y, int w, int h)
    {
        super.setDims(x, y, w, h);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
//        GUIUtil.drawNineSlicedTexture(context, NM_WIDGETS_TEXTURE, x - padding, y - padding, w + 2 * padding, h + 2 * padding, 4, 200, 20, 0, 90);
        GUIUtil.blitNineSliced(context, NM_WIDGETS_TEXTURE, x, y, w, h, 4, 200, 20, 0, 90);

        super.render(context, mouseX, mouseY, delta);
    }
}
