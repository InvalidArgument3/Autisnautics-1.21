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

import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.mojang.blaze3d.platform.Window;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepbus.screen.SliderScreenHandler;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class SliderScreen extends AbstractContainerScreen<SliderScreenHandler>
{
    private final RangeConfigWidget rangeConfig;

    public SliderScreen(SliderScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        rangeConfig = new SliderConfigWidget(6, 0, 100, 100, handler.sliderConfig);

        ScreenReady.ready();
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {

    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    protected void init()
    {
        super.init();

        int wHeight = minecraft.getWindow().getGuiScaledHeight();

        addRenderableWidget(rangeConfig);

        // Attach the config box to the lower left
        rangeConfig.init();
        rangeConfig.setPos(rangeConfig.x0(), wHeight - rangeConfig.nh() - 6);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        int yOff = 5;
        for (var text : font.split(NeepMeat.translationKey("screen", "slider.instructions"), 400))
        {
            GUIUtil.drawText(context, font, text, 5, yOff, PLCCols.TEXT.col, true);
            yOff += font.lineHeight;
        }
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();

        rangeConfig.tick();

        // Query keys every tick to allow continuous slider movement.
        Window window = minecraft.getWindow();
        if (GLFW.glfwGetKey(window.getWindow(), KeyBindingHelper.getBoundKeyOf(minecraft.options.keyUp).getValue())
                == GLFW.GLFW_PRESS)
        {
            menu.incrementC2S.emitter().scroll(1, !Screen.hasShiftDown());
        }
        else if (GLFW.glfwGetKey(window.getWindow(), KeyBindingHelper.getBoundKeyOf(minecraft.options.keyDown).getValue())
                == GLFW.GLFW_PRESS)
        {
            menu.incrementC2S.emitter().scroll(-1, !Screen.hasShiftDown());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
//        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E)
//            close();

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        super.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        menu.incrementC2S.emitter().scroll(amount, !Screen.hasShiftDown());
        return true;
    }

    @Override
    public void onClose()
    {
        rangeConfig.update();
        super.onClose();
    }
}
