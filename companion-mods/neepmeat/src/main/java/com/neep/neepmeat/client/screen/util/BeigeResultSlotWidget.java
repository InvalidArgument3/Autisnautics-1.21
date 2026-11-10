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

package com.neep.neepmeat.client.screen.util;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.SimpleSlotBlockWidget;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public class BeigeResultSlotWidget extends SimpleSlotBlockWidget
{
    public BeigeResultSlotWidget(Slot slot, BetterHandledScreen<?> handler)
    {
        super(0, 0, 1, 1, List.of(slot), handler);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
    }

    protected void drawSlot(GuiGraphics context, MobileSlot mobileSlot)
    {
        GUIUtil.drawTexture(GUIUtil.SCREEN_BORDER, context, x0(), y0(), 20, 228, nw(), nh());

        super.drawSlot(context, mobileSlot);
    }

    @Override
    protected void setSlotPositions()
    {
        setSlotPositions(x0() + 6, y0() + 6, slots, 1, 1);
    }

    @Override
    public int nw()
    {
        return 28;
    }

    @Override
    public int nh()
    {
        return 28;
    }
}
