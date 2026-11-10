/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Upstream alloy kiln screen: the gui texture, the burn flame, and the progress
 * arrow. Slot positions match AlloyKilnScreenHandler.
 */

package com.neep.neepmeat.machine.alloy;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AlloyKilnScreen extends AbstractContainerScreen<AlloyKilnMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "textures/gui/alloy_kiln.png");

    public AlloyKilnScreen(AlloyKilnMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 176;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GUIUtil.drawTexture(TEXTURE, graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int burn = menu.burnTime();
        int fuel = menu.fuelTime();
        if (burn > 0) {
            int height = (int) ((burn / (fuel + 1f)) * 14);
            GUIUtil.drawTexture(TEXTURE, graphics, leftPos + 57, topPos + 37 + 12 - height, 176, 12 - height, 14, height + 1);
        }
        int cook = menu.cookTime();
        int total = menu.cookTimeTotal();
        if (cook > 0 && total > 0) {
            int width = (int) ((cook / (total + 1f)) * 24);
            GUIUtil.drawTexture(TEXTURE, graphics, leftPos + 80, topPos + 36, 176, 14, width, 17);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
