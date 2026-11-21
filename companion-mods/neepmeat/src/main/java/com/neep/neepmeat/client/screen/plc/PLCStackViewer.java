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

package com.neep.neepmeat.client.screen.plc;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class PLCStackViewer implements Renderable, Rectangle
{
    private int x;
    private int y;
    private int width;
    private int height;
    private final PLCBlockEntity plc;

    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;

    private final TooltipHelper tooltipRenderer;

    private int adjustedX;

    public PLCStackViewer(PLCBlockEntity plc)
    {
        this.plc = plc;
        this.tooltipRenderer = new TooltipHelper(false, PLCCols.TEXT.col, true);
        adjustedX = this.x0();
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        int stride = textRenderer.lineHeight + 1;

        var stack = plc.getVariableStack();

        matrices.enableScissor(adjustedX, y, x1(), y1());
        int maxWidth = 70;
        try
        {
            int lastHeight = ((stack.size() + 2) * stride + 2);

            Component name = Component.literal("Stack");
            int nameWidth = textRenderer.width(name);

            // Fit width to the widest entry
            int adjustedWidth = nameWidth + 2;
            for (int i = 0; i < stack.size(); ++i)
            {
                adjustedWidth = Mth.clamp(adjustedWidth, 2 + textRenderer.width(entryText(plc, stack.peekUnsafe(i))), maxWidth);
            }

            adjustedX = x - adjustedWidth + width;
            matrices.fill(adjustedX, y + height - lastHeight, x + width, y + height, 0x90000000);
            GUIUtil.renderBorder(matrices, adjustedX, y + height - lastHeight, adjustedWidth, lastHeight - 1, PLCCols.BORDER.col, 0);

            int textY = this.y + height - 1 - textRenderer.lineHeight;

            GUIUtil.drawText(matrices, textRenderer, "Stack", adjustedX + 1 + (adjustedWidth - nameWidth) / 2f, textY, PLCCols.TEXT.col, false);
            textY -= stride;
            for (int i = stack.size() - 1; i >= 0; --i)
            {
                var entry = stack.peek(i);

                GUIUtil.drawText(matrices, textRenderer, entryText(plc, entry), adjustedX + 2, textY, PLCCols.TEXT.col, true);
                textY -= stride;
            }
        }
        catch (NeepASM.RuntimeException ignored) { }
        matrices.disableScissor();

        try
        {
            if (mouseX > adjustedX && mouseX <= x1() && mouseY > this.y0() && mouseY <= y1())
            {
                int hovered = (nh() - 2 - (mouseY - this.y0())) / stride - 1;
                if (hovered >= 0 && hovered < stack.size())
                {
                    var entry = stack.peek(stack.size() - 1 - hovered);
                    tooltipRenderer.draw(matrices, adjustedX,
                            (- (((hovered + 2) * stride) - nh())) + this.y0() - 2,
                            detailedEntryText(plc, entry));
                }
            }
        }
        catch (NeepASM.RuntimeException ignored) {}
    }

    private List<Component> detailedEntryText(PLC plc, ThordData entry)
    {
        List<Component> result = new ObjectArrayList<>();

        if (entry instanceof ThordData.PointerImpl pointer)
        {
            String string;
            String memoryType;

            // Display 0 pointers as NULL
            if (pointer.value() == MemoryManager.NULL)
            {
                string = "NULL";
                memoryType = "???";
            }
            else
            {
                var memoryEntry = plc.getMemory().fetch(pointer.value());
                if (memoryEntry == null)
                {
                    string = Integer.toString(pointer.value(), 10) + "*";
                    memoryType = "???";
                }
                else
                {
                    string = memoryEntry.toString();
                    memoryType = memoryEntry.type().name();
                }
            }

            result.add(Component.nullToEmpty(string));
            result.add(Component.nullToEmpty("Type: " + "POINTER"));
            result.add(Component.nullToEmpty("Addr: " + pointer.value()));
            result.add(Component.nullToEmpty("Type: " + memoryType));
        }
        else
        {
            String string = entry.stringValue();
            result.add(Component.nullToEmpty(string));
//            result.add(Text.of("Type: " + "INT"));
        }

        return result;
    }

    private String entryText(PLC plc, ThordData data)
    {
        String string;
        if (data instanceof ThordData.PointerImpl pointer)
        {
            // Display 0 pointers as NULL
            if (pointer.value() == MemoryManager.NULL)
            {
                string = "NULL";
            }
            else
            {
                var memoryEntry = plc.getMemory().fetch(pointer.value());
                if (memoryEntry == null)
                    string = Integer.toString(pointer.value(), 10) + "*";
                else
                    string = Integer.toString(pointer.value(), 10) + "->" + memoryEntry;
            }
        }
        else
        {
            string = data.stringValue();
        }

        return string;
    }

    public void init(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        return width;
    }

    @Override
    public int nh()
    {
        return height;
    }
}
