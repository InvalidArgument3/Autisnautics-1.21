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

package com.neep.neepmeat.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class RoboticArmRenderer implements BlockEntityRenderer<RoboticArmBlockEntity>
{
    public RoboticArmRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(RoboticArmBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        be.prevX = Mth.lerp(0.2, be.prevX, be.getX());
        be.prevY = Mth.lerp(0.2, be.prevY, be.getY());
        be.prevZ = Mth.lerp(0.2, be.prevZ, be.getZ());

//        Vec3d tipTarget = be.getTarget(tickDelta);
        double targetX = be.prevX;
        double targetY = be.prevY;
        double targetZ = be.prevZ;

        double relX = targetX - (be.getBlockPos().getX() + 0.5);
        double relY = targetY - (be.getBlockPos().getY() + 1 + 2 / 16f);
        double relZ = targetZ - (be.getBlockPos().getZ() + 0.5);

        float yaw = (float) Math.toDegrees(Math.atan2(relX, relZ));

        float l = 2 + 5 / 16f;
        double lx = Math.sqrt(relX * relX + relZ * relZ);
        double ly = relY;

        double d5 = Math.sqrt(lx * lx + ly * ly);

        // Constrain
        double x1 = lx / d5 * Math.min(d5, 2 * l - 0.0001);
        double y1 = ly / d5 * Math.min(d5, 2 * l - 0.0001);

        double d = Math.sqrt((x1 * x1 + y1 * y1));

        double x2 = x1 / d;
        double y2 = y1 / d;

        // ???
        double a = Math.acos((-(d * d)) / (-2 * d * l));

        // Rotation matrix
        double x3 = l * (x2 * cos(a) - y2 * sin(a));
        double y3 = l * (x2 * sin(a) + y2 * cos(a));

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(yaw));
        matrices.translate(-0.5, -0.5, -0.5);

//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SPINNY_BIT, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);

        matrices.pushPose();
        matrices.translate(0, 2 - 1 / 16f, 0);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle1 = Math.atan2(x3, y3);
        matrices.mulPose(Axis.XP.rotation((float) angle1));
        matrices.translate(-0.5, 12 / 16f, -0.5);
//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SEGMENT_1, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);
        matrices.popPose();

        matrices.translate(0, y3 + 2 - 1 / 16f, x3);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle2 = Math.atan2((lx - x3), (ly - y3));
        matrices.mulPose(Axis.XP.rotation((float) angle2));
        matrices.translate(-0.5, 12 / 16f, -0.5);
//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SEGMENT_2, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);
        matrices.popPose();

    }
}
