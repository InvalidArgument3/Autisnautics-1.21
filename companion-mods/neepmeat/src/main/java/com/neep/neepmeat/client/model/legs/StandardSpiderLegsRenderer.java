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

package com.neep.neepmeat.client.model.legs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.client.model.entity.SpiderLegsRenderer;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import com.neep.neepmeat.implant.entity.SpiderLegsImplant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StandardSpiderLegsRenderer<T extends LivingEntity> implements SpiderLegsRenderer<T>
{
    private final ResourceLocation upperLeg;
    private final ResourceLocation lowerLeg;
    private final ResourceLocation abdomen;

    public StandardSpiderLegsRenderer(ResourceLocation upperLeg, ResourceLocation lowerLeg, ResourceLocation abdomen)
    {
        this.upperLeg = upperLeg;
        this.lowerLeg = lowerLeg;
        this.abdomen = abdomen;
    }

    @Override
    public void render(T player, SpiderLegsImplant legs, PoseStack matrices, float tickDelta, MultiBufferSource vcp, int i)
    {
        Level world = player.level();

        legs.getLegs().forEach(l -> renderLeg(world, player, legs, l, matrices, tickDelta, vcp, i));

        matrices.translate(0, legs.getMeanBodyOffset(tickDelta), 0);

        renderAbdomen(world, player, matrices, tickDelta, vcp);

    }

    private void renderAbdomen(Level world, T player, PoseStack matrices, float tickDelta, MultiBufferSource vcp)
    {
        BlockState state = Blocks.STONE.defaultBlockState();

        float yaw = Mth.lerp(tickDelta, player.yBodyRotO, player.yBodyRot);
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrices.translate(-0.5, 0, -0.5);
        BERenderUtils.renderModelSmooth(abdomen, matrices, world, player.blockPosition().above(), vcp);
        matrices.popPose();
    }

    private double moveTowards(double amount, double current, double end)
    {
        double difference = end - current;
        if (difference > 0)
            return current + Math.signum(difference) * Math.min(amount, Math.abs(difference));

        return end;
    }

    private void renderLeg(Level world, T player, SpiderLegsImplant legs, SpiderLegsImplant.Leg leg, PoseStack matrices, float tickDelta, MultiBufferSource vcp, int i)
    {
        matrices.pushPose();
        var offset1 = leg.rotatedOffset(tickDelta);
//        Vec3d origin = player.getLerpedPos(tickDelta).add(offset1);
        float legStartOffset = (float) legs.getMeanBodyOffset(tickDelta);
        Vec3 origin = player.getPosition(tickDelta).add(offset1).add(0, legStartOffset, 0);
//        Vec3d legStart = origin.add(0, legs.getMeanBodyOffset(tickDelta), 0);

        Vec3 lerped = leg.getPositionSmooth(tickDelta);

        double targetX = lerped.x;
        double targetY = lerped.y;
        double targetZ = lerped.z;

        float relX = (float) (targetX - origin.x);
        float relY = (float) (targetY - origin.y);
        float relZ = (float) (targetZ - origin.z);

        float yaw = (float) Math.toDegrees(Math.atan2(relX, relZ));

        float l = leg.segmentLength();
        float lx = Mth.sqrt(relX * relX + relZ * relZ);
        float ly = relY;

        float d5 = Mth.sqrt(lx * lx + ly * ly);

        // Constrain
        float x1 = lx / d5 * Math.min(d5, 2 * l - 0.0001f);
        float y1 = ly / d5 * Math.min(d5, 2 * l - 0.0001f);

        float d = Mth.sqrt((x1 * x1 + y1 * y1));

        float x2 = x1 / d;
        float y2 = y1 / d;

        // ???
        float a = (float) Math.acos((-(d * d)) / (-2 * d * l));

        // Rotation matrix
        float x3 = l * (x2 * Mth.cos(a) - y2 * Mth.sin(a));
        float y3 = l * (x2 * Mth.sin(a) + y2 * Mth.cos(a));

        // Debug lines
//        DebugRenderUtil.renderHCircle(matrices, (float) medianOffset.x, ((float) offset.y + relY + 0.05f), (float) medianOffset.z, stepRadius, 0, 0, 1, vcp);
//        matrices.push();
//        matrices.translate(offset1.x, offset1.y + legStartOffset, offset1.z);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
//        DebugRenderUtil.renderLine(matrices, 0, 0, 0, 0, y3, x3, 1, 0, 0, vcp);
//        DebugRenderUtil.renderLine(matrices, 0, y3, x3, (float) 0, relY, MathHelper.sqrt(relX * relX + relZ * relZ), 1, 1, 0, vcp);
//        matrices.pop();

        BlockState state = Blocks.STONE.defaultBlockState();

        matrices.translate(offset1.x, offset1.y + legStartOffset, offset1.z);
        matrices.mulPose(Axis.YP.rotationDegrees(yaw));

        matrices.pushPose();
        double angle1 = Math.atan2(x3, y3);
        matrices.mulPose(Axis.XP.rotation((float) angle1));
        matrices.mulPose(Axis.ZP.rotationDegrees(180));
        matrices.translate(0, -1, 0);
        BERenderUtils.renderModelSmooth(upperLeg, matrices, world, player.blockPosition().above(), vcp);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, y3, x3);
        double angle2 = Math.atan2((lx - x3), (ly - y3));
        matrices.mulPose(Axis.XP.rotation((float) angle2));
        matrices.mulPose(Axis.ZP.rotationDegrees(180));
        matrices.translate(0, -1, 0);
        matrices.scale(0.99f, 1, 0.99f); // Prevent axis fighting
        BERenderUtils.renderModelSmooth(lowerLeg, matrices, world, player.blockPosition().above(), vcp);
        matrices.popPose();

        matrices.popPose();
    }
}
