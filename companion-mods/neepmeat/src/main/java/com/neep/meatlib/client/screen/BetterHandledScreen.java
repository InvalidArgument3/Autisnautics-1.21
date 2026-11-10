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

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.client.event.BetterHandledScreenRenderEvent;
import com.neep.meatlib.client.screen.auto.CloseListener;
import com.neep.meatlib.client.screen.slot.SlotBlockWidget;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Enables widget-based placement of slots while preserving Vanilla behaviour.
 * In order to render slots, they must be provided by a {@link SlotBlockWidget}
 */
public abstract class BetterHandledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
//    private final HandledScreenAccessor accessor = (HandledScreenAccessor) this;
    private final List<SlotBlockWidget> slotBlocks = new ObjectArrayList<>();

    public BetterHandledScreen(T handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        // Can't be bothered to wait for init().
        this.font = Minecraft.getInstance().font;
    }

    /**
     * Use this to add slot widgets.
     * Slot widgets should be added with addSelectableChild if they are to be interactive widgets.
     */
    protected <B extends SlotBlockWidget> B addSlotBlock(B block)
    {
        slotBlocks.add(block);
        return block;
    }

    @Override
    public void onClose()
    {
        // Notify the elements before the screen close packet is sent
        for (var element : children())
        {
            if (element instanceof CloseListener closeListener)
            {
                closeListener.close();
            }
        }

        super.onClose();
    }

    @Override
    public void removed()
    {
        // removed() no longer closes the screen handler.
        // This allows EMI screens to replace the current screen without closing the packet handlers.
        // TODO: make this system actually work
//        if (handler instanceof BasicScreenHandler handler1)
//        {
//            handler1.onRemoved();
//        }

        super.removed();
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        int i = this.leftPos;
        int j = this.topPos;

        this.renderBg(context, delta, mouseX, mouseY);
        BetterHandledScreenRenderEvent.background(this, context, leftPos, topPos, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();

        this.hoveredSlot = null;
        for (var block : slotBlocks)
        {
            block.render(context, mouseX, mouseY, delta);
        }

        for (var drawable : renderables)
        {
            drawable.render(context, mouseX, mouseY, delta);
            if (drawable instanceof TooltipRenderer toot)
            {
                toot.renderTooltipLayer(context, mouseX, mouseY, delta);
            }
        }

        context.pose().pushPose();
        context.pose().translate((float) i, (float) j, 0.0F);
        this.renderLabels(context, mouseX, mouseY);
        BetterHandledScreenRenderEvent.foreground(this, context, leftPos, topPos, mouseX, mouseY, delta);

        ItemStack carried = this.menu.getCarried();
        if (!carried.isEmpty())
        {
            this.drawItem(context, carried, mouseX - i - 8, mouseY - j - 8, null);
        }

        context.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void init()
    {
        slotBlocks.clear();
        super.init();
    }

    // Vanilla behaviour
    private void drawItem(GuiGraphics context, ItemStack stack, int x, int y, String amountText)
    {
        context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, 232.0F);
        context.renderItem(stack, x, y);
        context.renderItemDecorations(font, stack, x, y, amountText);
        context.pose().popPose();
    }

    // Access widened
    // Most of the vanilla slot behaviour gets the slot through this method.
    @Override
    @Nullable
    public Slot findSlot(double x, double y)
    {
        for (var block : slotBlocks)
        {
            Slot slot = block.findSlot(x, y);
            if (slot != null)
                return slot;
        }
        return super.findSlot(x, y);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context, mouseX, mouseY, delta);
    }

    public boolean cursorDragging()
    {
        return isQuickCrafting;
    }

    public Set<Slot> getCursorDragSlots()
    {
        return quickCraftSlots;
    }

    public void setFocusedSlot(Slot slot)
    {
        this.hoveredSlot = slot;
    }

    @Nullable
    public Slot getFocusedSlot()
    {
        return hoveredSlot;
    }
}
