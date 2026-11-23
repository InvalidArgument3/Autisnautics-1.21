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

package com.neep.meatlib.graphics;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface SyncEffect
{
    ChannelFormat<SyncEffect> FORMAT = ChannelFormat.builder(SyncEffect.class)
            .param(ParamCodecs.IDENTIFIER.map(ParamCodecs.as(ResourceKey.class), ResourceKey::location, i -> ResourceKey.create(Registries.DIMENSION, i)))
            .param(ParamCodecs.registryValue(GraphicsEffectType.class, GraphicsEffects.GRAPHICS_EFFECTS))
            .param(ParamCodecs.VEC3D)
            .param(ParamCodecs.VEC3D)
            .param(ParamCodecs.VEC3D)
            .param(ParamCodec.FLOAT)
            .param(ParamCodec.INT)
            .param(ParamCodec.BUF)
            .build();

    void apply(ResourceKey<Level> worldKey, GraphicsEffectType type, Vec3 start, Vec3 end, Vec3 velocity, float scale, int maxTime, FriendlyByteBuf byteBuf);
}
