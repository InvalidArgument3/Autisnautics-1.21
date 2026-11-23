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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.neep.meatlib.attachment.itemstack.MeatItemStack;
import com.neep.meatlib.client.event.AppendTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MeatItemStack
{
    // What was this even for?
//    private final NbtCompound volatileNbt = new NbtCompound();

//    @Override
//    public NbtCompound getVolatileNbt()
//    {
//        return volatileNbt;
//    }

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Component> modifyTooltip(List<Component> original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) TooltipFlag ctx)
    {
        ItemStack stack = (ItemStack) (Object) this;
        AppendTooltipEvent.EVENT.invoker().onAppendTooltip(stack, player != null ? player.level() : null, ctx, original);

        return original;
    }

    @Inject(at = @At("TAIL"), method = "getTooltip")
    private void onTooltip(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir)
    {
    }
}
