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

package com.neep.meatlib.client.screen;

import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.SlotBlockWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Set;

import static net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.renderSlotHighlight;

public abstract class AbstractSlotBlockWidget implements SlotBlockWidget, Point.Mutable, Renderable
{
    protected final Minecraft client = Minecraft.getInstance();

    protected int x;
    protected int y;

    protected final List<MobileSlot> slots;

    protected final BetterHandledScreen<?> handler;

    public AbstractSlotBlockWidget(int x, int y, List<Slot> slots, BetterHandledScreen<?> handler)
    {
        this.x = x;
        this.y = y;

        this.slots = slots.stream().map(MobileSlot::new).toList();
        this.handler = handler;
    }

    public void init()
    {

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        for (MobileSlot slot : slots)
        {
            // Render the slot
            if (slot.forSlot().isActive())
            {
                this.renderSlot(context, slot);
            }

            if (slot.isWithin(mouseX, mouseY) && slot.forSlot().isActive())
            {
                handler.setFocusedSlot(slot.forSlot());
                int l = slot.x0();
                int m = slot.y0();
                if (handler.getFocusedSlot().isHighlightable())
                {
                    renderSlotHighlight(context, l, m, 0);
                }
            }
        }
    }

    protected static void setSlotPositions(int x, int y, List<MobileSlot> slots, int nx, int ny)
    {
        setSlotPositions(x, y, slots, nx, ny, 18);
    }

    protected static void setSlotPositions(int x, int y, List<MobileSlot> slots, int nx, int ny, int w)
    {
        int idx = 0;
        int m, l;
        for (m = 0; m < ny; ++m)
        {
            for (l = 0; l < nx; ++l)
            {
                slots.get(idx).setPos(x + l * w, y + m * w);
                ++idx;
            }
        }
    }

    protected void renderSlot(GuiGraphics context, MobileSlot mobileSlot)
    {
        Slot slot = mobileSlot.forSlot();
        Set<Slot> cursorDragSlots = handler.getCursorDragSlots();
        AbstractContainerMenu screenHandler = handler.getMenu();

        // Ignore the slot's server-specified position.
        int i = mobileSlot.x0();
        int j = mobileSlot.y0();

        ItemStack itemStack = slot.getItem();
        boolean bl = false;
        boolean bl2 = false;

        ItemStack carried = screenHandler.getCarried();
        String countOverride = null;

        if (handler.cursorDragging() && cursorDragSlots.contains(slot) && !carried.isEmpty())
        {
            if (cursorDragSlots.size() == 1)
            {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && screenHandler.canDragTo(slot))
            {
                bl = true;
                int k = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
                int l = itemStack.isEmpty() ? 0 : itemStack.getCount();
                int m = Math.min(k, Math.max(1, carried.getCount() / cursorDragSlots.size()) + l);
                if (m > k)
                {
                    m = k;
                    countOverride = ChatFormatting.YELLOW.toString() + k;
                }

                itemStack = carried.copyWithCount(m);
            }
            else
            {
                cursorDragSlots.remove(slot);
            }
        }

        context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, 100.0F);

        mobileSlot.render(context, itemStack, countOverride, bl, bl2);

        context.pose().popPose();
    }

    @Override
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int x0()
    {
        return x;
    }

    @Override
    public int y0()
    {
        return y;
    }

    @Override
    public Slot findSlot(double x, double y)
    {
        for (var slot : slots)
        {
            if (slot.isWithin(x, y))
                return slot.forSlot();
        }

        return null;
    }
}
