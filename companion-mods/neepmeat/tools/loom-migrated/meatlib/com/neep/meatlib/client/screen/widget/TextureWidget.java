/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.client.screen.GUIUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TextureWidget extends AbstractClickableWidget
{
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int du;
    private final int dv;
    private final int col;

    public TextureWidget(ResourceLocation texture, int u, int v, int du, int dv, int col)
    {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.du = du;
        this.dv = dv;
        this.col = col;

        this.width = du;
        this.height = dv;
    }

    @Override
    protected void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        GUIUtil.drawTexture(texture, context, x0(), y0(), u, v, du, dv, col);
    }
}
