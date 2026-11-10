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

package com.neep.neepmeat.client.screen.util;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.NeepMeat;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.resources.ResourceLocation;

public class OldBackground implements Renderable
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(NeepMeat.NAMESPACE, "textures/gui/widget/borders.png");

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final int padding;
    private final Supplier<Integer> col;

    public OldBackground(int x, int y, int w, int h, int padding, Supplier<Integer> col)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.padding = padding;
        this.col = col;
    }

    public OldBackground(Rectangle bounds, int padding, Supplier<Integer> col)
    {
        this(bounds.x0(), bounds.y0(), bounds.nw(), bounds.nh(), padding, col);
    }

    public Rectangle withoutPadding()
    {
        return new Rectangle.ImmutableRect(x + padding, y + padding, w - padding * 2, h - padding * 2);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        int t = 5; // Border thickness

        // For reasons that I can't be bothered to investigate, it is necessary to extend the width and height by 1
        // to fit all elements.
        int x2 = x + w - t + 1;
        int y2 = y + h - t + 1;
        int a = 1;

        drawTexture(context, x, y, t, t, 0, 0, t, t);
        drawTexture(context, x, y2, t, t, 0, 11, t, t);
        drawTexture(context, x2, y, t, t, 11, 0, t, t);
        drawTexture(context, x2, y2, t, t, 11, 11, t, t);

        drawTexture(context, x + t, y, w - 2 * t + a, t, 5, 0, 1, 5);
        drawTexture(context, x, y + t, t, h - 2 * t + a, 0, 6, 5, 1);
        drawTexture(context, x + t, y2, w - 2 * t + a, t, 6, 11, 1, 5);
        drawTexture(context, x2, y + t, t, h - 2 * t + a, 11, 6, 5, 1);

        drawTexture(context, x + t, y + t, w - 2 * t, h - 2 * t, 7, 7, 1, 1);
    }

    private void drawTexture(GuiGraphics context, int x, int y, int w, int h, int u, int v, int du, int dv)
    {
        GUIUtil.drawTextureStretch(TEXTURE, context, x, y, w, h, u, v, du, dv, 32, 32);
    }

    public int padding()
    {
        return padding;
    }
}
