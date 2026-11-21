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

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.client.screen.ScreenSubElement;
import com.neep.neepmeat.client.screen.plc.edit.InstructionBrowserWidget;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class PLCScreenShellState extends ScreenSubElement implements Renderable, GuiEventListener, NarratableEntry, PLCScreenState
{
    private final PLCProgramScreen parent;
    private final InstructionBrowserWidget browser;
    private InstructionProvider selectedProvider;
    private @Nullable Component error;

    public PLCScreenShellState(PLCProgramScreen parent)
    {
        this.parent = parent;
        browser = new InstructionBrowserWidget(() -> selectedProvider, this::validProvider, this::selectProvider);
    }

    private boolean validProvider(InstructionProvider provider)
    {
        return provider instanceof SimpleInstructionProvider;
    }

    @Override
    protected void init()
    {
        browser.init(screenWidth, screenHeight);
        addDrawable(new HelpWidget(1, 1));
        addDrawable(new CurrentArgumentWidget(browser.getX(), browser.getY() - textRenderer.lineHeight - 2, parent.getMenu()));

        addDrawableChild(browser);
    }

    private void selectProvider(InstructionProvider provider)
    {
        setError(null);
//        PLCSyncThings.Client.switchOperation(provider, parent.getScreenHandler().getPlc());
        parent.getMenu().changeOperation.emitter().apply(provider.getParseName());
        this.selectedProvider = provider;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        super.render(matrices, mouseX, mouseY, delta);

        if (error != null)
        {
            int ex = 2;
            int ey = screenHeight - textRenderer.lineHeight - 2;
            GUIUtil.drawText(matrices, textRenderer, error, ex, ey, PLCCols.SELECTED.col, true);
        }
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.FOCUSED;
    }

    @Override
    public void argument(Argument argument)
    {
        setError(null);
//        PLCSyncThings.Client.sendArgument(argument, parent.getScreenHandler().getPlc());
        parent.getMenu().applyArgument.emitter().apply(argument);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) { keyPressed(keyCode, scanCode, modifiers); }

    @Override
    public boolean isSelected()
    {
        return false;
    }

    public void setError(@Nullable Component text)
    {
        this.error = text;
    }

    public class HelpWidget implements Renderable
    {
        private final int x;
        private final int y;
        private final int width = 16;
        private final int height = 16;
        private final Component text;

        public HelpWidget(int x, int y)
        {
            this.x = x;
            this.y = y;
            this.text = Component.translatable("text." + NeepMeat.NAMESPACE + ".plc.interactive_help");
        }

        private boolean isMouseOver(double mx, double my)
        {
            return (mx > x && mx < x + width && my > y && my < y + height);
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, PLCProgramScreen.WIDGETS);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int u = 112;
            int v = isMouseOver(mouseX, mouseY) ? 32 : 16;
            matrices.blit(PLCProgramScreen.WIDGETS, this.x, this.y, 0, u, v, width, height, 256, 256);

            int maxWidth = 150;

            List<FormattedCharSequence> lines = textRenderer.split(text, maxWidth);

            if (isMouseOver(mouseX, mouseY))
            {
                TooltipHelper.get().drawOrdered(matrices, mouseX, mouseY, lines, maxWidth);
            }
        }
    }
}
