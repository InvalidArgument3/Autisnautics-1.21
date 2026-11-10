/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The shell, editor, and projector HUD from the Fabric screen stay out until the
 * guide and projector item exist. This screen edits the stored source and runs it.
 */

package com.neep.neepmeat.client.screen.plc;

import com.neep.meatlib.client.screen.NMTextField;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.screen.PLCMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PLCScreen extends AbstractContainerScreen<PLCMenu> {
    private NMTextField field;
    private final String initialSource;
    private final String initialError;

    public PLCScreen(PLCMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 120;
        this.initialSource = menu.source();
        this.initialError = menu.error();
    }

    @Override
    protected void init() {
        super.init();
        field = new NMTextField(font, leftPos + 8, topPos + 24, imageWidth - 16, 20, Component.literal(initialSource));
        field.setText(initialSource);
        addRenderableWidget(field);
        addRenderableWidget(new NMButtonWidget(leftPos + 8, topPos + 52, 60, 20, Component.literal("Run"), button ->
                PLCBlockEntity.sendSource(menu.pos(), field.text(), true)));
        addRenderableWidget(new NMButtonWidget(leftPos + 72, topPos + 52, 60, 20, Component.literal("Stop"), button ->
                PLCBlockEntity.sendSource(menu.pos(), field.text(), false)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF101018);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFE0E0E0, false);
        if (!initialError.isEmpty()) {
            graphics.drawString(font, initialError, 8, 80, 0xFFFF5555, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
