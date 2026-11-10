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

package com.neep.neepmeat.machine.advanced_motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class AdvancedMotorVisual extends AbstractBlockEntityVisual<AdvancedMotorBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance rotor;
    private final PoseStack matrices = new PoseStack();

    public AdvancedMotorVisual(VisualizationContext ctx, AdvancedMotorBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.P_MOTOR_ROTOR)).createInstance();
        rotor.setIdentityTransform().translate(getVisualPosition());
        rotor.setChanged();
    }

    @Override
    public void _delete()
    {
        rotor.delete();
    }

    @Override
    public void updateLight(float v)
    {
        relight(getVisualPosition(), rotor);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum()))
            return;

        matrices.pushPose();
        Direction facing = blockEntity.getBlockState().getValue(BaseFacingBlock.FACING);

        float delta = Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaFrameTime();
        blockEntity.currentSpeed = (float) (blockEntity.rotorSpeed * Mth.lerp(0.1, blockEntity.currentSpeed, blockEntity.getSpeed()));
        blockEntity.angle = Mth.wrapDegrees(blockEntity.angle + blockEntity.currentSpeed * delta);

        BERenderUtils.rotateFacingSouth(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.ZP.rotationDegrees(blockEntity.angle));
        matrices.translate(-0.5, -0.5, -0.5);

        rotor.setTransform(matrices);

        matrices.popPose();

        rotor.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {
        consumer.accept(rotor);
    }

   
}
