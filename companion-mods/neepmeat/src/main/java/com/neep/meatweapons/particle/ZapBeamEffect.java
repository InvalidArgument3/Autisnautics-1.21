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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.BeamRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ZapBeamEffect extends BeamGraphicsEffect
{
    public static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/zap_trail.png");
    public static final RenderType TRAIL_LAYER = RenderType.entityTranslucent(TRAIL_TEXTURE);

    public ZapBeamEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, start, end, col, scale, maxTime, buf);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        matrices.pushPose();
        VertexConsumer consumer = consumers.getBuffer(TRAIL_LAYER);
        float x = (maxTime - time + 2 - tickDelta) / (float) maxTime;
        float length = 0.5f;
        float distance = (float) end.distanceTo(start);
        Vec3 beam = end.subtract(start).normalize();
        Vec3 newStart = start.add(beam.scale(distance * x));
        Vec3 newEnd = start.add(beam.scale((distance * x) + length));
        BeamRenderer.renderBeam(matrices, consumer, camera.getPosition(),
                newStart, newEnd, 255, 255, 255,
               255, scale, 255);
        matrices.popPose();
    }
}
