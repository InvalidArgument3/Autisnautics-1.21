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

package com.neep.neepbus.client.screen;

import com.neep.meatlib.client.screen.auto.GeneralPanel;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.NMTextField;
import com.neep.meatlib.client.screen.util.Background;
import com.neep.neepmeat.client.screen.button.OpenGuideButton;
import com.neep.neepmeat.client.screen.util.Border;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepbus.screen.AddressConfigHandler;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class AddressConfigWidget extends GeneralPanel
{
    private final Font textRenderer;
    private final AddressConfigHandler handler;
    private final int headerHeight = 17;

    public AddressConfigWidget(AddressConfigHandler handler)
    {
        this.textRenderer = Minecraft.getInstance().font;
        this.handler = handler;
    }

    @Override
    public void init()
    {
        clearChildren();
        int entryHeight = 30;
        int columnWidth = 120; // Includes padding

        int numEntries = Math.max(handler.inputs.size(), handler.outputs.size());

        var guideButton = new OpenGuideButton("neepbus");

        w = 2 * columnWidth;
        h = headerHeight + numEntries * entryHeight + 2;

        var background = addDrawable(new Background(x, y, w, h, 6));
        var bounds = background.withoutPadding();
        int entryWidth = bounds.nw() / 2;

        for (int i = 0; i < handler.inputs.size(); ++i)
        {
            int finalI = i;
            addChild(new EntryWidget(textRenderer, bounds.x0(), bounds.y0() + headerHeight + 2 + entryHeight * i, entryWidth - 1, entryHeight - 1,
                    false, i, () -> handler.inputs.get(finalI)));
        }

        for (int i = 0; i < handler.outputs.size(); ++i)
        {
            int finalI = i;
            addChild(new EntryWidget(textRenderer, bounds.x0() + bounds.nw() / 2, bounds.y0() + headerHeight + 2 + entryHeight * i, entryWidth - 1, entryHeight - 1,
                    true, i, () -> handler.outputs.get(finalI)));
        }

        addChild(guideButton);
        guideButton.setPos(x + w - guideButton.nw(), y);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

//        GUIUtil.drawScreenBorder(context, x, y, backgroundWidth, backgroundHeight);

        GUIUtil.drawHorizontalLine1(context, x, x + w, y + headerHeight, PLCCols.BORDER.col);
        GUIUtil.drawVerticalLine1(context, x + w / 2, y - 1, y + h, PLCCols.BORDER.col);
        GUIUtil.drawCenteredText(context, textRenderer, Component.nullToEmpty("Input"), x + w / 4f, y + (headerHeight / 2f - textRenderer.lineHeight / 2f), PLCCols.TEXT.col, false);
        GUIUtil.drawCenteredText(context, textRenderer, Component.nullToEmpty("Output"), x + 3 * w / 4f, y + (headerHeight / 2f - textRenderer.lineHeight / 2f), PLCCols.TEXT.col, false);
    }

    protected class EntryWidget extends NMTextField
    {
        private final Border border;
        private final Supplier<AddressConfigHandler.SyncEntry> entry;

        public EntryWidget(Font textRenderer, int x, int y, int width, int height, boolean isOutput, int idx, Supplier<AddressConfigHandler.SyncEntry> entry)
        {
            super(textRenderer, x, y, width, height, Component.empty());
            this.entry = entry;
            this.border = new Border(x, y, width, height, 0, () -> PLCCols.BORDER.col);
            drawFancyBackground(false);

            setText(entry.get().address());

            setChangedListener(newText ->
            {
                handler.addressChangeC2S.emitter().onAddressChange(isOutput, idx, newText.strip());
            });

            tooltip(() -> Collections.singletonList(entry.get().tooltip()));
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.renderWidget(context, mouseX, mouseY, delta);
            GUIUtil.drawText(context, textRenderer, Component.nullToEmpty(entry.get().name() + ": "), x0() + 2, this.y0(), PLCCols.TEXT.col, false);
        }

        @Override
        protected String getPrefix()
        {
            return "→";
        }

        @Override
        protected int getTextY()
        {
//            return super.getTextY();
            return this.y0() + textRenderer.lineHeight;
        }

        @Override
        protected int getTextStart()
        {
            return nw() - textRenderer.width(text()) -
                    (isFocused() ? 10 : 4); // Make room for _ cursor when selected
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers)
        {
            // Consume E keys to stop the inventory from being closed
            if (keyCode == GLFW.GLFW_KEY_E)
                return true;

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void appendClickableNarrations(NarrationElementOutput builder)
        {

        }

        @Override
        public void playDownSound(SoundManager soundManager)
        {
//            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
