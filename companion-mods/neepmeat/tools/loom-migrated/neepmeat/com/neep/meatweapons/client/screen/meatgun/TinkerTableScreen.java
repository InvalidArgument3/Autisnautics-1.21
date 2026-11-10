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

package com.neep.meatweapons.client.screen.meatgun;

import com.neep.meatlib.client.screen.NMTextField;
import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.slot.OutlineInventoryWidget;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.config.StringConfigField;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.screen.TinkerTableScreenHandler;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.client.screen.util.BorderSlotBlockWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TinkerTableScreen extends BetterHandledScreen<TinkerTableScreenHandler>
{
    public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(MeatWeapons.NAMESPACE, "textures/gui/tinker_table/widgets.png");
    private final DisplayPane displayPane;
    private final TreePane treePane;
    private final StringConfigField nameField;

    public TinkerTableScreen(TinkerTableScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        treePane = new TreePane(handler, handler.itemSlot);
        displayPane = new DisplayPane(handler.itemSlot);
        nameField = new StringConfigField(font, 0, 0, 100, 18, Component.empty(), f ->
        {
            handler.setNameC2SS2C.emitter().apply(f.parse());
            f.dirty = false;
        });
        nameField.tooltip(() -> List.of(Component.nullToEmpty("Click to rename" + (nameField.dirty ? "\n(modified)" : ""))));

        handler.setNameC2SS2C.receiver(nameField::setTextSilent);
    }

    @Override
    protected void init()
    {
        imageWidth = TinkerTableScreenHandler.BACKGROUND_WIDTH;
        imageHeight = TinkerTableScreenHandler.BACKGROUND_HEIGHT;
        super.init();

        var playerInv = addSlotBlock(new OutlineInventoryWidget(menu.playerSlots, this, false));
        var itemSlot = addSlotBlock(new ItemBorderSlot(menu.itemSlot, this));

        var buttons = new SnapPanel<>(
                SnapPanel.Snap.BOTTOM,
                new VStackPanel(0, 1, true, true)
                    .addChild(new OpenImplantConfigButton(0, 0))
                    .addChild(itemSlot));

        var inventoryAndButtons = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(buttons)
                .setRight(playerInv)
                .setFit(SplitPanel.Fit.FIRST)
                .separation()
                ;

        var panes = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(
                        new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                            .setLeft(displayPane)
                            .setRight(inventoryAndButtons)
                            .setFit(SplitPanel.Fit.SECOND)
                            .separation()
                )
                .setRight(treePane)
//                        new SplitPanel(SplitPanel.Split.VERTICAL, 0)
//                                .setLeft(nameField)
//                                .setRight(treePane)
//                                .setDivide(new Divide.Abs(PLCCols.ENTRY_H))
//                )
                .setDivide(new Divide.Abs(Anchor.START, 185))
                .separation()
        ;

        var main = new HashedBackgroundPanel(new BlackBackgroundPanel(
                panes
        ));

        addWidget(nameField);
        addRenderableWidget(main);
        addRenderableWidget(nameField);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);
        nameField.setDims(displayPane.x0() + 1, displayPane.y0(), displayPane.nw(), PLCCols.ENTRY_H - 1);

        ScreenReady.ready();
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        super.renderBackground(context);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (treePane.isWithin(mouseX, mouseY))
        {
            return treePane.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        else if (displayPane.isWithin(mouseX, mouseY) && !nameField.isWithin(mouseX, mouseY))
        {
            return displayPane.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onClose()
    {
        menu.setNameC2SS2C.emitter().apply(nameField.parse());
        super.onClose();
    }

    @Override
    protected void containerTick()
    {
        treePane.tick();
        displayPane.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    static abstract class PaneWidget extends AbstractClickableWidget implements Rectangle.Mutable, ScreenInitialisable
    {
        protected boolean focused;

        @Override
        public void setDims(int x, int y, int w, int h)
        {
            setPos(x, y);
            this.width = w;
            this.height = h;
            init();
        }

        @Override
        public void init()
        {
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, this, PLCCols.BORDER.col, 0);
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

    private static class ItemBorderSlot extends BorderSlotBlockWidget
    {
        private final Slot slot;
        private final Component tooltip = Component.translatable("text.meatweapons.tinker_table.item_here");

        public ItemBorderSlot(Slot slot, BetterHandledScreen<?> handler)
        {
            super(0, 0, 1, 1, List.of(slot), handler);
            this.slot = slot;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.render(context, mouseX, mouseY, delta);

            if (!slot.hasItem())
            {
                GUIUtil.drawTexture(WIDGETS_TEXTURE, context, x + 2, y + 2, 19, 1, 16, 16);
                if (isWithin(mouseX, mouseY))
                {
                    TooltipHelper.get().draw(context, mouseX, mouseY, List.of(tooltip));
                }
            }
        }
    }

    protected class OpenImplantConfigButton extends PLCScreenButton
    {
        public OpenImplantConfigButton(int x, int y)
        {
            super(x, y, Component.empty());
            tooltip(Component.translatable("screen.neepmeat.implant_manager.open"));
        }

        @Override
        protected int getU()
        {
            return 96;
        }

        @Override
        protected int getV()
        {
            return 48;
        }

        @Override
        protected int getYImage(boolean hovered)
        {
            if (menu.itemSlot.getItem().isEmpty())
                return 0;

            return super.getYImage(hovered);
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            menu.openImplantConfigC2S.emitter().run();
        }
    }
}
