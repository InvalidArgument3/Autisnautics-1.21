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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value= EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public class KeyboardMixin
{
    @Inject(method = "onKey", at = @At(value = "HEAD"), cancellable = true)
    void preOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci)
    {
        if (KeyboardEvents.PRE_INPUT.invoker().onKey(window, key, scancode, action, modifiers))
            ci.cancel();
    }

    @Inject(method = "onKey", at = @At(value = "TAIL"))
    void postOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci)
    {
        KeyboardEvents.POST_INPUT.invoker().onKey(window, key, scancode, action, modifiers);
    }
}
