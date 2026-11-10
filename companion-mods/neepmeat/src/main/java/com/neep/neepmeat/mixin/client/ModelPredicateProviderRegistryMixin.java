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

import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.init.NMComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/client/item/ModelPredicateProviderRegistry$1")
public abstract class ModelPredicateProviderRegistryMixin
{
    @Shadow protected abstract double getTime(Level par1, double par2);

    @Inject(method = "unclampedCall", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/ModelPredicateProviderRegistry$1;getTime(Lnet/minecraft/world/World;D)D"),
            cancellable = true)
    void onClockUnclambedCall(ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir)
    {
        var player = Minecraft.getInstance().player;
        if (player != null && NMComponents.ENLIGHTENMENT_MANAGER.get(player) instanceof PlayerEnlightenmentManager manager
            && manager.randomClock)
        {
            cir.setReturnValue((float) getTime(clientWorld, Math.random()));
        }
    }
}
