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

import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.plc.MonoTextRenderer;
import com.neep.meatlib.client.screen.GUIUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class EditBoxWidget extends AbstractScrollWidget
{
//    private final TextRenderer textRenderer;
    protected final MonoTextRenderer textRenderer;
    protected final EditBox editBox;

    @Nullable protected String errorMessage;
    private int errorCol = 0xFFFFFFFF;

    protected final float scale;
    private int errorLine = -1;
    private int debugLine = -1;
    private final Component placeholder;

    protected boolean renderLineNumber = true;

    private float lineNumberWidth;

    public EditBoxWidget(int x, int y, int width, int height, float scale, Component placeholder)
    {
        super(x, y, width, height, Component.nullToEmpty("gle"));
        this.scale = scale;
        this.placeholder = placeholder;
        this.textRenderer = new MonoTextRenderer();
        updateLineNumber();
        this.editBox = new EditBox(this.textRenderer, (int) (width - this.totalInnerPadding() - lineNumberWidth), scale);
        this.editBox.setCursorChangeListener(this::onCursorChange);
    }

    public void setRenderLineNumber(boolean renderLineNumber)
    {
        this.renderLineNumber = renderLineNumber;
        updateLineNumber();
    }

    private void updateLineNumber()
    {
        if (renderLineNumber)
            this.lineNumberWidth = textRenderer.getWidth("11") + 4;
        else
            this.lineNumberWidth = 0;
    }

    public void setMaxLength(int maxLength)
    {
        this.editBox.setMaxLength(maxLength);
    }

    public void setChangeListener(Consumer<String> changeListener)
    {
        this.editBox.setChangeListener(changeListener);
    }

    public String getText()
    {
        return this.editBox.getText();
    }

    public void insert(String s)
    {
        EditBox.Substring selection = editBox.getSelection();
        if (!editBox.getText().isEmpty() && !Character.isWhitespace(editBox.getText().charAt(Math.max(0, selection.beginIndex() - 1))))
        {
            s = " " + s;
        }
        editBox.replaceSelection(s);
    }

    public void setText(String text)
    {
        this.editBox.setText(text);
    }

    public void tick()
    {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Scroll bar
        boolean bl2 = this.scrollbarVisible() && mouseX >= (this.getX() + this.width) && mouseX <= (this.getX() + this.width + 8) && mouseY >= this.getY() && mouseY < (this.getY() + this.height);

        // The super implementation of this method returns true both when scrolling and clicking the main widget,
        // so there is no way of telling whether to move the cursor or not.
        if (withinContentAreaPoint(mouseX, mouseY) && bl2)
            return super.mouseClicked(mouseX, mouseY, button);

        if (this.withinContentAreaPoint(mouseX, mouseY) && button == 0)
        {
            this.editBox.setSelecting(Screen.hasShiftDown());
            this.moveCursor(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
        {
            return true;
        }
        else if (this.withinContentAreaPoint(mouseX, mouseY) && button == 0)
        {
            this.editBox.setSelecting(true);
            this.moveCursor(mouseX, mouseY);
            this.editBox.setSelecting(Screen.hasShiftDown());
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder)
    {

    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return this.editBox.handleSpecialKey(keyCode);
    }

    public boolean charTyped(char chr, int modifiers)
    {
        if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(chr))
        {
            this.editBox.replaceSelection(Character.toString(chr));
            return true;
        }
        else
        {
            return false;
        }
    }

    protected int textCol()
    {
        return PLCCols.TEXT.col;
    }

    private int selCol()
    {
        return PLCCols.SELECTED.col;
    }

    private int borderCol()
    {
        return PLCCols.BORDER.col;
    }

    protected int cursorCol()
    {
        return PLCCols.SELECTED.col;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        PoseStack matrices = context.pose();
        int x = getX();
        int y = getY();
        if (this.visible)
        {
            int col = !this.isFocused() ? PLCCols.INVALID.col : PLCCols.SELECTED.col;
            context.fill(x + 1, y + 1, x + this.width - 1, y + this.height - 1, 0x90000000);
            GUIUtil.renderBorder(context, x, y, width - 1, height - 1, col, 0);

            context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);

            // Render error message
            if (errorMessage != null)
            {
                List<FormattedCharSequence> errorLines = textRenderer.wrapLines(FormattedText.of(errorMessage), 360);

                matrices.pushPose();
                float bottom = (float) (y + height - lineHeight() * errorLines.size() - innerPadding());
                matrices.translate(0, bottom, 0);
                matrices.scale(scale, scale, 1);
                for (var line : errorLines)
                {
                    textRenderer.draw(matrices, line, x + innerPadding(), 0, errorCol);
                    matrices.translate(0, textRenderer.fontHeight(), 0);
                }
                matrices.popPose();
            }

            matrices.pushPose();
            matrices.translate(0.0, -this.scrollAmount(), 0.0);
            renderHighlightLine(context, errorLine, PLCCols.ERROR_LINE.col);
            renderHighlightLine(context, debugLine, PLCCols.DEBUG_LINE.col);
            renderContents(context, mouseX, mouseY, delta);
            matrices.popPose();

            context.disableScissor();
            renderOverlay(matrices);
        }
    }

    protected void renderHighlightLine(GuiGraphics matrices, int line, int col)
    {
        if (line >= 0)
        {
            double lineStart = getY() + innerPadding() + editBox.absLineToWrapped(line) * lineHeight();
            matrices.fill(getX() + innerPadding(), (int) lineStart, getX() + width - innerPadding(), (int) (lineStart + lineHeight()), col);
        }
    }

    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        PoseStack matrices = context.pose();
        matrices.pushPose();

        String string = this.editBox.getText();

        // Render placeholder
        if (string.isEmpty() && !this.isFocused())
        {
            int i = 0;
            for (var line : textRenderer.wrapLines(placeholder, this.width))
            {
                matrices.pushPose();
                matrices.translate(0, i * lineHeight(), 0);
                matrices.scale(scale, scale, 1);
                textRenderer.draw(matrices, line, getX() + innerPadding(), getY() + innerPadding(), textCol());
                matrices.popPose();
                ++i;
            }
        }


        int i = this.editBox.getCursor();
        boolean cursorVisible = this.isFocused() && editBox.isEditable();
        boolean inBounds = i <= string.length();
        float j = 0;
        float k = 0;
        float l = getY() + this.innerPadding();

        int numLines = 0;
        for (Iterator<EditBox.LineEntry> it = this.editBox.getLines().iterator(); it.hasNext(); l += (float) lineHeight())
        {
            matrices.pushPose();
            EditBox.LineEntry substring = it.next();

            boolean lineVisible = this.withinContentAreaTopBottom((int) (l), (int) (l + lineHeight()));

            matrices.translate(getX() + innerPadding(), l, 0);

            // Render line number
            if (renderLineNumber)
            {
                if (!editBox.getText().isEmpty() && !substring.wrapped())
                {
                    matrices.pushPose();
                    matrices.scale(scale, scale, 1);
                    textRenderer.draw(matrices, Component.nullToEmpty(String.valueOf(numLines)).getVisualOrderText(), 0, 0, PLCCols.LINE_NUMBER.col);
                    numLines++;
                    matrices.popPose();
                }
            }

            // Render normal text line
            if (lineVisible)
            {
                matrices.pushPose();
                matrices.scale(scale, scale, 1);
                matrices.translate(lineNumberWidth, 0, 0);
                String sub = string.substring(substring.beginIndex(), substring.endIndex());

                // Render cursor
                if (cursorVisible && inBounds && i >= substring.beginIndex() && i <= substring.endIndex())
                {
                    float cursorOffset = this.textRenderer.getWidth(string.substring(substring.beginIndex(), i));
                    context.fill((int) (cursorOffset - 1), 0, (int) cursorOffset, textRenderer.fontHeight(), cursorCol());
                }

                // Make comments grey in a dirty way
                String[] strings = sub.split("(?=#)"); // regex eh?
                j = 0;
                for (int m = 0; m < strings.length; m++)
                {
                    j += this.textRenderer.drawWithShadow(matrices, strings[m], j, 0, m == 0 && !strings[m].startsWith("#") ? textCol() : 0xff666666) - 1;
                }
                matrices.popPose();
            }

            k = l;
            matrices.popPose();
        }

        if (cursorVisible && !inBounds)
        {
            if (this.withinContentAreaTopBottom((int) k, (int) (k + lineHeight())))
            {
                // Render end of text cursor
                matrices.pushPose();
                matrices.translate(0, k, 0);
                matrices.scale(scale, scale, 1);
                matrices.translate((lineNumberWidth), 0, 0);
                context.fill((int) (j + 5), 0, (int) j + 10, textRenderer.fontHeight(), cursorCol());
                matrices.popPose();
            }
        }

        // Selection abomination
        if (this.editBox.hasSelection())
        {
            EditBox.Substring substring2 = this.editBox.getSelection();
            int m = getX() + this.innerPadding();
            l = getY() + this.innerPadding();

            for (EditBox.LineEntry substring3 : this.editBox.getLines())
            {
                if (substring2.beginIndex() <= substring3.endIndex())
                {
                    if (substring3.beginIndex() > substring2.endIndex())
                    {
                        break;
                    }

                    if (this.withinContentAreaTopBottom((int) l, (int) (l + lineHeight())))
                    {
                        int selWidth = this.textRenderer.getWidth(string.substring(substring3.beginIndex(), Math.max(substring2.beginIndex(), substring3.beginIndex())));
                        float o;
                        if (substring2.endIndex() > substring3.endIndex())
                        {
                            o = this.width / scale - totalInnerPadding();
                        }
                        else
                        {
                            o = this.textRenderer.getWidth(string.substring(substring3.beginIndex(), substring2.endIndex()));
                        }

                        int selLeft = (int) (m + selWidth + lineNumberWidth);
                        int selRight = (int) (m + o + lineNumberWidth);
                        matrices.pushPose();
                        matrices.translate(0, l, 0);
                        this.drawSelection(matrices, selLeft, 0, selRight, 9);
                        matrices.popPose();
                    }

                }
                l += (float) lineHeight();
            }
        }
        matrices.popPose();
    }

    protected void renderOverlay(PoseStack matrices)
    {
//        super.renderOverlay(matrices);
//        if (this.editBox.hasMaxLength())
//        {
//            int i = this.editBox.getMaxLength();
//            Text text = Text.translatable("gui.multiLineEditBox.character_limit", this.editBox.getText().length(), i);
//            drawTextWithShadow(matrices, this.textRenderer, text, this.x + this.width - this.textRenderer.getWidth(text), this.y + this.height + 4, 10526880);
//        }
    }

    public int getInnerHeight()
    {
        return (int) (lineHeight() * this.editBox.getLineCount());
    }

    protected boolean scrollbarVisible()
    {
        return (double) this.editBox.getLineCount() > this.getMaxLinesWithoutOverflow();
    }

    protected double scrollRate()
    {
        return lineHeight() / 2.0;
    }

    private void drawSelection(PoseStack matrices, int left, int top, int right, int bottom)
    {
        matrices.pushPose();
        matrices.scale(scale, scale, 1);

        Matrix4f matrix4f = matrices.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
//        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(LogicOp.OR_REVERSE);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(matrix4f, (float) left, (float) bottom, 0.0F);
        bufferBuilder.addVertex(matrix4f, (float) right, (float) bottom, 0.0F);
        bufferBuilder.addVertex(matrix4f, (float) right, (float) top, 0.0F);
        bufferBuilder.addVertex(matrix4f, (float) left, (float) top, 0.0F);
        tessellator.end();
//        int col = selCol();
//        RenderSystem.setShaderColor(((col >> 24) & 0xFF) / 255f, ((col >> 16) & 0xFF) / 255f, ((col >> 8) & 0xFF) / 255f, (col & 0xFF) / 255f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
//        RenderSystem.enableTexture();

        matrices.popPose();
    }

    private void onCursorChange()
    {
        double d = this.scrollAmount();
        EditBox var10000 = this.editBox;
        EditBox.Substring substring = var10000.getLine((int) (d / lineHeight())).substring();
        int var5;
        if (this.editBox.getCursor() <= substring.beginIndex())
        {
            var5 = this.editBox.getCurrentLineIndex();
            d = var5 * lineHeight();
        }
        else
        {
            double var10001 = d + (double) this.height;
            EditBox.Substring substring2 = var10000.getLine((int) (var10001 / lineHeight()) - 1).substring();
            if (this.editBox.getCursor() > substring2.endIndex())
            {
                var5 = this.editBox.getCurrentLineIndex();
                var5 = (int) (var5 * lineHeight() - this.height);
                d = var5 + lineHeight() + this.totalInnerPadding();
            }
        }

        this.setScrollAmount(d);
    }

    private double getMaxLinesWithoutOverflow()
    {
        double height1 = this.height - this.totalInnerPadding();
        return height1 / lineHeight();
    }

    private void moveCursor(double mouseX, double mouseY)
    {
        double d = mouseX - (double) getX() - ((double) this.innerPadding() + lineNumberWidth * scale);
        double e = mouseY - (double) getY() - (double) this.innerPadding() + this.scrollAmount();
        this.editBox.moveCursor(d / scale, e / scale);
    }

    private double lineHeight()
    {
        return (textRenderer.fontHeight()) * scale;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setError(String message, int col)
    {
        this.errorMessage = message;
        this.errorCol = col;
    }

    public void setErrorLine(int line)
    {
        this.errorLine = line;
    }

    public void setDebugLine(int line)
    {
        this.debugLine = line;
    }

    public void setEditable(boolean editable)
    {
        editBox.setEditable(editable);
    }
}
