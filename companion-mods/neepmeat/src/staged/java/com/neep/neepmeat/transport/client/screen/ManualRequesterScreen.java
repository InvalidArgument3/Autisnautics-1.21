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

package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.SnapPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.screen.ManualRequesterScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import java.util.List;

@Environment(value = Dist.CLIENT)
public class ManualRequesterScreen extends BetterHandledScreen<ManualRequesterScreenHandler>
{
    private final ItemPane itemPane;

    public ManualRequesterScreen(ManualRequesterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.imageWidth = 12 * 18 + 20 + 5;
        this.imageHeight = 226;

        Minecraft client1 = Minecraft.getInstance();
        this.itemPane = new ItemPane(handler.requestC2S.emitter(), 12, 6, client1.getItemRenderer(), client1.font, handler.getItems(), Minecraft.getInstance());

        handler.syncItemsS2C.receiver(this::receiveItems);
    }

    @Override
    protected void init()
    {
        super.init();

        var playerInv = addSlotBlock(new VanillaInventoryWidget(menu.playerSlots, this));

        var returnSlot = addSlotBlock(new BeigeSlotBlockWidget(1,  1, menu.returnSlots, this)
                .overlay(GUIUtil.SCREEN_BORDER, 0, 218));

        var main = new HashedBackgroundPanel(new BlackBackgroundPanel(itemPane), 4);

        var craftingGrid = addSlotBlock(new BeigeSlotBlockWidget(3, 3, menu.craftingSlots, this));
        var craftingOutput = addSlotBlock(new BeigeSlotBlockWidget(1,  1, List.of(menu.craftingResultSlot), this)
                .overlay(GUIUtil.SCREEN_BORDER, 0, 196));

        var utilSlots = new HashedBackgroundPanel(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setFit(SplitPanel.Fit.FIRST)
                .setLeft(craftingGrid)
                .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setLeft(new SnapPanel<>(returnSlot))
                        .setRight(new SnapPanel<>((craftingOutput)))
                ));

        var mainVsplit = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
//                .setDivide(new Divide.Rel(Anchor.START, 0.25f))
                .setFit(SplitPanel.Fit.SECOND)
                .setLeft(main)
                .setRight(
                        new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setLeft(playerInv)
                        .setRight(utilSlots)
                        .setFit(SplitPanel.Fit.FIRST))
                .centre(false)
                ;

        mainVsplit.setDims(leftPos, topPos, imageWidth, imageHeight);
        addRenderableWidget(mainVsplit);

//        returnSlot.setPos(playerInv.x2(), playerInv.y2() - returnSlot.h() - 2);

        ScreenReady.ready();
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
//        matrices.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType)
    {
        super.slotClicked(slot, slotId, button, actionType);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (itemPane.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        return itemPane.charTyped(chr, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics matrices, int mouseX, int mouseY)
    {
//        this.textRenderer.draw(matrices, this.title, this.playerInventoryTitleX, this.titleY, 0x404040);
    }

    public void receiveItems(List<RoutingNetwork.Entry> items)
    {
        menu.receiveItems(items);
        itemPane.updateSearch();
    }
}