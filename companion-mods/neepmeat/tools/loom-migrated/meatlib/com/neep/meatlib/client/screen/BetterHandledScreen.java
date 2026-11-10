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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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

        // Background
        this.renderBg(context, delta, mouseX, mouseY);
        BetterHandledScreenRenderEvent.BACKGROUND.invoker().render(this, context, leftPos, topPos, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();

        // Render slots at the same time as normal widgets
        this.hoveredSlot = null;
        for (var block : slotBlocks)
        {
            // This may cause changes to the screen (dragging, clicking, etc.)
            block.render(context, mouseX, mouseY, delta);
        }

        for (var drawable : renderables)
        {
            drawable.render(context, mouseX, mouseY, delta);

            if (drawable instanceof TooltipRenderer toot)
            {
//                context.draw();
                toot.renderTooltipLayer(context, mouseX, mouseY, delta);
            }
        }

        context.pose().pushPose();
        context.pose().translate((float) i, (float) j, 0.0F);

        // Foreground
        this.renderLabels(context, mouseX, mouseY);
        BetterHandledScreenRenderEvent.FOREGROUND.invoker().render(this, context, leftPos, topPos, mouseX, mouseY, delta);

        // Render cursor stack (vanilla behaviour)
        ItemStack itemStack = draggingItem.isEmpty() ? this.menu.getCarried() : draggingItem;
        if (!itemStack.isEmpty())
        {
            int l = draggingItem.isEmpty() ? 8 : 16;
            String string = null;
            if (!touchDragStack().isEmpty() && isSplittingStack)
            {
                itemStack = itemStack.copyWithCount(Mth.ceil((float) itemStack.getCount() / 2.0F));
            }
            else if (isQuickCrafting && quickCraftSlots.size() > 1)
            {
                itemStack = itemStack.copyWithCount(quickCraftingRemainder);
                if (itemStack.isEmpty())
                {
                    string = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(context, itemStack, mouseX - i - 8, mouseY - j - l, string);
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
    private void renderFloatingItem(GuiGraphics context, ItemStack stack, int x, int y, String amountText)
    {
        context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, 232.0F);
        context.renderItem(stack, x, y);
        context.renderItemDecorations(font, stack, x, y - (draggingItem.isEmpty() ? 0 : 8), amountText);
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
            Slot slot = block.getSlotAt(x, y);
            if (slot != null)
                return slot;
        }

        return null;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType)
    {
        super.slotClicked(slot, slotId, button, actionType);
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

    public Slot touchDragSlotStart()
    {
        return clickedSlot;
    }

    public ItemStack touchDragStack()
    {
        return draggingItem;
    }

    public boolean touchIsRightClickDrag()
    {
        return isSplittingStack;
    }

    public int heldButtonType()
    {
        return quickCraftingType;
    }
}
