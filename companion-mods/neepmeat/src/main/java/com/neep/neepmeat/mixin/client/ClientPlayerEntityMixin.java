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

import com.mojang.authlib.GameProfile;
import com.neep.neepbus.client.input.InputInterceptorHandler;
import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepbus.input.InputInterceptorPlayer;
import com.neep.neepbus.input.InputInterceptors;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayer
{
//    private final SpiderLegsModel spiderLegs = new SpiderLegsModel(this);

    @Shadow @Final protected Minecraft client;

    @Shadow private int clientPermissionLevel;

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "tickNewAi", at = @At(value = "HEAD"), cancellable = true)
    void onTickNewAi(CallbackInfo ci)
    {
        // Erm...
        // Deactivate if (there is a screen and no handler) or (a screen that the handler does not accept) or (there is no screen and a handler that needs a screen)
        InputInterceptorHandler handler = InputInterceptorsClient.currentHandler();
        if (InputInterceptorsClient.isActive() &&
                ((client.screen != null && (handler == null || !handler.acceptScreen(client.screen)))
                    || client.screen == null && handler != null && handler.requiresScreen()
                ))
        {
            InputInterceptorsClient.signalDeactivate();
        }

        if (PLCHudRenderer.active())
            ci.cancel();
    }

//    @Override
//    public SpiderLegsModel neepmeat$getSpiderLegs()
//    {
//        return spiderLegs;
//    }
}
