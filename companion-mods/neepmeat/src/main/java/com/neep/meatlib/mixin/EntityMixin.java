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
import com.neep.meatlib.api.event.EntityNbtEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin
{
    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void onWriteNbt(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir)
    {
        EntityNbtEvents.WRITE.invoker().writeNbt((Entity) (Object) this, nbt);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void onReadNbt(CompoundTag nbt, CallbackInfo ci)
    {
        EntityNbtEvents.READ.invoker().writeNbt((Entity) (Object) this, nbt);
    }

    @ModifyReturnValue(method = "getStandingEyeHeight", at = @At("RETURN"))
    protected float onGetStandingEyeHeight(float original)
    {
        return original;
    }

    @ModifyReturnValue( method = "getEyeY", at = @At("RETURN"))
    protected double onGetEyeY(double original)
    {
        return original;
    }
}
