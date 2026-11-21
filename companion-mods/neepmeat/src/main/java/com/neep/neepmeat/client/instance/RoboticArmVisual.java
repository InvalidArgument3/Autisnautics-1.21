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

package com.neep.neepmeat.client.instance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class RoboticArmVisual extends AbstractBlockEntityVisual<RoboticArmBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance spinnyBit;
    private final TransformedInstance segment1;
    private final TransformedInstance segment2;
    private final PoseStack matrices = new PoseStack();

    public RoboticArmVisual(VisualizationContext ctx, RoboticArmBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.spinnyBit = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.ROBOTIC_ARM_SPINNY_BIT)).createInstance();
        this.segment1 = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.ROBOTIC_ARM_SEGMENT_1)).createInstance();
        this.segment2 = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.ROBOTIC_ARM_SEGMENT_2)).createInstance();
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        spinnyBit.translate(getVisualPosition()).setChanged();
        segment1.translate(getVisualPosition()).setChanged();
        segment2.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        spinnyBit.delete();
        segment1.delete();
        segment2.delete();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, spinnyBit, segment1, segment2);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        blockEntity.prevX = Mth.lerp(0.2, blockEntity.prevX, blockEntity.getX());
        blockEntity.prevY = Mth.lerp(0.2, blockEntity.prevY, blockEntity.getY());
        blockEntity.prevZ = Mth.lerp(0.2, blockEntity.prevZ, blockEntity.getZ());

//        Vec3d tipTarget = be.getTarget(tickDelta);
        double targetX = blockEntity.prevX;
        double targetY = blockEntity.prevY;
        double targetZ = blockEntity.prevZ;

        double relX = targetX - (blockEntity.getBlockPos().getX() + 0.5);
        double relY = targetY - (blockEntity.getBlockPos().getY() + 1 + 2 / 16f);
        double relZ = targetZ - (blockEntity.getBlockPos().getZ() + 0.5);

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

        float a = (float) Math.acos((-(d * d)) / (-2 * d * l));

        // Rotation matrix
        double x3 = l * (x2 * Mth.cos(a) - y2 * Mth.sin(a));
        double y3 = l * (x2 * Mth.sin(a) + y2 * Mth.cos(a));

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(yaw));
        matrices.translate(-0.5, -0.5, -0.5);

        spinnyBit.setTransform(matrices);
//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SPINNY_BIT, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);

        matrices.pushPose();
        matrices.translate(0, 2 - 1 / 16f, 0);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle1 = Math.atan2(x3, y3);
        matrices.mulPose(Axis.XP.rotation((float) angle1));
        matrices.translate(-0.5, 12 / 16f, -0.5);
        segment1.setTransform(matrices);
//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SEGMENT_1, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);
        matrices.popPose();

        matrices.translate(0, y3 + 2 - 1 / 16f, x3);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle2 = Math.atan2((lx - x3), (ly - y3));
        matrices.mulPose(Axis.XP.rotation((float) angle2));
        matrices.translate(-0.5, 12 / 16f, -0.5);
        segment2.setTransform(matrices);
//        BERenderUtils.renderModelSmooth(NMExtraModels.ROBOTIC_ARM_SEGMENT_2, matrices, be.getWorld(), be.getPos().up(), be.getCachedState(), vertexConsumers);
        matrices.popPose();

        spinnyBit.setChanged();
        segment1.setChanged();
        segment2.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

    @Override
    public Plan<Context> planFrame()
    {
        return RunnablePlan.of(this::planFrame);
    }
}
