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

package com.neep.neepmeat.client.screen.living_machine;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.SnapPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.screen_handler.ItemInputScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemInputPortScreen extends BetterHandledScreen<ItemInputScreenHandler>
{
    private final BeigeSlotBlockWidget invSlots;

    private final Panel mainWidget;
    private final VanillaInventoryWidget vanillaInventory;

    public ItemInputPortScreen(ItemInputScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        invSlots = new BeigeSlotBlockWidget(0, 0, 3, 3, handler.slots.subList(0, 9), this);
        vanillaInventory = new VanillaInventoryWidget(0, 0, handler.slots.subList(9, 9 + 4 * 9), this);

        mainWidget = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setLeft(new HashedBackgroundPanel(new SnapPanel<>(invSlots)))
                .setRight(vanillaInventory);
    }

    @Override
    protected void init()
    {
        super.init();
        addSlotBlock(invSlots);
        addSlotBlock(vanillaInventory);

        addRenderableWidget(mainWidget);

        mainWidget.setDims(leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
