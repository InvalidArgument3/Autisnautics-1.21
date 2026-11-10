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

package com.neep.neepbus.client.instance;

import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepbus.block.SlantedSliderBlock;
import com.neep.neepbus.block.SliderBlock;
import com.neep.neepbus.block.entity.SliderBlockEntity;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class LinearLeverVisual extends AbstractBlockEntityVisual<SliderBlockEntity> implements SimpleDynamicVisual
{
    private float lerpOffset = 0;

    private final TransformedInstance handle;

    public LinearLeverVisual(VisualizationContext ctx, SliderBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        handle = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LINEAR_LEVER_HANDLE)).createInstance();
        handle.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        handle.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        float f = 10 / 16f * clamp(
                (float) ((float)
                                (blockEntity.getValue() - blockEntity.getMinValue()) / (blockEntity.getMaxValue() - blockEntity.getMinValue())),
                0f, 1f);
        lerpOffset = Mth.lerp(0.4f, lerpOffset, f);

        if (blockState.getBlock() instanceof SliderBlock)
        {
            Quaternionf facing = rotateThing(blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACING), blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE));
            handle.setIdentityTransform()
                    .translate(getVisualPosition())
                    .center()
                    .rotate(facing)
                    .translate(0, 0, lerpOffset)
                    .uncenter()
            ;
        }
        else if (blockState.getBlock() instanceof SlantedSliderBlock)
        {
            Quaternionf facing = rotateSlanted(blockState.getValue(SlantedSliderBlock.FACING), blockState.getValue(SlantedSliderBlock.FACE));
            handle.setIdentityTransform()
                    .translate(getVisualPosition())
                    .center()
                    .rotate(facing)
                    .uncenter()
                    .translate(0, 1 / 16f, 0)
                    .rotateX(-Mth.PI / 4)
                    .translate(0, 0, 4 / 16f)
                    .translate(0, 0, lerpOffset)
            ;
        }

        handle.setChanged();
    }


    @Override
    public void updateLight(float v)
    {
        relight(pos, handle);
    }

    // What's easier? Work out how to use the same rotation values as in the blockstate file?
    // Or spend an hour trying to get them right through trial and error?
    // Yay! Trial and error again!
    private Quaternionf rotateThing(Direction horFacing, AttachFace face)
    {
        if (face == AttachFace.WALL)
        {
            return switch (horFacing)
            {
                case NORTH -> euler(-90, 0);
                case SOUTH -> euler(-90, 180);
                case EAST -> euler(-90, -90);
                default -> euler(-90, 90);
            };
        }
        else if (face == AttachFace.FLOOR)
        {
            return switch (horFacing)
            {
                case NORTH -> Axis.YP.rotationDegrees(180);
                case SOUTH -> new Quaternionf();
                case EAST -> euler(0, 90);
                default -> euler(0, -90);
            };
        }
        else
        {
            return switch (horFacing)
            {
                case NORTH -> euler(180, 0);
                case SOUTH -> euler(180, 180);
                case EAST -> euler(180, -90);
                default -> euler(180, 90);
            };
        }
    }

    private Quaternionf rotateSlanted(Direction horFacing, SlantedSliderBlock.Attachment face)
    {
        if (face == SlantedSliderBlock.Attachment.FLOOR)
        {
            return switch (horFacing)
            {
                case NORTH -> Axis.YP.rotationDegrees(180);
                case SOUTH -> new Quaternionf();
                case EAST -> euler(0, 90);
                default -> euler(0, -90);
            };
        }
        else
        {
            return switch (horFacing)
            {
                case NORTH -> euler(180, 0);
                case SOUTH -> euler(180, 180);
                case EAST -> euler(180, -90);
                default -> euler(180, 90);
            };
        }
    }

    // Handles NaNs, unlike MathHelper
    public static float clamp(float value, float min, float max)
    {
        if (Float.isNaN(value))
            return min; // We can assume that 0 is safe in this context.

        return value < min ? min : Math.min(value, max);
    }

    private static Quaternionf euler(float xDeg, float yDeg)
    {
//        return new Quaternionf().rotateXYZ(xDeg * MathHelper.RADIANS_PER_DEGREE, yDeg * MathHelper.RADIANS_PER_DEGREE, 0);
        return new Quaternionf().rotateZYX(0, yDeg * Mth.DEG_TO_RAD, xDeg * Mth.DEG_TO_RAD);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
