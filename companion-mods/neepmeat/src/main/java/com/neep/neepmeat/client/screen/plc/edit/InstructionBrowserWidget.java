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

package com.neep.neepmeat.client.screen.plc.edit;

import com.google.common.collect.Lists;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.gui.InstructionAttributes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class InstructionBrowserWidget extends Panel
{
    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;
    private final Supplier<InstructionProvider> selected;
    private final Predicate<InstructionProvider> predicate;
    private final Consumer<InstructionProvider> action;

    @Nullable private GuiEventListener focused;

    private final TooltipHelper tooltipHelper = new TooltipHelper(false, PLCCols.TEXT.col, false);

    private float scrollAmount = 0;

    private int x, y;
    private int width, height;
    private final int pad = 1;

    private final List<DropWidget> entries = Lists.newArrayList();

    public InstructionBrowserWidget(Supplier<InstructionProvider> selected, Predicate<InstructionProvider> predicate, Consumer<InstructionProvider> action)
    {
        this.selected = selected;
        this.predicate = predicate;
        this.action = action;
    }

    @Override
    public void init()
    {

    }

    public void init(int screenWidth, int screenHeight)
    {
        this.width = 100;
        this.height = 200;
        this.x = screenWidth - width;
        this.y = screenHeight - height;

        if (entries.isEmpty())
        {
            addEntries();
        }
    }

    protected void addEntries()
    {
        entries.clear();

        int elementWidth = width - 3;
        var map = Instructions.REGISTRY.values().stream()
                .filter(predicate)
                .collect(Collectors.groupingBy(p -> InstructionAttributes.get(p).category()));

        for (InstructionAttributes.Category category : InstructionAttributes.Category.values())
        {
            var list = map.get(category);
            if (list != null)
                entries.add(new DropWidget(elementWidth, category, list.stream().map(p -> new OperationWidget(elementWidth - 2, p, action)).collect(Collectors.toList())));
        }
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        context.fill(x, y, x + width, y + height, 0x90000000);
        GUIUtil.renderBorder(context, x, y, width - 1, height - 1, PLCCols.BORDER.col, 0);

        context.pose().pushPose();
        int yOffset = (int) (y + 2 + scrollAmount);
        int xOffset = x + 2;

        enableScissor(context);
        for (var entry : entries)
        {
            yOffset += entry.render(context, xOffset, yOffset, mouseX, mouseY, delta) + pad;
        }
        disableScissor(context);
        context.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        if (isInBounds(mouseX, mouseY))
        {
            scrollAmount = (float) Math.min(0.0f, scrollAmount + amount * 6f);

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isInBounds(mouseX, mouseY))
        {
            for (var entry : entries)
            {
                entry.onClick(mouseX, mouseY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return isInBounds(mouseX, mouseY);
    }

    public boolean isInBounds(double mx, double my)
    {
        return (x <= mx && y <= my && x + width >= mx && y + height >= my);
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

    @Nullable
    @Override
    public GuiEventListener getFocused()
    {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused)
    {
        this.focused = focused;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    private void enableScissor(GuiGraphics context)
    {
        context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);
    }

    private void disableScissor(GuiGraphics context)
    {
        context.disableScissor();
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        for (var child : entries)
        {
            child.renderTooltipLayer(context, mouseX, mouseY, delta);
        }
    }

    private class DropWidget implements TooltipRenderer
    {
        private final InstructionAttributes.Category category;
        private final List<OperationWidget> operationWidgets;
        private final int width;
        private final int headerHeight = textRenderer.lineHeight + 4;
        private boolean unfolded = false;

        private int prevX;
        private int prevY;

        public DropWidget(int elementWidth, InstructionAttributes.Category category, List<OperationWidget> operationWidgets)
        {
            this.width = elementWidth;
            this.category = category;
            this.operationWidgets = operationWidgets;
        }

        public float render(GuiGraphics context, int x, int y, double mouseX, double mouseY, float delta)
        {
            prevX = x;
            prevY = y;

            String arrow = unfolded ? "↓" : "→";
            context.drawString(textRenderer, arrow, x + 2, y + 2, PLCCols.TEXT.col, false);
            context.drawString(textRenderer, category.name, x + 2 + 9, y + 2, PLCCols.TEXT.col, false);
//            textRenderer.draw(arrow, x + 2, y + 2, PLCCols.TEXT.col, false, matrices.peek(), );
//            textRenderer.draw(category.name, x + 2 + 9, y + 2, PLCCols.TEXT.col);

            int yOffset = headerHeight;
            if (unfolded)
            {
                for (var widget : operationWidgets)
                {
                    widget.render(context, x, y + yOffset, mouseX, mouseY, delta);
                    yOffset += widget.height + 1;
                }
            }
            return yOffset;
        }

        private boolean isMouseOver(int x, int y, double mouseX, double mouseY)
        {
            return x <= mouseX && y <= mouseY && x + this.width >= mouseX && y + headerHeight >= mouseY;
        }

        public void onClick(double mouseX, double mouseY)
        {
            if (isMouseOver(prevX, prevY, mouseX, mouseY))
            {
                unfolded = !unfolded;
                client.getSoundManager().play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1));
            }

            if (unfolded)
            {
                operationWidgets.forEach(w ->
                {
                    if (w.isMouseOver(mouseX, mouseY))
                        w.onClick(mouseX, mouseY);
                });
            }
        }

        @Override
        public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            if (unfolded)
            {
                for (var child : operationWidgets)
                {
                    child.renderTooltipLayer(context, mouseX, mouseY, delta);
                }
            }
        }
    }

    private class OperationWidget implements TooltipRenderer
    {
        private final InstructionProvider provider;
        private final Consumer<InstructionProvider> action;
        private final int width, height;
        private final Component message;

        private int prevX;
        private int prevY;

        public OperationWidget(int width, InstructionProvider provider, Consumer<InstructionProvider> action)
        {
            this.message = Component.nullToEmpty(provider.getShortName());
            this.width = width; this.height = textRenderer.lineHeight + 1;
            this.provider = provider;
            this.action = action;
        }

        public void render(GuiGraphics context, int x, int y, double mouseX, double mouseY, float delta)
        {
            this.prevX = x;
            this.prevY = y;

            int col = selected.get() == provider ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
            GUIUtil.drawHorizontalLine1(context, x, x + width, y, col);

            Font textRenderer = Minecraft.getInstance().font;
            context.drawString(textRenderer, message, x + 2, y + 2, col, false);
        }

        private boolean isMouseOver(double mouseX, double mouseY)
        {
            return InstructionBrowserWidget.this.isMouseOver(mouseX, mouseY) &&
                    (prevX<= mouseX && prevY <= mouseY && prevX + this.width >= mouseX && prevY + this.height >= mouseY);
        }

        public void renderTooltip(GuiGraphics matrices, int x, int y, int mouseX, int mouseY)
        {
            InstructionAttributes.InstructionTooltip tooltip = InstructionAttributes.get(provider);
            int width = 230;
            int tx = x - width - 5;
            if (tooltip != InstructionAttributes.InstructionTooltip.EMPTY)
            {
                List<FormattedCharSequence> wrapped = textRenderer.split(tooltip.description(), width);
                tooltipHelper.drawOrdered(matrices, tx, y, wrapped, width);
            }
        }

        @Override
        public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            if (isMouseOver(mouseX, mouseY))
            {
                renderTooltip(context, x, y, (int) mouseX, (int) mouseY);
            }
        }

        public void onClick(double mouseX, double mouseY)
        {
            action.accept(provider);
            playDownSound(client.getSoundManager());
        }

        public void playDownSound(SoundManager soundManager)
        {
            soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0f));
        }

        public int height()
        {
            return height;
        }
    }
}
