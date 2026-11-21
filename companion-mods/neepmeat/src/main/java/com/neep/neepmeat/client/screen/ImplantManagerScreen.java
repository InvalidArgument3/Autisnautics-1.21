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
import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.slot.OutlineInventoryWidget;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.implant.ImplantAttributes;
import com.neep.neepmeat.client.implant.ImplantConfigClient;
import com.neep.neepmeat.client.implant.ImplantConfigPanel;
import com.neep.neepmeat.client.screen.widget.MultiLineConfigTextField;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.entity.ImplantRegistry;
import com.neep.neepmeat.implant.item.ItemImplantManager;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImplantManagerScreen extends BetterHandledScreen<ImplantManagerScreenHandler>
{
    @Nullable private ResourceLocation selected;
    @Nullable private ImplantManager implantManager;
    @Nullable private MutateInPlace<?> mip;

    @Nullable private Panel configPanel;

    public ImplantManagerScreen(ImplantManagerScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 360;
        this.imageHeight = 200;

        super.init();

        var inventory = addSlotBlock(new OutlineInventoryWidget(menu.slots.subList(0, 36), this, false));
        var buttons = new ScrollVStackPanel(1, 1, true, false);
        buttons.setSize(143, 100);

        HashedBackgroundPanel main = new HashedBackgroundPanel(
                new BlackBackgroundPanel(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setLeft(buttons)
                        .setRight(new SplitPanel(SplitPanel.Split.VERTICAL, 1)
                                .setLeft(configPanel)
                                .setRight(
                                        new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                                .setLeft(inventory)
                                                .setRight(new RemoveButton(Component.translatable("screen.neepmeat.upgrade_manager.remove")))
                                                .setFit(SplitPanel.Fit.FIRST)
                                                .separation()
                                )
                                .setDivide(new Divide.Abs(109)))
                        .setFit(SplitPanel.Fit.FIRST),
                        3),
                3);

        int entryHeight = 18;
        ImplantManager manager = menu.getImplantManager();
        if (manager != null)
        {
            this.implantManager = manager;
            var implants = implantManager.getInstalled();
            if (implants.isEmpty())
            {
                var textField = getNoImplantsMessage(entryHeight, manager);

                buttons.addChild(textField);
            }
            else
            {
                for (var entry : menu.getImplantManager().getInstalled())
                {
                    buttons.addChild(new InstalledWidget(entry, 0, 0, 140, entryHeight));
                }
            }
        }

        main.setDims(leftPos, topPos, imageWidth, imageHeight);
        addRenderableWidget(main);
    }

    private @NotNull MultiLineConfigTextField getNoImplantsMessage(int entryHeight, ImplantManager manager)
    {
        var textField = new MultiLineConfigTextField(0, 0, 120, 3 * entryHeight, c -> {});
        textField.setMaxLength(300);
        textField.setEditable(false);

        // A little bit of jank
        if (manager instanceof ItemImplantManager)
            textField.setText("Use a PLC and the COMBINE instruction to install implants.");
        else
            textField.setText("Use a PLC, Surgery Platform and the IMPLANT instruction to install implants.");
        return textField;
    }

    public void setConfigPanel(@Nullable ImplantConfigPanel panel)
    {
        this.configPanel = panel;
        init();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
        if (menu.shouldVerifyMip())
        {
            mip = menu.getMip();
            if (mip == null)
            {
                Component message1 = Component.nullToEmpty("No valid PLC workbench found.");
                Component message2 = Component.nullToEmpty("Place a PLC workbench in front of the machine.");
                int textWidth = font.width(message1);
                GUIUtil.drawText(context, font, message1, ((imageWidth - textWidth) / 2f), 20, PLCCols.TEXT.col, true);
                textWidth = font.width(message2);
                GUIUtil.drawText(context, font, message2, ((imageWidth - textWidth) / 2f), (20 + font.lineHeight * 1.5f), PLCCols.TEXT.col, true);
            }
            else if (implantManager == null)
            {
                Component message1 = Component.nullToEmpty("No upgradable object found.");
                Component message2 = Component.nullToEmpty("Insert an item or entity into the connected workbench.");
                int textWidth = font.width(message1);
                GUIUtil.drawText(context, font, message1, (imageWidth - textWidth) / 2f, 20, PLCCols.TEXT.col, true);
                textWidth = font.width(message2);
                GUIUtil.drawText(context, font, message2, (imageWidth - textWidth) / 2f, 20 + font.lineHeight * 1.5f, PLCCols.TEXT.col, true);
            }
        }
    }

    @Override
    protected void containerTick()
    {
        if (minecraft.level.getGameTime() % 4 == 0)
        {
            ImplantManager newManager = menu.getImplantManager();

            if (newManager != this.implantManager)
            {
                this.implantManager = newManager;
                rebuildWidgets();
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.setDragging(false);
        if (this.getChildAt(mouseX, mouseY).filter(element -> element.mouseReleased(mouseX, mouseY, button)).isPresent())
            return true;

        // HandledScreen doesn't send mouse release events to children
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (configPanel != null)
        {
            return configPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void select(@Nullable ResourceLocation id)
    {
        this.selected = id;

        menu.selectClient(id);
        var factory = ImplantConfigClient.FACTORIES.get(id);

        if (factory != null)
            configPanel = factory.create(menu, this);
        else
            configPanel = null;

        rebuildWidgets();
    }

    private void remove()
    {
        ImplantManager manager = menu.getImplantManager();
        if (manager != null && selected != null)
        {
            manager.removeImplant(selected);

            menu.removeImplantC2S.emitter().apply(selected);
            select(null);

            rebuildWidgets();
        }
        else
        {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(NMSounds.ERROR.get(), 1));
        }
    }

    class RemoveButton extends AbstractWidget implements Rectangle.Mutable, com.neep.meatlib.client.screen.widget.ClickableWidget
    {
        public RemoveButton(Component message)
        {
            super(0, 0, 1, 1, message);
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) { }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            int col = PLCCols.INVALID.col;
            if (selected != null)
            {
                col = isMouseOver(mouseX, mouseY) ? PLCCols.SELECTED.col : PLCCols.TEXT.col;
            }

            GUIUtil.renderBorderInner(matrices, getX(), getY(), width, height,  col, 0);
            matrices.drawCenteredString(font, this.getMessage(),
                    getX() + width / 2, getY() + (height - 8) / 2, PLCCols.TEXT.col);
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            remove();
        }

        @Override
        public void playDownSound(SoundManager soundManager)
        {
            soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0f));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder)
        {

        }

        @Override
        public void setDims(int x, int y, int w, int h)
        {
            setX(x);
            setY(y);
            this.width = w;
            this.height = h;
        }

        @Override
        public int x0()
        {
            return getX();
        }

        @Override
        public int y0()
        {
            return getY();
        }

        @Override
        public int nw()
        {
            return this.width;
        }

        @Override
        public int nh()
        {
            return this.height;
        }
    }

    class InstalledWidget extends AbstractClickableWidget
    {
        private final ResourceLocation id;
        private final Font textRenderer = Minecraft.getInstance().font;
        private final @Nullable ItemLike item;

        public InstalledWidget(ResourceLocation id, int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.empty());
            this.id = id;
            this.item = ImplantRegistry.getItem(id);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            int textCol = id.equals(selected) ? PLCCols.SELECTED.col : PLCCols.TEXT.col;
            GUIUtil.renderBorderInner(context, x0(), y0(), width, height, textCol, 0);

            Component name = ImplantAttributes.getName(id);
            GUIUtil.drawText(context, textRenderer, name, x0() + 2, y0() + 2, textCol, false);

            if (item != null)
            {
                context.renderItem(item.asItem().getDefaultInstance(), x0() + nw() - 18, y0() + 2);
            }
        }

        @Override
        public NarrationPriority narrationPriority()
        {
            return NarrationPriority.NONE;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            select(id);
        }

        @Override
        public void playDownSound(SoundManager soundManager)
        {
            soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0f));
        }
    }
}
