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

package com.neep.meatlib.mixin;

import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.meatlib.client.event.ScrollEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MouseHandler.class)
public class MouseMixin
{
    @Shadow @Final private Minecraft client;

    @Inject(method = "onMouseButton", at = @At(value = "HEAD"))
    void preMouseButton(long window, int button, int action, int mods, CallbackInfo ci)
    {
        KeyboardEvents.PRE_INPUT.invoker().onKey(window, button, -1, action, mods);
    }

    @Inject(method = "onMouseButton", at = @At(value = "TAIL"))
    void postMouseButton(long window, int button, int action, int mods, CallbackInfo ci)
    {
        KeyboardEvents.POST_INPUT.invoker().onKey(window, button, -1, action, mods);
    }

    @Inject(method = "onMouseScroll", at = @At(value = "HEAD"), cancellable = true)
    void preMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci)
    {
        if (window == client.getWindow().getWindow() && client.level != null && client.player != null && client.screen == null)
        {
            double amount = (client.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical)
                    * client.options.mouseWheelSensitivity().get();

            boolean override = ScrollEvents.PRE_INGAME_SCROLL.invoker().onScroll(client, client.level, client.player, window, amount);

            if (override)
            {
                ci.cancel();
            }
        }
    }
}
