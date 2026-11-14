/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Invert toggles the entry. Tag switches it to the item's first item tag.
 */

package com.neep.neepmeat.machine.transport;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FilterPipeScreen extends AbstractContainerScreen<FilterPipeMenu> {
    public FilterPipeScreen(FilterPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 80;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            int index = i;
            addRenderableWidget(Button.builder(Component.literal("Invert"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index))
                    .bounds(leftPos + 44 + i * 18, topPos + 42, 16, 12).build());
            addRenderableWidget(Button.builder(Component.literal("Tag"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index + 3))
                    .bounds(leftPos + 44 + i * 18, topPos + 56, 16, 12).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF303030);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
