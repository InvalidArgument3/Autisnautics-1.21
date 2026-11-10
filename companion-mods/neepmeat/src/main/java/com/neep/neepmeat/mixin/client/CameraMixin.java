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
import com.neep.neepmeat.client.renderer.PlayerLegsRenderer;
import com.neep.neepmeat.implant.entity.ImplantRenderingHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin
{
    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow private Vec3 pos;

    @Shadow private Entity focusedEntity;

    @Inject(method = "update", at = @At(value = "TAIL"))
    void onUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci)
    {
        var renderer = PLCHudRenderer.getInstance();
        if (renderer != null)
            renderer.onCameraUpdate(tickDelta);

        if (InputInterceptorsClient.currentHandler() != null)
            InputInterceptorsClient.currentHandler().overrideCamera(Minecraft.getInstance(), (Camera) (Object) this, tickDelta);
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    void updateAfterSetPos(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci)
    {
        if (focusedEntity instanceof Player player && ((ImplantRenderingHelper) player).neepmeat$getSpiderLegs() != null)
        {
            PlayerLegsRenderer.onUpdateCamera((Camera) (Object) this, area, player, thirdPerson, inverseView, tickDelta);
        }
    }

    @Inject(method = "updateEyeHeight", at = @At("TAIL"))
    void onUpdateEyeHeight(CallbackInfo ci)
    {
        if (focusedEntity instanceof Player player && ((ImplantRenderingHelper) player).neepmeat$getSpiderLegs() != null)
        {
            PlayerLegsRenderer.onUpdateEyeHeight((Camera) (Object) this, player);
        }
    }
}
