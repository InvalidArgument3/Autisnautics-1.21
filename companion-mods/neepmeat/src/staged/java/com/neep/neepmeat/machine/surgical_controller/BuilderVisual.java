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

package com.neep.neepmeat.machine.surgical_controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(value = Dist.CLIENT)
public class BuilderVisual extends AbstractBlockEntityVisual<BuilderBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance robotModel;
    private final PoseStack matrixStack = new PoseStack();
    private final int animOffset;

    private float speed;
    private float smoothSpeed;

    public BuilderVisual(VisualizationContext ctx, BuilderBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        matrixStack.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        robotModel = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                MeatModels.cutout(NMExtraModels.BUILDER_ROBOT, false)).createInstance();
        robotModel.setTransform(matrixStack).setChanged();

        this.animOffset = (int) (Math.random() * 360);
    }

    @Override
    public void _delete()
    {
        robotModel.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        var robot = blockEntity.getRobot();

        robot.prevX = robot.clientX;
        robot.prevY = robot.clientY;
        robot.prevZ = robot.clientZ;

        // Smooth the robot's position
        robot.clientX = Mth.lerp(0.1d, robot.clientX, robot.getX());
        robot.clientY = Mth.lerp(0.1d, robot.clientY, robot.getY());
        robot.clientZ = Mth.lerp(0.1d, robot.clientZ, robot.getZ());
        robot.clientYaw = Mth.rotLerp(0.1f, robot.clientYaw, robot.getYaw());

        double vx = robot.clientX - robot.prevX;
        double vy = robot.clientY - robot.prevY;
        double vz = robot.clientZ - robot.prevZ;

        // Tilt would look jerky otherwise
        this.smoothSpeed = Mth.rotLerp(0.5f, this.smoothSpeed, (float) Math.sqrt(vx * vx + vz * vz));

        matrixStack.pushPose();
        matrixStack.translate(
                robot.clientX - blockEntity.getBlockPos().getX() - 0.5,
                robot.clientY - blockEntity.getBlockPos().getY() - 0.5,
                robot.clientZ - blockEntity.getBlockPos().getZ() - 0.5);

        float tickDelta = context.partialTick();
        int time = (int) ((blockEntity.getLevel().getGameTime() % 360) + animOffset);
        double sinTime = Math.sin(Math.toRadians(time * 7)) * Math.cos(Math.toRadians(tickDelta) * 7)
                + Math.cos(Math.toRadians(time * 7)) * Math.sin(Math.toRadians(tickDelta) * 7);
        boolean floating = robot.getState();
        matrixStack.translate(0, 0.5 + (!floating ? 0.1 * sinTime : 0), 0);

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(Axis.YN.rotationDegrees(robot.clientYaw + 180));
        matrixStack.mulPose(Axis.XN.rotationDegrees((float) (100 * smoothSpeed)));
        matrixStack.translate(-0.5, -0.5, -0.5);

        robotModel.setTransform(matrixStack);

        matrixStack.popPose();

        robotModel.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        var robot = blockEntity.getRobot();
        relight(BlockPos.containing(robot.clientX, robot.clientY, robot.clientZ), robotModel);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {
    }
}