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

package com.neep.neepmeat.machine.motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.meatlib.client.visual.DynamicBlockEntityVisual;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class MotorVisual extends DynamicBlockEntityVisual<MotorBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance rotor;
    private final PoseStack matrices = new PoseStack();

    public MotorVisual(VisualizationContext ctx, MotorBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.P_MOTOR_ROTOR)).createInstance();
        rotor.setIdentityTransform().translate(getVisualPosition());
        rotor.setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        matrices.pushPose();
        Direction facing = blockEntity.getBlockState().getValue(BaseFacingBlock.FACING);

        float delta = Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        blockEntity.currentSpeed = (float) (blockEntity.rotorSpeed * Mth.lerp(0.1, blockEntity.currentSpeed, blockEntity.getSpeed()));
        blockEntity.angle = Mth.wrapDegrees(blockEntity.angle + blockEntity.currentSpeed * delta);

        rotor.setIdentityTransform().translate(getVisualPosition()).center()
            .rotate(Axis.YN.rotationDegrees(facing.toYRot()))
            .rotate(Axis.XN.rotationDegrees(facing == Direction.UP ? 90 : facing == Direction.DOWN ? -90 : 0))
            .rotate(Axis.ZP.rotationDegrees(blockEntity.angle))
            .uncenter();

        rotor.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(blockEntity.getBlockPos(), rotor);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

    @Override
    protected void _delete()
    {
        rotor.delete();
    }

   
}
