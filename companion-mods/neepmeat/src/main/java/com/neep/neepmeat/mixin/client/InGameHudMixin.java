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

package com.neep.neepmeat.mixin.client;

import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.enlightenment.EnlightenmentSleepOverlays;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin
{
    @Inject(method = "render", at  = @At(value = "HEAD"), cancellable = true)
    public void onRender(GuiGraphics context, float tickDelta, CallbackInfo ci)
    {
        var renderer = PLCHudRenderer.getInstance();
        if ((renderer != null && renderer.onRender()) || InputInterceptorsClient.currentHandler() != null && InputInterceptorsClient.currentHandler().hideHud())
            ci.cancel();

        EnlightenmentSleepOverlays.render(context, tickDelta);
    }
}
