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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.neep.neepmeat.client.screen.plc.MonoTextRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class EditBox
{
    private final List<LineEntry> lines = Lists.newArrayList();
    private int width;
    private final MonoTextRenderer textRenderer;
    private String text = "";
    private int cursor;
    private int selectionEnd;
    private boolean selecting;
    private int maxLength = Integer.MAX_VALUE;

    private Consumer<String> changeListener = (text) ->
    {
    };
    private Runnable cursorChangeListener = () ->
    {
    };

    private final float scale;

    private boolean editable = true;

    public EditBox(MonoTextRenderer textRenderer, int width, float scale)
    {
        this.textRenderer = textRenderer;
        this.width = width;
        this.scale = scale;
    }

    public int getMaxLength()
    {
        return this.maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        if (maxLength < 0)
            throw new IllegalArgumentException("Character limit cannot be negative");
        else
            this.maxLength = maxLength;
    }

    public boolean hasMaxLength()
    {
        return this.maxLength != Integer.MAX_VALUE;
    }

    public void setChangeListener(Consumer<String> changeListener)
    {
        this.changeListener = changeListener;
    }

    public void setCursorChangeListener(Runnable cursorChangeListener)
    {
        this.cursorChangeListener = cursorChangeListener;
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String text)
    {
        this.text = this.truncateForReplacement(text);
//        this.cursor = this.text.length();
        this.cursor = Mth.clamp(this.cursor, 0, this.text.length());
        this.selectionEnd = this.cursor;
        this.onChange();
    }

    public void replaceSelection(String string)
    {
        if (!editable)
            return;

        if (!string.isEmpty() || this.hasSelection())
        {
            String string2 = this.truncate(SharedConstants.filterText(string, true));

            Substring substring = this.getSelection();

            // Preserve current indent level
            if (string2.equals("\n") && substring.beginIndex == substring.endIndex)
            {
                int spaces = 0;

                int index = Math.min(text.length() - 1, substring.beginIndex);

                if (index >= 0)
                {
                    char c = text.charAt(index);
                    // If the next character is a newline we need to reverse in order to find the start of the current line or something I don't really know this is really janky but I can't be bothered to make this readable or sensible I just want it to work now please
                    if (c == '\n' && index > 0)
                        index--;

                    int i = index;
                    do
                    {
                        c = this.text.charAt(i);
                        if (c == '\n')
                        {
                            // Add one to get past the current \n
                            spaces = countSpaces(text, Math.min(text.length() - 1, i + 1));
                            string2 = string2 + " ".repeat(spaces);
                            break;
                        }

                        i--;
                    }
                    while (i > 0);
                }
            }

            this.text = (new StringBuilder(this.text)).replace(substring.beginIndex, substring.endIndex, string2).toString();
            this.cursor = substring.beginIndex + string2.length();
            this.selectionEnd = this.cursor;
            this.onChange();
        }
    }

    private int countSpaces(String string, int startIndex)
    {
        int spaces = 0;

        char c;
        int i = startIndex;
        do
        {
            c = this.text.charAt(i);

            if (c == ' ')
                spaces++;

            if (c != ' ')
                break;

            i++;
        }
        while (i < string.length());

        return spaces;
    }

    public void delete(int offset)
    {
        if (!editable)
            return;

        if (!this.hasSelection())
        {
            this.selectionEnd = Mth.clamp(this.cursor + offset, 0, this.text.length());
        }

        this.replaceSelection("");
    }

    public int getCursor()
    {
        return this.cursor;
    }

    public void setSelecting(boolean selecting)
    {
        this.selecting = selecting;
    }

    public Substring getSelection()
    {
        return new Substring(Math.min(this.selectionEnd, this.cursor), Math.max(this.selectionEnd, this.cursor));
    }

    public int getLineCount()
    {
        return this.lines.size();
    }

    public int absLineToWrapped(int line)
    {
        for (int i = 0; i < lines.size(); ++i)
        {
            if (lines.get(i).lineNum() == line)
                return i;
        }

        return lines.size();
    }

    public int getCurrentLineIndex()
    {
        for (int i = 0; i < this.lines.size(); ++i)
        {
            Substring substring = this.lines.get(i).substring();
            if (this.cursor >= substring.beginIndex && this.cursor <= substring.endIndex)
            {
                return i;
            }
        }

        return -1;
    }

    public LineEntry getLine(int index)
    {
        return this.lines.get(Mth.clamp(index, 0, this.lines.size() - 1));
    }

    public void moveCursor(Whence movement, int amount)
    {
        if (!editable)
            return;

        switch (movement)
        {
            case ABSOLUTE:
                this.cursor = amount;
                break;
            case RELATIVE:
                this.cursor += amount;
                break;
            case END:
                this.cursor = this.text.length() + amount;
        }

        this.cursor = Mth.clamp(this.cursor, 0, this.text.length());
        this.cursorChangeListener.run();
        if (!this.selecting)
        {
            this.selectionEnd = this.cursor;
        }

    }

    public void moveCursorLine(int offset)
    {
        if (offset != 0)
        {
            int i = this.textRenderer.getWidth(this.text.substring(this.getCurrentLine().beginIndex, this.cursor)) + 2;
            Substring substring = this.getOffsetLine(offset);
            int j = this.textRenderer.trimToWidth(this.text.substring(substring.beginIndex, substring.endIndex), i).length();
            this.moveCursor(Whence.ABSOLUTE, substring.beginIndex + j);
        }
    }

    public void moveCursor(double x, double y)
    {
        int i = Mth.floor(x);
        int j = Mth.floor(y / 9.0);
        LineEntry substring = this.lines.get(Mth.clamp(j, 0, Math.max(0, this.lines.size() - 1)));
        int k = this.textRenderer.trimToWidth(this.text.substring(substring.beginIndex(), substring.endIndex()), i).length();
        this.moveCursor(Whence.ABSOLUTE, substring.beginIndex() + k);
    }

    public boolean handleSpecialKey(int keyCode)
    {
        if (!editable)
            return false;

        this.selecting = Screen.hasShiftDown();
        if (Screen.isSelectAll(keyCode))
        {
            this.cursor = this.text.length();
            this.selectionEnd = 0;
            return true;
        }
        else if (Screen.isCopy(keyCode))
        {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            return true;
        }
        else if (Screen.isPaste(keyCode))
        {
            this.replaceSelection(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        else if (Screen.isCut(keyCode))
        {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            this.replaceSelection("");
            return true;
        }
        else
        {
            Substring substring;
            switch (keyCode)
            {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
                {
                    this.replaceSelection("\n");
                    return true;
                }
                case GLFW.GLFW_KEY_BACKSPACE ->
                {
                    if (Screen.hasControlDown())
                    {
                        substring = this.getPreviousWordAtCursor();
                        this.delete(substring.beginIndex - this.cursor);
                    }
                    else
                    {
                        this.delete(-1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE ->
                {
                    if (Screen.hasControlDown())
                    {
                        substring = this.getNextWordAtCursor();
                        this.delete(substring.beginIndex - this.cursor);
                    }
                    else
                    {
                        this.delete(1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_RIGHT ->
                {
                    if (Screen.hasControlDown())
                    {
                        substring = this.getNextWordAtCursor();
                        this.moveCursor(Whence.ABSOLUTE, substring.beginIndex);
                    }
                    else
                    {
                        this.moveCursor(Whence.RELATIVE, 1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT ->
                {
                    if (Screen.hasControlDown())
                    {
                        substring = this.getPreviousWordAtCursor();
                        this.moveCursor(Whence.ABSOLUTE, substring.beginIndex);
                    }
                    else
                    {
                        this.moveCursor(Whence.RELATIVE, -1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_DOWN ->
                {
                    if (!Screen.hasControlDown())
                    {
                        this.moveCursorLine(1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_UP ->
                {
                    if (!Screen.hasControlDown())
                    {
                        this.moveCursorLine(-1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_PAGE_UP ->
                {
                    this.moveCursor(Whence.ABSOLUTE, 0);
                    return true;
                }
                case GLFW.GLFW_KEY_PAGE_DOWN ->
                {
                    this.moveCursor(Whence.END, 0);
                    return true;
                }
                case GLFW.GLFW_KEY_HOME ->
                {
                    if (Screen.hasControlDown())
                    {
                        this.moveCursor(Whence.ABSOLUTE, 0);
                    }
                    else
                    {
                        this.moveCursor(Whence.ABSOLUTE, this.getCurrentLine().beginIndex);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_END ->
                {
                    if (Screen.hasControlDown())
                    {
                        this.moveCursor(Whence.END, 0);
                    }
                    else
                    {
                        this.moveCursor(Whence.ABSOLUTE, this.getCurrentLine().endIndex);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_TAB ->
                {
                    replaceSelection("  ");
                    return true;
                }
                default ->
                {
                    return false;
                }
            }
        }
    }

    public Iterable<LineEntry> getLines()
    {
        return this.lines;
    }

    public boolean hasSelection()
    {
        return this.selectionEnd != this.cursor;
    }

    @VisibleForTesting
    public String getSelectedText()
    {
        Substring substring = this.getSelection();
        return this.text.substring(substring.beginIndex, substring.endIndex);
    }

    private Substring getCurrentLine()
    {
        return this.getOffsetLine(0);
    }

    private Substring getOffsetLine(int offsetFromCurrent)
    {
        int i = this.getCurrentLineIndex();
        if (i < 0)
        {
            int var10002 = this.cursor;
            throw new IllegalStateException("Cursor is not within text (cursor = " + var10002 + ", length = " + this.text.length() + ")");
        }
        else
        {
            return this.lines.get(Mth.clamp(i + offsetFromCurrent, 0, this.lines.size() - 1)).substring();
        }
    }

    @VisibleForTesting
    public Substring getPreviousWordAtCursor()
    {
        if (this.text.isEmpty())
        {
            return EditBox.Substring.EMPTY;
        }
        else
        {
            int i;
            for (i = Mth.clamp(this.cursor, 0, this.text.length() - 1); i > 0 && Character.isWhitespace(this.text.charAt(i - 1)); --i)
            {
            }

            while (i > 0 && !Character.isWhitespace(this.text.charAt(i - 1)))
            {
                --i;
            }

            return new Substring(i, this.getWordEndIndex(i));
        }
    }

    @VisibleForTesting
    public Substring getNextWordAtCursor()
    {
        if (this.text.isEmpty())
        {
            return EditBox.Substring.EMPTY;
        }
        else
        {
            int i;
            for (i = Mth.clamp(this.cursor, 0, this.text.length() - 1); i < this.text.length() && !Character.isWhitespace(this.text.charAt(i)); ++i)
            {
            }

            while (i < this.text.length() && Character.isWhitespace(this.text.charAt(i)))
            {
                ++i;
            }

            return new Substring(i, this.getWordEndIndex(i));
        }
    }

    private int getWordEndIndex(int startIndex)
    {
        int i;
        for (i = startIndex; i < this.text.length() && !Character.isWhitespace(this.text.charAt(i)); ++i);

        return i;
    }

    private void onChange()
    {
        this.rewrap();
        this.changeListener.accept(this.text);
        this.cursorChangeListener.run();
    }

    private void rewrap()
    {
        lines.clear();
        if (text.isEmpty())
        {
            lines.add(LineEntry.EMPTY);
        }
        else
        {
//            AtomicInteger numLines = new AtomicInteger();
//            String[] splitLines = text.split("\n");
//            AtomicInteger lineEnd = new AtomicInteger(0);
//            for (String line : splitLines)
//            {
//                line += '\n';
//                AtomicBoolean lineStart = new AtomicBoolean(true);
//                textRenderer.getTextHandler().wrapLines(line, Math.round(width / scale), Style.EMPTY, false, (style, start, end) ->
//                {
//                    lines.add(new LineEntry(lineEnd.get() + start, lineEnd.get() + end, !lineStart.get()));
//                    lineEnd.addAndGet(end);
//                    lineStart.set(false);
//                });
//            }

            AtomicInteger numLines = new AtomicInteger(0);
            textRenderer.getTextHandler().splitLines(text, Math.round(width / scale), Style.EMPTY, false, (style, start, end) ->
            {
                boolean wrap = start - 1 >= 0 && text.charAt(start - 1) != '\n';

                if (!wrap && start - 1 >= 0)
                    numLines.incrementAndGet();

                lines.add(new LineEntry(start, end, numLines.get(), wrap));

            });
            if (text.charAt(this.text.length() - 1) == '\n')
            {
                lines.add(new LineEntry(text.length(), text.length(), numLines.get(), false));
            }

        }
    }

    private String truncateForReplacement(String value)
    {
        return this.hasMaxLength() ? StringUtil.truncateStringIfNecessary(value, this.maxLength, false) : value;
    }

    private String truncate(String value)
    {
        if (this.hasMaxLength())
        {
            int i = this.maxLength - this.text.length();
            return StringUtil.truncateStringIfNecessary(value, i, false);
        }
        else
        {
            return value;
        }
    }

    public void setWidth(int w)
    {
        this.width = w;
        rewrap();
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public boolean isEditable()
    {
        return editable;
    }

    @OnlyIn(Dist.CLIENT)
    public record Substring(int beginIndex, int endIndex)
    {
        static final Substring EMPTY = new Substring(0, 0);

        public int beginIndex()
        {
            return this.beginIndex;
        }

        public int endIndex()
        {
            return this.endIndex;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record LineEntry(Substring substring, int lineNum, boolean wrapped)
    {
        static final LineEntry EMPTY = new LineEntry(Substring.EMPTY, 0, false);

        public LineEntry(int i, int j, int lineNum, boolean wrapped)
        {
            this(new Substring(i, j), lineNum, wrapped);
        }

        public int endIndex()
        {
            return substring.endIndex;
        }

        public int beginIndex()
        {
            return substring.beginIndex;
        }
    }
}
