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

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.config.ConfigTextField;
import com.neep.neepmeat.client.screen.plc.edit.EditBoxWidget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class MultiLineConfigTextField extends EditBoxWidget implements ClickableWidget, Rectangle.Mutable, ConfigTextField
{
    private final Consumer<MultiLineConfigTextField> update;
    private boolean ignoreChange;
    private boolean dirty;

    public MultiLineConfigTextField(int x, int y, int width, int height, Consumer<MultiLineConfigTextField> update)
    {
        this(x, y, width, height, Component.empty(), update);
    }

    public MultiLineConfigTextField(int x, int y, int width, int height, Component placeholder, Consumer<MultiLineConfigTextField> update)
    {
        super(x, y, width, height, 1, placeholder);
        this.update = update;
        editBox.setText("");
        setRenderLineNumber(false);

        setChangeListener(this::onChange);
    }

    protected void onChange(String newText)
    {
        if (!ignoreChange)
        {
            dirty = true;
            update.accept(this);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Consume E keys to stop the inventory from being closed
        if (keyCode == GLFW.GLFW_KEY_E)
            return true;

        // Don't allow newlines
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setPos(int x, int y)
    {
        setX(x);
        setY(y);
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        this.width = w;
        this.height = h;
        editBox.setWidth(w - innerPadding());
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

    public String parse()
    {
        return getText();
    }

    public void setTextSilent(String text)
    {
        ignoreChange = true;
        setText(text);
        ignoreChange = false;
    }

    @Override
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

//    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta)
//    {
//        MatrixStack matrices = context.getMatrices();
//        matrices.push();
//
//        String string = text();
//
//        int i = getCursor();
//        boolean cursorVisible = this.isFocused();
//        boolean inBounds = i <= string.length();
//        float j = 0;
//        float k = 0;
//        float l = y() + 1; // 1 is padding
//
//        int numLines = 0;
//        var lines = textRenderer.
//        for (Iterator<EditBox.LineEntry> it = lines.iterator(); it.hasNext(); l += (float) lineHeight())
//        {
//            matrices.push();
//            EditBox.LineEntry substring = it.next();
//
//            boolean lineVisible = this.isVisible((int) (l), (int) (l + lineHeight()));
//
//            matrices.translate(getX() + getPadding(), l, 0);
//
//            // Render line number
//            if (!editBox.getText().isEmpty() && !substring.wrapped())
//            {
//                matrices.push();
//                matrices.scale(scale, scale, 1);
//                textRenderer.draw(matrices, Text.of(String.valueOf(numLines)).asOrderedText(), 0, 0, PLCCols.LINE_NUMBER.col);
//                numLines++;
//                matrices.pop();
//            }
//
//            // Render normal text line
//            if (lineVisible)
//            {
//                matrices.push();
//                matrices.scale(scale, scale, 1);
//                matrices.translate(lineNumberWidth, 0, 0);
//                String sub = string.substring(substring.beginIndex(), substring.endIndex());
//
//                // Render cursor
//                if (cursorVisible && inBounds && i >= substring.beginIndex() && i <= substring.endIndex())
//                {
//                    float cursorOffset = this.textRenderer.getWidth(string.substring(substring.beginIndex(), i));
//                    context.fill((int) (cursorOffset - 1), 0, (int) cursorOffset, textRenderer.fontHeight(), cursorCol());
//                }
//
//                // Make comments grey in a dirty way
//                String[] strings = sub.split("(?=#)"); // regex eh?
//                j = 0;
//                for (int m = 0; m < strings.length; m++)
//                {
//                    j += this.textRenderer.drawWithShadow(matrices, strings[m], j, 0, m == 0 && !strings[m].startsWith("#") ? textCol() : 0xff666666) - 1;
//                }
//                matrices.pop();
//            }
//
//            k = l;
//            matrices.pop();
//        }
//
//        if (cursorVisible && !inBounds)
//        {
//            if (this.isVisible((int) k, (int) (k + lineHeight())))
//            {
//                // Render end of text cursor
//                matrices.push();
//                matrices.translate(0, k, 0);
//                matrices.scale(scale, scale, 1);
//                matrices.translate((lineNumberWidth), 0, 0);
//                context.fill((int) (j + 5), 0, (int) j + 10, textRenderer.fontHeight(), cursorCol());
//                matrices.pop();
//            }
//        }
//
//        // Selection abomination
//        if (this.editBox.hasSelection())
//        {
//            EditBox.Substring substring2 = this.editBox.getSelection();
//            int m = getX() + this.getPadding();
//            l = getY() + this.getPadding();
//
//            for (EditBox.LineEntry substring3 : this.editBox.getLines())
//            {
//                if (substring2.beginIndex() <= substring3.endIndex())
//                {
//                    if (substring3.beginIndex() > substring2.endIndex())
//                    {
//                        break;
//                    }
//
//                    if (this.isVisible((int) l, (int) (l + lineHeight())))
//                    {
//                        int selWidth = this.textRenderer.getWidth(string.substring(substring3.beginIndex(), Math.max(substring2.beginIndex(), substring3.beginIndex())));
//                        float o;
//                        if (substring2.endIndex() > substring3.endIndex())
//                        {
//                            o = this.width / scale - getPaddingDoubled();
//                        }
//                        else
//                        {
//                            o = this.textRenderer.getWidth(string.substring(substring3.beginIndex(), substring2.endIndex()));
//                        }
//
//                        int selLeft = (int) (m + selWidth + lineNumberWidth);
//                        int selRight = (int) (m + o + lineNumberWidth);
//                        matrices.push();
//                        matrices.translate(0, l, 0);
//                        this.drawSelection(matrices, selLeft, 0, selRight, 9);
//                        matrices.pop();
//                    }
//
//                }
//                l += (float) lineHeight();
//            }
//        }
//        matrices.pop();
//    }
}
