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
import com.neep.meatlib.client.visual.DynamicBlockEntityVisual;
import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.input.ImplanterInterceptorHandler;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import com.neep.neepmeat.screen_handler.ImplanterScreenHandler;
import com.neep.neepmeat.util.NMMaths;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ImplanterVisual extends DynamicBlockEntityVisual<ImplanterBlockEntity> implements SimpleDynamicVisual, TickableVisual
{
    private final TransformedInstance spinnyBit;
    private final TransformedInstance segment1;
    private final TransformedInstance segment2;
    private final TransformedInstance head;
    private final PoseStack matrices = new PoseStack();

    public ImplanterVisual(VisualizationContext ctx, ImplanterBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.spinnyBit = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.IMPLANTER_SPINNY_BIT)).createInstance();
        this.segment1 = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.ROBOTIC_ARM_SEGMENT_1)).createInstance();
        this.segment2 = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.ROBOTIC_ARM_SEGMENT_2)).createInstance();
        this.head = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.IMPLANTER_HEAD)).createInstance();
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        this.spinnyBit.translate(getVisualPosition()).setChanged();
        this.segment1.translate(getVisualPosition()).setChanged();
        this.segment2.translate(getVisualPosition()).setChanged();
        this.head.translate(getVisualPosition()).setChanged();

        blockEntity.renderYaw = blockEntity.getYaw();
        blockEntity.renderX = blockEntity.getPolX();
        blockEntity.renderY = blockEntity.getPolY();
    }

    @Override
    public void _delete()
    {
        spinnyBit.delete();
        segment1.delete();
        segment2.delete();
        head.delete();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, spinnyBit, segment1, segment2, head);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        if (Minecraft.getInstance().player.containerMenu instanceof ImplanterScreenHandler handler && handler.getImplanter().equals(blockEntity))
        {
            segment2.scale(0, 0, 0);
            segment1.scale(0, 0, 0);
            head.scale(0, 0, 0);
        }
        else
        {
            blockEntity.renderYaw = NMMaths.angleLerp(0.2f, blockEntity.renderYaw, blockEntity.getYaw());
            blockEntity.renderX = Mth.lerp(0.2f, blockEntity.renderX, blockEntity.getPolX());
            blockEntity.renderY = Mth.lerp(0.2f, blockEntity.renderY, blockEntity.getPolY());

            float yaw = (float) Math.toDegrees(blockEntity.renderYaw);

            float l = 2 + 5 / 16f;
            double lx = blockEntity.renderX;
            double ly = blockEntity.renderY - 1;

            double d5 = Math.sqrt(lx * lx + ly * ly);

            // Constrain
            double x1 = lx / d5 * Math.min(d5, 2 * l - 0.0001);
            double y1 = ly / d5 * Math.min(d5, 2 * l - 0.0001);

            double d = Math.sqrt((x1 * x1 + y1 * y1));

            double x2 = x1 / d;
            double y2 = y1 / d;

            // Rotation matrix
            float a = (float) Math.acos((-(d * d)) / (-2 * d * l));
            double x3 = l * (x2 * Mth.cos(a) - y2 * Mth.sin(a));
            double y3 = l * (x2 * Mth.sin(a) + y2 * Mth.cos(a));

            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees(yaw));
            matrices.translate(-0.5, -0.5, -0.5);

            spinnyBit.setTransform(matrices);

            matrices.pushPose();
            matrices.translate(0, 2 - 1 / 16f, 0);
            matrices.translate(0.5, -12 / 16f, 0.5);
            double angle1 = Math.atan2(x3, y3);
            matrices.mulPose(Axis.XP.rotation((float) angle1));
            matrices.translate(-0.5, 12 / 16f, -0.5);
            segment1.setTransform(matrices);
            matrices.popPose();

            matrices.pushPose();
            matrices.translate(0, y3 + 2 - 1 / 16f, x3);
            matrices.translate(0.5, -12 / 16f, 0.5);
            double angle2 = Math.atan2((lx - x3), (ly - y3));
            matrices.mulPose(Axis.XP.rotation((float) angle2));
            matrices.translate(-0.5, 12 / 16f, -0.5);
            segment2.setTransform(matrices);
            matrices.popPose();

            matrices.translate(0, y1 + 0.5, x1);
            head.setTransform(matrices);

            matrices.popPose();
        }

        spinnyBit.setChanged();
        segment1.setChanged();
        segment2.setChanged();
        head.setChanged();
    }

    public void tick()
    {
        if (InputInterceptorsClient.currentHandler() instanceof ImplanterInterceptorHandler)
        {
            segment2.scale(0, 0, 0);
            head.scale(0, 0, 0);
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

    @Override
    public Plan<SimpleDynamicVisual.Context> planFrame()
    {
        return RunnablePlan.of(this::beginFrame);
    }

    @Override
    public Plan<TickableVisual.Context> planTick()
    {
        return RunnablePlan.of(this::tick);
    }
}
