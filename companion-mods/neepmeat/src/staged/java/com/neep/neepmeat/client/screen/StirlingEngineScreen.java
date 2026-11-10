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

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.SnapPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.BurnSlotWidget;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.client.screen.util.HashedBackground;
import com.neep.neepmeat.screen_handler.StirlingEngineScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.function.Supplier;

@Environment(value = Dist.CLIENT)
public class StirlingEngineScreen extends BetterHandledScreen<StirlingEngineScreenHandler>
{
    private final StirlingEngineScreenHandler handler;

    public StirlingEngineScreen(StirlingEngineScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 90 + 1 + 90;

        super.init();

        var background = addRenderableOnly(new HashedBackground(leftPos, topPos, imageWidth, imageHeight - PLCCols.INV_H - 4, 6, true)).withoutPadding();

        var playerInv = addSlotBlock(new VanillaInventoryWidget(leftPos, topPos + imageWidth - PLCCols.INV_H + 1,
                handler.slots.subList(1, 1 + 4 * 9), this));

        var inv = addSlotBlock(new BurnSlotWidget(background.x0(), background.y0(), 1, 1, handler.slots.subList(0, 1), this,
                () -> handler.getProperty(0), () -> handler.getProperty(1)));


        SplitPanel mainSplit = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(new SnapPanel<>(inv))
                .setRight(
                        new BlackBackgroundPanel(new VStackPanel(1, 1, true)
//                                .addChild(new EnergyWidget(PowerUtil.ENERGY, () -> PowerUtil.formatPower(handler.getProperty(2), 0)))
                                .addChild(new EnergyWidget(PowerUtil.POWER, () -> PowerUtil.formatPower(handler.getProperty(3), 0)))
                        , 2));

        addRenderableWidget(mainSplit);

        mainSplit.setDims(background.x0(), background.y0(), background.nw(), background.nh());
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);
    }

    public static class EnergyWidget implements Renderable, Rectangle.Mutable, GuiEventListener
    {
        private int x;
        private int y;
        private int w;
        private int h;

        private final Supplier<String> value;
        private final Component label;
        private final Font textRenderer = Minecraft.getInstance().font;

        public EnergyWidget(Component label, Supplier<String> value)
        {
            this.x = 0;
            this.y = 0;
            this.value = value;
            this.label = label;
            this.w = 10;
            this.h = textRenderer.lineHeight * 2 + 2;
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
        public int nw()
        {
            return w;
        }

        @Override
        public int nh()
        {
            return h;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
//            Text label = PowerUtils.ENERGY;
            GUIUtil.drawText(context, textRenderer, label, x + 1, y + 1, PLCCols.TEXT.col, false);
            Component number = Component.nullToEmpty(value.get());
            int numberWidth = textRenderer.width(number);
            GUIUtil.drawText(context, textRenderer, number, x + w - numberWidth, y + textRenderer.lineHeight + 1, PLCCols.TEXT.col, false);
        }

        @Override
        public void setDims(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public void setFocused(boolean focused)
        {

        }

        @Override
        public boolean isFocused()
        {
            return false;
        }
    }
}
