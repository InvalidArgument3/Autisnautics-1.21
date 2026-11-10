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

package com.neep.neepmeat.mixin;

import com.neep.neepmeat.item.MeatSteelHoeItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EnchantmentTargetMixins
{
    @Mixin(targets = "net/minecraft/enchantment/EnchantmentTarget$11")
    public static class Weapon
    {
        @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
        void onIsAcceptableItem(Item item, CallbackInfoReturnable<Boolean> cir)
        {
            if (item instanceof MeatSteelHoeItem)
            {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Mixin(targets = "net/minecraft/enchantment/EnchantmentTarget$12")
    public static class Digger
    {
        @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
        void onIsAcceptableItem(Item item, CallbackInfoReturnable<Boolean> cir)
        {
            if (item instanceof MeatSteelHoeItem)
            {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
