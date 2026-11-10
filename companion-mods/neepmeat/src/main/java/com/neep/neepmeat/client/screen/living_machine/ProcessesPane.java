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

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.Processes;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class ProcessesPane extends LivingMachineScreen.PaneWidget
{
    private final LivingMachineScreen parent;
    private final TooltipHelper tooltipHelper = new TooltipHelper(false, PLCCols.TEXT.col, false);

    public ProcessesPane(LivingMachineScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        addChild(new MetricsPane.ThingyButton(x0() + 2, y0() + nh() - 16 - 2, 112, parent::switchMode));

        int yOffset = y0() + parent.getTextRenderer().lineHeight + 4;
        for (var pair : Processes.getInstance().getEntries())
        {
            var entry = addChild(new EntryWidget(pair));
            entry.setDims(x0() + 2, yOffset, nw(), parent.getTextRenderer().lineHeight + 3);

            yOffset += entry.nh() + 1;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        GUIUtil.drawCenteredText(context, parent.getTextRenderer(), Component.nullToEmpty("Processes and Requirements"), x0() + nw() / 2f, y0() + 3, PLCCols.TEXT.col, false);
    }

    private class EntryWidget extends AbstractClickableWidget implements TooltipRenderer, Rectangle.Mutable
    {
        private final Process process;

//        private List<OrderedText> contents;

        public EntryWidget(Pair<BitSet, Process> pair)
        {
            this.process = pair.value();
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawHorizontalLine1(context, x0() + 1, x0() + nw() - 6, y0(), PLCCols.TRANSPARENT.col);
            GUIUtil.drawText(context, parent.getTextRenderer(), process.getName(), x0() + 1, y0() + 3, PLCCols.TEXT.col, false);
        }

        @Override
        public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            if (isMouseOver(mouseX, mouseY))
            {
                int width = 180;
                List<FormattedCharSequence> texts = new ArrayList<>();
                Component head = NeepMeat.translationKey("screen", "living_machine.requirements");
                texts.add(head.getVisualOrderText());
                List<ComponentType<?>> requiredTypes = process.getRequired();
                for (var type : requiredTypes)
                {
                    ResourceLocation id = ComponentType.REGISTRY.getKey(type);
                    if (id == null)
                        continue;

                    ChatFormatting formatting = parent.getMenu().getBlockEntity().getDisplayComponents().contains(id) ?
                            ChatFormatting.GOLD
                            : ChatFormatting.GRAY;
                    texts.addAll(parent.getTextRenderer().split(
                            Component.literal(" ")
                                    .append(Component.translatable(id.toLanguageKey("component")).withStyle(formatting)), width));
                }

                tooltipHelper.drawOrdered(context, x0() + nw() - 3, y0(), texts, width);
            }
        }

        @Override
        public void setDims(int x, int y, int w, int h)
        {
            setPos(x, y);
            this.width = w;
            this.height = h;
        }
    }
}
