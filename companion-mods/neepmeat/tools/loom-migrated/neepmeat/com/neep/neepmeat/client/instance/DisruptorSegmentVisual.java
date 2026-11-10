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

import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorSegmentBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorSegmentBlock;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class DisruptorSegmentVisual extends AbstractBlockEntityVisual<DisruptorSegmentBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance centre;
    private final TransformedInstance[] arms;

    private float rotSpeedRadians;
    private float rotation;
    private float lerpExtension = -1;

    public DisruptorSegmentVisual(VisualizationContext ctx, DisruptorSegmentBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);

        centre = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.DISRUPTOR_SEGMENT_CENTRE))
                .createInstance()
                .translate(getVisualPosition());
        centre.setChanged();

        arms = new TransformedInstance[4];
        for (int i = 0; i < 4; ++i)
        {
            arms[i] = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.DISRUPTOR_SEGMENT_ARM))
                    .createInstance()
                    .translate(getVisualPosition());
            arms[i].setChanged();
        }
    }

    @Override
    public void _delete()
    {
        centre.delete();
        for (var arm : arms)
            arm.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        if (!Minecraft.getInstance().isPaused())
        {
            rotSpeedRadians = Mth.lerp(0.1f, rotSpeedRadians, blockEntity.progressIncrement() * 0.1f);
            rotation += rotSpeedRadians;
        }

        Direction.Axis axis = blockEntity.getBlockState().getValue(DisruptorSegmentBlock.AXIS);

        centre.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotate(getRotation(axis))
                .rotate(rotation, Direction.UP.step())
                .uncenter()
                ;

        float extension = -(1 - 5 / 16f) * Mth.clamp(blockEntity.getDisruption() / 20f, 0f, 1f);
        if (lerpExtension == -1)
            lerpExtension = extension;
        else
            lerpExtension = Mth.lerp(0.1f, lerpExtension, extension);

        for (int i = 0; i < arms.length; ++i)
        {
            arms[i].setIdentityTransform().
                    translate(getVisualPosition())
                    .center()
                    .rotate(getRotation(axis))
                    .rotate(i / 4f * Mth.TWO_PI + rotation, Direction.UP.step())
                    .uncenter()
                    .translate(0, 0, lerpExtension);
            ;

            arms[i].setChanged();
        }

        centre.setChanged();
    }

    private Quaternionf getRotation(Direction.Axis axis)
    {
        return switch (axis)
        {
            case X -> Axis.ZN.rotationDegrees(90);
            case Y -> new Quaternionf();
            case Z -> Axis.XN.rotationDegrees(90);
        };
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, arms);
        relight(pos, centre);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
