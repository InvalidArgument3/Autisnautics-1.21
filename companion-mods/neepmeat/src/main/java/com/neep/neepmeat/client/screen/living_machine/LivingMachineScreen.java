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
import com.neep.meatlib.client.screen.auto.Divide;
import com.neep.meatlib.client.screen.auto.GeneralPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.live_machine.metrics.DataLog;
import com.neep.neepmeat.client.screen.util.Border;
import com.neep.neepmeat.screen_handler.LivingMachineScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LivingMachineScreen extends BetterHandledScreen<LivingMachineScreenHandler>
{
    private final GraphPane graphPane;

    private final MetricsPane metricsPane;
    private final ProcessesPane processesPane;
    private PaneWidget leftPane;

    public LivingMachineScreen(LivingMachineScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.metricsPane = new MetricsPane(handler, this);
        this.processesPane = new ProcessesPane(this);
        this.graphPane = new GraphPane(handler);

        leftPane = metricsPane;

        handler.syncGraph.receiver(this::receive);
    }

    void receive(DataLog.DataView data, List<Component> problems)
    {
        graphPane.update(data);
        metricsPane.updateProblems(problems);
    }

    @Override
    protected void init()
    {
        imageWidth = 400;
        imageHeight = 200;
        super.init();

        var mainBorder = addRenderableOnly(new Border(leftPos, topPos, imageWidth, imageHeight, 2, () -> PLCCols.BORDER.col));
        Rectangle withoutPadding = mainBorder.withoutPadding();
        float fraction = 0.4f;

        var main = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(leftPane)
                .setRight(graphPane)
                .setDivide(new Divide.Rel(0.4f));

        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        addRenderableOnly(main);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return leftPane.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {

    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        metricsPane.tick();
        graphPane.tick();
    }

    @Override
    public void onClose()
    {
        super.onClose();
    }

    public void switchMode()
    {
        if (leftPane == processesPane)
            leftPane = metricsPane;
        else
            leftPane = processesPane;

        rebuildWidgets();
    }

    public Font getTextRenderer()
    {
        return font;
    }

    static abstract class PaneWidget extends GeneralPanel
    {
        protected boolean focused;
        protected Border border;

        @Override
        public void init()
        {
            this.border = new Border(this, 0, () -> PLCCols.BORDER.col);
            super.init();
            clearChildren();
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.render(context, mouseX, mouseY, delta);
            if (border != null)
                border.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void setFocused(boolean focused)
        {
            this.focused = focused;
        }

        @Override
        public boolean isFocused()
        {
            return focused;
        }

        @Override
        public NarrationPriority narrationPriority()
        {
            return NarrationPriority.NONE;
        }
    }
}
