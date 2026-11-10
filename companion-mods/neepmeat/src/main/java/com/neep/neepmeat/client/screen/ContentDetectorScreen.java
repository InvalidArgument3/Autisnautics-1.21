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
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.machine.content_detector.InventoryDetector;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlockEntity;
import com.neep.neepmeat.screen_handler.ContentDetectorScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContentDetectorScreen extends BetterHandledScreen<ContentDetectorScreenHandler>
{
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/dispenser.png");
    private static final ResourceLocation BEHAVIOUR_WIDGETS = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/widget/detector_behaviour.png");

    @Nullable protected BehaviourButtonWidget behaviourButton;
    @Nullable protected CyclingButtonWidget countButton;

    public ContentDetectorScreen(ContentDetectorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        ScreenReady.ready();

        handler.updateButtons.receiver(this::updateButtons);
    }

    private void updateButtons(InventoryDetectorBlockEntity.CountMode countMode, InventoryDetectorBlockEntity.BehaviourMode behaviourMode)
    {
        if (behaviourButton != null && countButton != null)
        {
            countButton.setIndex(countMode.ordinal());
            behaviourButton.mode = behaviourMode;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    protected void init()
    {
        super.init();

        this.countButton = new CyclingButtonWidget(0, 0, 26, 16,
                0, 0, 26, 16, 16, 3, BEHAVIOUR_WIDGETS, 128, 128, Component.nullToEmpty("uwu"),
                (button) -> menu.buttonPress.emitter().press(0))
        {
            @Override
            public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
            {
                super.renderWidget(matrices, mouseX, mouseY, delta);
                if (isMouseOver(mouseX, mouseY))
                {
                    matrices.renderTooltip(font, Component.translatable("screen.neepmeat.content_detector.stack_condition"), mouseX, mouseY);
                }
            }
        };

//        this.behaviourButton = new CyclingButtonWidget(0, 0, 26, 16,
//                26, 0, 26, 16, 16, 1, BEHAVIOUR_WIDGETS, 128, 128, Text.of("uwu"),
//                (button) -> handler.buttonPress.emitter().press(1))
        this.behaviourButton = new BehaviourButtonWidget();

        var invSlots = addSlotBlock(new BeigeSlotBlockWidget(0, 0, 3, 3, menu.slots.subList(0, 9), this));

        var playerInv = addSlotBlock(new VanillaInventoryWidget(leftPos, topPos + imageWidth - PLCCols.INV_H + 1,
                menu.slots.subList(9, 9 + 4 * 9), this));

        var background = new HashedBackgroundPanel(
                new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setLeft(invSlots)
                        .setRight(new BlackBackgroundPanel(
                                new VStackPanel(2, 2, false)
                                        .addChild(countButton)
                                        .addChild(behaviourButton),
                                2)
                        )
                        .setDivide(new Divide.Abs(invSlots.nw() + 4))
        );

        background.setDims(leftPos, topPos, imageWidth, PLCCols.INV_H - 4);
        addRenderableWidget(background);
    }

    @Override
    protected void containerTick()
    {
//        behaviourButton.setIndex(handler.delegate.get(InventoryDetectorBehaviour.DEL_COUNT));
//        countButton.setIndex(handler.delegate.get(InventoryDetectorBehaviour.DEL_BEHAVIOUR));
    }

    protected class BehaviourButtonWidget extends NMButtonWidget implements Rectangle.Mutable
    {
        private InventoryDetector.BehaviourMode mode = InventoryDetector.BehaviourMode.REGULATE;

        public BehaviourButtonWidget()
        {
            super(0, 0, 26, 16, Component.empty(), b -> ContentDetectorScreen.this.menu.buttonPress.emitter().press(1));
            showBackground = false;
        }

        @Override
        public Component getMessage()
        {
            return Component.nullToEmpty(mode.toString());
        }

        @Override
        public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            super.renderWidget(matrices, mouseX, mouseY, delta);
            if (isMouseOver(mouseX, mouseY))
            {
                switch (mode)
                {
                    case REGULATE ->
                            matrices.renderComponentTooltip(font, List.of(
                                    Component.translatable("screen.neepmeat.content_detector.regulate"),
                                    Component.translatable("screen.neepmeat.content_detector.regulate.desc")), mouseX, mouseY);
                    case ABSOLUTE ->
                            matrices.renderComponentTooltip(font, List.of(Component.translatable("screen.neepmeat.content_detector.absolute"),
                                    Component.translatable("screen.neepmeat.content_detector.absolute.desc")), mouseX, mouseY);
                }
            }
        }
    }
}
