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

package com.neep.meatweapons.mixin;

import com.neep.meatweapons.interfaces.VehicleMovementHolder;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin implements VehicleMovementHolder
{
    @Unique private double vehicleMovementX;
    @Unique private double vehicleMovementY;
    @Unique private double vehicleMovementZ;

    @Inject(method = "onVehicleMove",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onOnVehicleMove(ServerboundMoveVehiclePacket packet, CallbackInfo ci, Entity entity, ServerLevel serverWorld, double d, double e, double f, double g, double h, double i, float j, float k, double l, double m, double n, double o, double p, boolean bl, boolean bl2)
    {
        vehicleMovementX = l;
        vehicleMovementY = m;
        vehicleMovementZ = n;
    }

    @Override
    public double meatweapons$movementX()
    {
        return vehicleMovementX;
    }

    @Override
    public double meatweapons$movementY()
    {
        return vehicleMovementY;
    }

    @Override
    public double meatweapons$movementZ()
    {
        return vehicleMovementZ;
    }
}
