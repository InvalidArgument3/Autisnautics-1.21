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

package com.neep.meatlib.client.screen.slot;

import com.neep.meatlib.client.screen.AbstractSlotBlockWidget;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SimpleSlotBlockWidget extends AbstractSlotBlockWidget implements Rectangle, ClickableWidget
{
    private boolean focused;
    protected final int rows;
    protected final int cols;
    @Nullable protected ResourceLocation overlay;
    protected int overlayU;
    protected int overlayV;
    protected Supplier<List<Component>> tooltip = () -> List.of();

    public SimpleSlotBlockWidget(int x, int y, int rows, int cols, List<Slot> slots, BetterHandledScreen<?> handler)
    {
        super(x, y, slots, handler);

        if (slots.size() != rows * cols)
            throw new IllegalArgumentException("Slot block size does not match rows and cols");

        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        // tODO: (not necessary to do this every frame, but I can't think of anywhere else to do it)
        setSlotPositions();

        super.render(context, mouseX, mouseY, delta);
//        GUIUtil.renderBorderInner(context, x(), y(), w(), h(), PLCCols.TEXT.col, 0);
    }

    @Override
    protected void renderSlot(GuiGraphics context, MobileSlot mobileSlot)
    {
        super.renderSlot(context, mobileSlot);
        if (overlay != null)
            // Why x0 - 2 instead of x0 - 1? I don't know. It just works.
            GUIUtil.drawTexture(overlay, context, mobileSlot.x0() - 1, mobileSlot.y0() - 1, overlayU, overlayV, mobileSlot.nw() + 2, mobileSlot.nh() + 2);
    }

    // Override to add padding
    protected void setSlotPositions()
    {
        setSlotPositions(x, y, slots, cols, rows);
    }

    @Override
    public int nw()
    {
        // Subtract 2 as the slots share borders... I think... Why is this so hard?
        return cols * 18 - 2;
    }

    @Override
    public int nh()
    {
        return rows * 18 - 2;
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }

    @Override
    public boolean isFocused()
    {
        return focused;
    }

    public SimpleSlotBlockWidget overlay(ResourceLocation texture, int u, int v)
    {
        this.overlay = texture;
        this.overlayU = u;
        this.overlayV = v;
        return this;
    }
}
