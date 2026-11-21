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

package com.neep.meatweapons.particle;

import com.neep.meatlib.graphics.GraphicsEffectType;
import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.meatweapons.MeatWeapons;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MWGraphicsEffects
{

    public static void init()
    {}

    public static final GraphicsEffectType BEAM = GraphicsEffects.register(MeatWeapons.NAMESPACE, "beam", new GraphicsEffectType());
    public static final GraphicsEffectType BULLET_TRAIL = GraphicsEffects.register(MeatWeapons.NAMESPACE, "bullet_trail", new GraphicsEffectType());
    public static final GraphicsEffectType RICOCHET_BULLET_TRAIL = GraphicsEffects.register(MeatWeapons.NAMESPACE, "ricochet_bullet_trail", new GraphicsEffectType());
    public static final GraphicsEffectType LONG_BULLET_TRAIL = GraphicsEffects.register(MeatWeapons.NAMESPACE, "long_bullet_trail", new GraphicsEffectType());
    public static final GraphicsEffectType BONE_STEALER = GraphicsEffects.register(MeatWeapons.NAMESPACE, "bone_stealer", new GraphicsEffectType());

    public static final GraphicsEffectType ZAP = GraphicsEffects.register(MeatWeapons.NAMESPACE, "zap", new GraphicsEffectType());
//    public static final GraphicsEffectType NORMAL_MUZZLE_FLASH = GraphicsEffects.register(MeatWeapons.NAMESPACE, "normal_muzzle_flash", new GraphicsEffectType());

    public static final GraphicsEffectType SMALL_PHAGE_RAY = GraphicsEffects.register(MeatWeapons.NAMESPACE, "small_phage_ray", new GraphicsEffectType());

    public static void syncBeamEffect(ServerPlayer player, GraphicsEffectType factory, Level world, Vec3 start, Vec3 end, Vec3 velocity, float scale, int maxTime)
    {
        if (world.isClientSide)
            throw new IllegalStateException("packet create called on the client!");

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        GraphicsEffects.CHANNEL.emitter(player).apply(
                world.dimension(), factory, start, end, velocity, scale, maxTime, byteBuf);
    }

}
