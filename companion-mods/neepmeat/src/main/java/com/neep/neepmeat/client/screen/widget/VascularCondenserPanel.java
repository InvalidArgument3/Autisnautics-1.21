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

package com.neep.neepmeat.client.screen.widget;

import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.SimplePanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.screen_handler.widget.VascularCondenserWidgetHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import static com.neep.meatlib.theme.PLCCols.ENTRY_H;

public class VascularCondenserPanel extends SimplePanel
{
    private final VascularCondenserWidgetHandler handler;

    private final PowerDisplayPanel displayPanel;

    private boolean destroy;

    public VascularCondenserPanel(VascularCondenserWidgetHandler handler)
    {
        this.handler = handler;

        displayPanel = new PowerDisplayPanel();

        handler.amountS2C.receiver(this::update);

        element = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(new BlackBackgroundPanel(
                        new ConfigPanel1(handler), 3)
                )
                .setRight(new BlackBackgroundPanel(displayPanel, 3).solidW())
                .setFit(SplitPanel.Fit.SECOND);

//        OpenNEEPBusConfigButton neepbus = new OpenNEEPBusConfigButton(handler.openNeepbus.emitter());
    }

    private void update(long amount, long capacity, long networkInternal)
    {
        displayPanel.update(amount, capacity, networkInternal);
    }

    @Override
    protected ClickableWidget initMainElement()
    {
        setDims();
        return element;
    }

    private static class ConfigPanel1 extends ConfigPanel<VascularCondenserWidgetHandler.Values>
    {
        private final IntNumberTextField targetField;
        boolean destroy;

        public ConfigPanel1(ConfigWidgetHandler<VascularCondenserWidgetHandler.Values> handler)
        {
            super(handler);

            targetField = addTrackedField(new IntNumberTextField(textRenderer, 0, 0, 100, ENTRY_H, Component.nullToEmpty("Target (eJ/t):"),
                    c -> update(), 0, Integer.MAX_VALUE, 10, 100));
            targetField.tooltip(NeepMeat.translationKey("screen", "vascular_condenser.target.tooltip"));

            CheckboxWidget destroyExcess = new CheckboxWidget(100, ENTRY_H, () -> destroy, Component.nullToEmpty("Dissipate excess power"),
                    (button, toggled) -> destroy = toggled);
            destroyExcess.tooltip(NeepMeat.translationKey("screen", "vascular_condenser.destroy.tooltip"));

            addTrackedField(destroyExcess);
        }

        @Override
        protected VascularCondenserWidgetHandler.Values createValues()
        {
            return new VascularCondenserWidgetHandler.Values(targetField.parse(), destroy);
        }

        @Override
        protected void updateParams(VascularCondenserWidgetHandler.Values values)
        {
            targetField.setNumberText(values.target());
            destroy = values.destroy();
        }
    }

    public static class PowerDisplayPanel extends Panel
    {
        private long amount;
        private long capacity;
        private long networkInternal;

        public PowerDisplayPanel()
        {
            w = 14;
        }

        @Override
        public void init()
        {
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, x, y, w, h, PLCCols.BORDER.col, 0);

            double f = capacity != 0 ? amount / (double) capacity : 0;

            int bh = (int) (f * h);

            for (int i = 0; i < bh; i++)
            {
                if ((i + 1) % 2 == 0)
                {
                    GUIUtil.drawHorizontalLine1(context, x + 2, x + w - 3, y + h - i, PLCCols.BORDER.col);
                }
                else
                {
//                    GUIUtil.drawHorizontalLine1(context, x + 1, x + w - 2, y + h - i, PLCCols.TRANSPARENT.col);
                }
            }

            if (isWithin(mouseX, mouseY))
            {
                List<Component> texts = new ObjectArrayList<>();
                texts.add(Component.nullToEmpty( PowerUtil.formatEnergy(amount, 2) + " / " + PowerUtil.formatEnergy(capacity, 2)));
                texts.add(Component.nullToEmpty("Network capacity: " + PowerUtil.formatEnergy(networkInternal, 2)));

                TooltipHelper.get().draw(context, mouseX, mouseY, texts);
            }
        }

        @Override
        protected List<Renderable> drawables()
        {
            return List.of();
        }

        @Override
        public List<GuiEventListener> children()
        {
            return List.of();
        }

        public void update(long amount, long capacity, long networkInternal)
        {
            this.amount = amount;
            this.capacity = capacity;
            this.networkInternal = networkInternal;
        }
    }
}
