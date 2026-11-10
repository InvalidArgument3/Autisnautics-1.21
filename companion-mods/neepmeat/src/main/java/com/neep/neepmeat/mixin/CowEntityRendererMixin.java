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

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.hydraulic_press.HydraulicPressBlockEntity;
import com.neep.neepmeat.machine.hydraulic_press.HydraulicPressVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobRenderer.class)
public class CowEntityRendererMixin
{
    @Inject(method = "render(Lnet/minecraft/entity/mob/MobEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
    public void render(Mob entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci)
    {
        BlockPos pos = entity.blockPosition().above();
        Level world = entity.level();
        if (entity instanceof Cow && world.getBlockState(pos).is(NMBlocks.HYDRAULIC_PRESS) && world.getBlockEntity(pos) instanceof HydraulicPressBlockEntity be)
        {
            float height = entity.getBbHeight();
            float scale = (height - be.renderExtension * HydraulicPressVisual.MAX_DISPLACEMENT) / height;
            float scaleH = 1 + (1 - scale) * 0.3f;
            matrices.scale(scaleH, scale, scaleH);
        }
    }
}
