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

import com.mojang.math.Axis;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.breaker.LinearOscillatorBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class LinearOscillatorVisual extends AbstractBlockEntityVisual<LinearOscillatorBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance armature;
    private float extension = 0;

    public LinearOscillatorVisual(VisualizationContext ctx, LinearOscillatorBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);

        armature = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LO_ARMATURE)).createInstance();
        armature.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        armature.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        Direction facing = blockEntity.getBlockState().getValue(BaseFacingBlock.FACING);
        extension = (float) Mth.lerp(0.3, extension, blockEntity.extension);
        float maxExtension = 1 / 16f * 9;
        Quaternionf yaw =
                Axis.YN.rotationDegrees(facing.toYRot())
                        .rotateX(!facing.getAxis().isVertical() ? 0 : (facing == Direction.UP ? -Mth.HALF_PI : Mth.HALF_PI));
                ;

        armature.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotate(yaw)
                .uncenter()
                .translate(0, 0, extension * maxExtension);

        armature.setChanged();

//        BERenderUtils.rotateFacingSouth(facing, matrices);
//        matrices.translate(0.5, 0.5, 0.5);
//        matrices.translate(0, 0, blockEntity.clientExtension * maxExtension);
//        matrices.translate(-0.5, -0.5, -0.5);
//        BERenderUtils.renderModel(NMExtraModels.LO_ARMATURE, matrices, blockEntity.getWorld(), blockEntity.getPos(), blockEntity.getCachedState(), vertexConsumers);
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, armature);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
