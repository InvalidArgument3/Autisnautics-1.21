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

package com.neep.neepmeat.client.screen;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.button.TextToggleButtonWidget;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.neepmeat.screen_handler.DeployerScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DeployerScreen extends BetterHandledScreen<DeployerScreenHandler>
{
    private boolean roundRobin;
    private boolean interactBlock;

    public DeployerScreen(DeployerScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        handler.updateS2CC2S.receiver(this::receiveValues);

        ScreenReady.ready();
    }

    private void receiveValues(boolean roundRobin, boolean interactBlock)
    {
        this.roundRobin = roundRobin;
        this.interactBlock = interactBlock;
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 90 + 1 + 90;

        super.init();

        var playerInv = addSlotBlock(new VanillaInventoryWidget(leftPos, topPos + imageWidth - PLCCols.INV_H + 1,
                menu.slots.subList(9, 9 + 4 * 9), this));

        var inv = addSlotBlock(new BeigeSlotBlockWidget(0, 0, 3, 3, menu.slots.subList(0, 9), this));

        VStackPanel rightPanel = new VStackPanel(1, 1, false);

        SplitPanel mainSplit = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(inv)
                .setRight(new BlackBackgroundPanel(rightPanel, 2));
        mainSplit.setDivide(new Divide.Abs(inv.nw() + 4));

        var background = new HashedBackgroundPanel(mainSplit);

        rightPanel.addChild(new CheckboxWidget(50, 13, () -> roundRobin, NeepMeat.translationKey("screen", "deployer.round_robin"), (button, toggled) ->
                menu.updateS2CC2S.emitter().update(!roundRobin, interactBlock)));
        rightPanel.addChild(new TextToggleButtonWidget(background.x0() + 5, inv.y0(), 50, 13, () -> interactBlock, NeepMeat.translationKey("screen", "deployer.block"), (button, toggled) ->
                menu.updateS2CC2S.emitter().update(roundRobin, !interactBlock))
                .showBackground(false));

        background.setDims(leftPos, topPos, imageWidth, PLCCols.INV_H - 4);
        addRenderableWidget(background);

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button)
//    {
//        this.setDragging(false);
//
//        super.mouseReleased(mouseX, mouseY, button);
//
//        if (getSlotAt(mouseX, mouseY) != null)
//            return true;
//
//        return this.hoveredElement(mouseX, mouseY).filter((element) ->
//                element.mouseReleased(mouseX, mouseY, button)).isPresent();
//    }
}
