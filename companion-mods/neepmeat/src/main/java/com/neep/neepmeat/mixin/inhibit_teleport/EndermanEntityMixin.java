/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.mixin.inhibit_teleport;

import com.neep.neepmeat.interfaces.EndermanExtension;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public class EndermanEntityMixin implements EndermanExtension
{
    @Unique private boolean inhibitTeleport = false;
    @Unique private int counter;

    @Inject(method = "mobTick", at = @At("TAIL"))
    void onTick(CallbackInfo ci)
    {
        if (counter > 0)
            counter--;
        else
            inhibitTeleport = false;
    }

    @Inject(method = "teleportTo(DDD)Z", at = @At("HEAD"), cancellable = true)
    void interceptTeleport(CallbackInfoReturnable<Boolean> cir)
    {
        if (inhibitTeleport)
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    public void neepmeat$setDisableTeleport(boolean value, int duration)
    {
        this.inhibitTeleport = value;
        this.counter = duration;
    }
}
