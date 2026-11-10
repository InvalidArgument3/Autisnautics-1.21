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

import com.neep.neepmeat.api.big_block.BlockPlacementPreview;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin
{
    @Shadow
    protected ClientLevel world;

    @Shadow public abstract void addParticle(Particle particle);

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    void onAddBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci)
    {
        if (state.getBlock() instanceof BlockPlacementPreview preview)
        {
            preview.addBlockBreakParticles(world, pos, state, (x, y, z, vx, vy, vz) ->
            {
                addParticle(
                        new TerrainParticle(this.world, x, y, z, vx, vy, vz, state, pos)
                );
            });

            ci.cancel();
        }
    }
}
