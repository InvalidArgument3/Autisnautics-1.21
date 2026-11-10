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
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.meatlib.mixin.client.AbstractParentElementAccessor;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import com.neep.neepmeat.transport.screen.ProcessInterfaceScreenHandler;
import com.neep.neepmeat.transport.screen.widget.ProcessInterfaceWidgetHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ProcessInterfaceScreen extends BetterHandledScreen<ProcessInterfaceScreenHandler>
{
    private final ValuesConfigPanel configPanel;

    public ProcessInterfaceScreen(ProcessInterfaceScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        this.configPanel = new ValuesConfigPanel(handler.widgetHandler);
    }

    @Override
    protected void init()
    {
        this.imageWidth = FilterScreenHandler.BACKGROUND_WIDTH + 30;
        this.imageHeight = 170;

        super.init();

        var playerInv = addSlotBlock(new VanillaInventoryWidget(leftPos, topPos + imageHeight - PLCCols.INV_H, menu.playerSlots, this));
        var resultSlots = addSlotBlock(new BeigeSlotBlockWidget(3, 3, menu.resultSlots, this));

        var background = new HashedBackgroundPanel(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(new SnapPanel<>(new LabeledPanel(Component.nullToEmpty("Process Cards"), addSlotBlock(new BeigeSlotBlockWidget(3, 6, menu.cardSlots,this))
                    .overlay(GUIUtil.SCREEN_BORDER, 71, 238))))
                .setRight(new BlackBackgroundPanel(configPanel))
                .setDivide(new Divide.Rel(0.48f)), 3);

        var mainPanel = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setDivide(new Divide.Abs(imageHeight - PLCCols.INV_H))
                .setLeft(background)
                .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setFit(SplitPanel.Fit.FIRST)
                        .setLeft(playerInv)
                        .setRight(new HashedBackgroundPanel(
                                new SnapPanel<>((new LabeledPanel(Component.nullToEmpty("Results"), resultSlots))))
                ));

        addRenderableWidget(mainPanel);
        mainPanel.setDims(leftPos, topPos, imageWidth, imageHeight);

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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private static class ValuesConfigPanel extends ConfigPanel<ProcessInterfaceWidgetHandler.Values>
    {
        private boolean useInterruptsState;
        private boolean delayInterruptsState;
        private boolean recordQueueState;
//        private ProcessInterfaceWidgetHandler.Values values = new ProcessInterfaceWidgetHandler.Values(false, false, false);

        public ValuesConfigPanel(ConfigWidgetHandler<ProcessInterfaceWidgetHandler.Values> handler)
        {
            super(handler);
            addTrackedField(new CheckboxWidget(100, PLCCols.ENTRY_H, () -> useInterruptsState,
                    NeepMeat.translationKey("screen", "process_interface.use_interrupts"),
                    (w, b) -> {
                        useInterruptsState = b;
                        update();
                    })
                    .tooltip(NeepMeat.translationKey("screen", "process_interface.use_interrupts.tooltip")));

            addTrackedField(new CheckboxWidget(100, PLCCols.ENTRY_H, () -> delayInterruptsState,
                    NeepMeat.translationKey("screen", "process_interface.delay_interrupts"),
                    (w, b) -> {
                        delayInterruptsState = b;
                        update();
                    })
                    .tooltip(NeepMeat.translationKey("screen", "process_interface.delay_interrupts.tooltip")));

            addTrackedField(new CheckboxWidget(100, PLCCols.ENTRY_H, () -> recordQueueState,
                    NeepMeat.translationKey("screen", "process_interface.record_queue"),
                    (w, b) -> {
                        recordQueueState = b;
                        update();
                    })
                    .tooltip(NeepMeat.translationKey("screen", "process_interface.record_queue.tooltip")));
        }

        @Override
        protected ProcessInterfaceWidgetHandler.Values createValues()
        {
            return new ProcessInterfaceWidgetHandler.Values(useInterruptsState, delayInterruptsState, recordQueueState);
        }

        @Override
        protected void updateParams(ProcessInterfaceWidgetHandler.Values values)
        {
            this.useInterruptsState = values.useInterrupts();
            this.delayInterruptsState = values.delayInterrupts();
            this.recordQueueState = values.recordQueue();
//            this.values = values;
        }
    }
}
