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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.neep.neepmeat.block.SpecialRail;
import com.neep.neepmeat.entity.MobEggEntity;
import com.neep.neepmeat.interfaces.AbstractMinecartEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin implements AbstractMinecartEntityAccess
{
    @Shadow protected abstract boolean willHitBlockAt(BlockPos pos);

    @Unique
    private Vec3 controllerVelocity = Vec3.ZERO;

    // Passenger velocity is reset at some point after this is called.
    // We have to inject outside moveOnRail for compatibility with Audaki Cart Engine, which cancels moveOnRail immediately.
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;moveOnRail(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"),
        locals = LocalCapture.CAPTURE_FAILSOFT)
    void tick(CallbackInfo ci, int i, int j, int k, BlockPos blockPos, BlockState blockState)
    {
        AbstractMinecart thisEntity = ((AbstractMinecart) (Object) (this));
        Entity passenger = thisEntity.getFirstPassenger();
        if (passenger instanceof Player player)
        {
            controllerVelocity = player.getDeltaMovement();
        }

        if (blockState.getBlock() instanceof SpecialRail specialRail)
        {
            specialRail.apply(thisEntity.level(), blockPos, blockState, thisEntity);
        }
    }

    @ModifyReturnValue(method = "collidesWith", at = @At("RETURN"))
    boolean modify(boolean prev, Entity other)
    {
        return !(other instanceof MobEggEntity) && prev;
    }

    @Override
    public Vec3 neepmeat$getControllerVelocity()
    {
        return controllerVelocity;
    }
}
