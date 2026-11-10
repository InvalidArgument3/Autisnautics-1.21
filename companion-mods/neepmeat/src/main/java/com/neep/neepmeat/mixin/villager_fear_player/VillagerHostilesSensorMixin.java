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

package com.neep.neepmeat.mixin.villager_fear_player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin
{
    @Inject(method = "isCloseEnoughForDanger", at  = @At("HEAD"), cancellable = true)
    private void injectPlayer(LivingEntity villager, LivingEntity target, CallbackInfoReturnable<Boolean> cir)
    {
        // No need to do the enlightenment event check here as this is queried after isHostile.
        if (target instanceof Player player)
        {
            cir.setReturnValue(player.distanceToSqr(villager) <= 8 * 8);
            cir.cancel();
        }
    }

    @ModifyReturnValue(method = "isHostile", at = @At("RETURN"))
    private boolean onIsHostile(boolean original, LivingEntity entity)
    {
        if (entity instanceof Player player)
        {
            var manager = EnlightenmentManager.get(player);
            return original || manager.scareVillagers;
        }
        return original;
    }
}
