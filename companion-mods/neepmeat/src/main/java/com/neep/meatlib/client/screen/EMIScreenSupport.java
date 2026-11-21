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

package com.neep.meatlib.client.screen;

import com.neep.meatlib.client.event.BetterHandledScreenRenderEvent;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import net.neoforged.fml.ModList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EMIScreenSupport
{
    public EMIScreenSupport()
    {
        if (!ModList.get().isLoaded("emi"))
            throw new IllegalStateException("EMI support instantiated while EMI is not present");

        BetterHandledScreenRenderEvent.FOREGROUND.add(this::renderFg);
    }

    // Renders the EMI panel in screens that do not call AbstractContainerScreen::render.
    // EMI's own screen mixin covers vanilla screens. This hooks the widget screens.
    public void renderFg(AbstractContainerScreen<?> handledScreen, GuiGraphics raw, int x, int y, int mouseX, int mouseY, float delta)
    {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.push();
        context.matrices().translate(-x, -y, 0.0);
        EmiPort.setPositionTexShader();
        EmiScreenManager.render(context, mouseX, mouseY, delta);
        context.pop();
    }

    public boolean isRecipeScreen()
    {
        return Minecraft.getInstance().screen instanceof RecipeScreen;
    }

    public void openRecipe(ItemStack stack)
    {
        EmiApi.displayRecipes(EmiStack.of(stack));
    }
}
