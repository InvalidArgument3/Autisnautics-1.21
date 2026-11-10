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

package com.neep.neepmeat.transport.client.screen.filter;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.mixin.client.AbstractParentElementAccessor;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class FilterScreen extends BetterHandledScreen<FilterScreenHandler>
{
    private Panel mainPanel = null;
    private VanillaInventoryWidget playerInv;

    public FilterScreen(FilterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        this.imageWidth = FilterScreenHandler.BACKGROUND_WIDTH;
        this.imageHeight = FilterScreenHandler.BACKGROUND_HEIGHT;

        super.init();

        if (mainPanel == null)
        {
            playerInv = new VanillaInventoryWidget(leftPos, topPos + imageHeight - PLCCols.INV_H,
                    menu.slots.subList(0, 36), this);

            var filterPanel = new FilterPanel(menu.filterHandler, menu::getCarried);

            var filterSplit = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                    .setLeft(filterPanel)
                    .setRight(new FilterButtonsPanel(menu.filterHandler, 1, 1))
                    .setDivide(new Divide.Abs(imageWidth - 16 - 16));

            var background = new HashedBackgroundPanel(new BlackBackgroundPanel(filterSplit, 3), 3);

            mainPanel = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                    .centre(true)
                    .setDivide(new Divide.Abs(imageHeight - PLCCols.INV_H))
                    .setLeft(background)
                    .setRight(playerInv);
        }

        mainPanel.setDims(leftPos, topPos, imageWidth, imageHeight);

        addSlotBlock(playerInv);
        addRenderableWidget(mainPanel);

        ScreenReady.ready();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc())
        {
            this.onClose();
            return true;
        }

        if (mainPanel.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (minecraft.options.keyInventory.matches(keyCode, scanCode) && this.shouldCloseOnEsc())
        {
            this.onClose();
            return true;
        }

        return false;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused)
    {
        AbstractParentElementAccessor accessor = (AbstractParentElementAccessor) this;
        if (accessor.getFieldFocused() != null && accessor.getFieldFocused() != focused)
            accessor.getFieldFocused().setFocused(false);

        if (focused != null)
        {
            focused.setFocused(true);
        }

        accessor.setFieldFocused(focused);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
