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

package com.neep.neepmeat.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.transport.api.pipe.AbstractPipeBlock;
import com.neep.neepmeat.transport.api.pipe.PrecisePipeBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.*;

@OnlyIn(Dist.CLIENT)
public class WrenchPlacementHelper
{
    public static void init()
    {
        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, ctx) ->
        {
            BlockState state = ctx.blockState();
            PoseStack matrices = worldRenderContext.matrixStack();
            BlockPos pos = ctx.blockPos();

            Player player = Minecraft.getInstance().player;

            var block = state.getBlock();
            if (((block instanceof PrecisePipeBlock ppb && ppb.supportsWrenching()) || block instanceof AbstractPipeBlock)
                    && (player.getMainHandItem().is(NMTags.WRENCH) || player.getOffhandItem().is(NMTags.WRENCH)))
            {
                BlockHitResult result = (BlockHitResult) Minecraft.getInstance().hitResult;
                Direction useDirection = PrecisePipeBlock.getUseDirection(result.getDirection(), pos, result.getLocation());

                MultiBufferSource vcp = worldRenderContext.consumers();
                if (vcp != null)
                {
                    matrices.pushPose();

                    Vec3 camPos = worldRenderContext.camera().getPosition();

                    if (MeatLib.vsUtil != null)
                        MeatLib.vsUtil.CLIENT.transformRenderIfOnShip(matrices, new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
                    else
                        matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                    Matrix4f local = new Matrix4f();
                    local.translate(0.5f, 0.5f, 0.5f);
                    local.rotate(useDirection.getRotation());
                    local.translate(-0.5f, -0.5f, -0.5f);

                    matrices.translate(-camPos.x, -camPos.y, -camPos.z);
                    drawOutline(vcp,
                            matrices.last().pose(), matrices.last().normal(),
                            local);

                    matrices.popPose();

                }
                return false;
            }

            return true;
        });
    }

//    private static void drawSquare(VertexConsumerProvider vcp, Matrix4f matrix4f, float f, float g, float h, float i, float j, float k)
    // f j h; f j k; i j h; i j k
    private static void drawOutline(MultiBufferSource vcp, Matrix4f matrix4f, Matrix3f normal, Matrix4f local)
    {
        VertexConsumer consumer = vcp.getBuffer(RenderType.lines());

        float r = 1;
        float g = 0.36f;
        float b = 0.13f;
        float a = 0.8f;

        VoxelShape shape = PrecisePipeBlock.box(4, 13, 4, 12, 19, 12);

        // Can't apply an arbitrary transform to a MatrixStack's normal as well as position.
        matrix4f.mul(local);
        normal.rotate(local.getNormalizedRotation(new Quaternionf()));

        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) ->
        {
            float k = (float) (maxX - minX);
            float l = (float) (maxY - minY);
            float m = (float) (maxZ - minZ);
            float n = Mth.sqrt(k * k + l * l + m * m);
//            Vector4f p1 = local.transform(new Vector4f());
            consumer.addVertex(matrix4f, (float) (minX), (float) (minY), (float) (minZ)).setColor(r, g, b, a).setNormal(normal, k /= n, l /= n, m /= n);
            consumer.addVertex(matrix4f, (float) (maxX), (float) (maxY), (float) (maxZ)).setColor(r, g, b, a).setNormal(normal, k, l, m);
        });
    }
}
