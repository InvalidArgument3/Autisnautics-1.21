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

package com.neep.neepmeat.machine.reactor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ReactionCoreRenderer implements BlockEntityRenderer<ReactionCoreBlockEntity>
{
    public ReactionCoreRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(ReactionCoreBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        entity.lerpIncidentZoneRadius = Mth.lerp(0.1, entity.lerpIncidentZoneRadius, entity.clientIncidentZoneRadius);
        float radius = (float) entity.lerpIncidentZoneRadius;
        if (radius >= 0.8)
        {
            // Sphere time!
            VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.endPortal());

            matrices.translate(0.5, 0.5, 0.5);
            Matrix4f top = matrices.last().pose();

            int lat = 20;
            int lon = 20;

            for (int i = 0; i < lat; ++i)
            {
                float yaw1 = Mth.TWO_PI * ((float) i / lat);
                float yaw2 = Mth.TWO_PI * ((float) (i + 1) / lat);

                // Only make half a wedge.
                for (int j = 0; j < lon / 2; ++j)
                {
                    // Subtract half pi to rotate the wedge
                    float pitch1  = Mth.TWO_PI * ((float) j / lon) - Mth.HALF_PI;
                    float pitch2  = Mth.TWO_PI * ((float) (j + 1) / lon) - Mth.HALF_PI;

                    float xzLen1 = radius * Mth.cos(pitch1);
                    float xzLen2 = radius * Mth.cos(pitch2);

                    float y1 = radius * Mth.sin(pitch1);
                    float y2 = radius * Mth.sin(pitch2);

                    float x1 = xzLen1 * Mth.cos(yaw1);
                    float x2 = xzLen1 * Mth.cos(yaw2);
                    float z1 = xzLen1 * Mth.sin(yaw1);
                    float z2 = xzLen1 * Mth.sin(yaw2);

                    float x3 = xzLen2 * Mth.cos(yaw1);
                    float x4 = xzLen2 * Mth.cos(yaw2);
                    float z3 = xzLen2 * Mth.sin(yaw1);
                    float z4 = xzLen2* Mth.sin(yaw2);

                    consumer.vertex(top, x2, y1, z2).endVertex();
                    consumer.vertex(top, x1, y1, z1).endVertex();
                    consumer.vertex(top, x3, y2, z3).endVertex();
                    consumer.vertex(top, x4, y2, z4).endVertex();
                }
            }
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(ReactionCoreBlockEntity blockEntity)
    {
        return true;
    }
}
