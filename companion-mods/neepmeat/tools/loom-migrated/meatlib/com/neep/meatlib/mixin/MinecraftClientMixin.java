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

import com.neep.meatlib.client.event.UseAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin
{
    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "doItemUse", at = @At(value = "HEAD"), cancellable = true)
    void onItemUse(CallbackInfo ci)
    {
        if (!UseAttackCallback.DO_USE.invoker().context(Minecraft.getInstance(), player)) ci.cancel();
    }

    @Inject(method = "doAttack", at = @At(value = "HEAD"), cancellable = true)
    void onAttack(CallbackInfoReturnable<Boolean> cir)
    {
        if (!UseAttackCallback.DO_ATTACK.invoker().context(Minecraft.getInstance(), player))
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At(value = "HEAD"), cancellable = true)
    void onAttack(boolean breaking, CallbackInfo ci)
    {
        if (breaking)
        {
            if (!UseAttackCallback.DO_ATTACK.invoker().context(Minecraft.getInstance(), player))
            {
                ci.cancel();
            }
        }
    }
}
