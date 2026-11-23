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
import com.neep.neepmeat.machine.live_machine.block.BallMillBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
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
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BallMillVisual extends AbstractBlockEntityVisual<BallMillBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance drum;
    private final TransformedInstance drumEnd;

    public BallMillVisual(VisualizationContext ctx, BallMillBlockEntity blockEntity, float delta)
    {
        super(ctx, blockEntity, delta);
        drum = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.BALL_MILL_DRUM, true))
                .createInstance()
                .translate(getVisualPosition());
        drumEnd = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.BALL_MILL_DRUM_END, true))
                .createInstance()
                .translate(getVisualPosition());

        drum.setChanged();
        drumEnd.setChanged();
    }

    @Override
    public void _delete()
    {
        drum.delete();
        drumEnd.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx)
    {
        if (!Minecraft.getInstance().isPaused())
        {
            blockEntity.rotSpeedRadians = Mth.lerp(0.1f, blockEntity.rotSpeedRadians, blockEntity.progressIncrement() * 0.05f);
            blockEntity.angle += blockEntity.rotSpeedRadians;
        }

        Direction.Axis axis = blockEntity.getBlockState().getValue(BallMillBlock.AXIS);

        BallMillBlock.AxisConnection connection = blockState.getValue(BallMillBlock.CONNECTION);

        if (connection == BallMillBlock.AxisConnection.PLUS_MINUS || connection == BallMillBlock.AxisConnection.NONE)
        {
            drum.setIdentityTransform()
                    .translate(getVisualPosition())
                    .center()
                    .rotate(getRotation(axis))
                    .rotate(blockEntity.angle, Direction.EAST)
                    .uncenter()
            ;

            drumEnd.setVisible(false);
            drum.setVisible(true);
            drum.setChanged();
        }
        else
        {
            drum.setVisible(false);
            drumEnd.setVisible(true);

            drumEnd.setIdentityTransform()
                    .translate(getVisualPosition())
                    .center()
                    .rotate(getRotation(axis))
                    .rotate(connection == BallMillBlock.AxisConnection.MINUS ? 0 : Mth.PI, Direction.UP)
                    .rotate(connection == BallMillBlock.AxisConnection.MINUS ? blockEntity.angle : -blockEntity.angle, Direction.EAST)
                    .uncenter()
            ;

            drumEnd.setChanged();
        }
    }

    @Override
    public void updateLight(float delta)
    {
        relight(pos, drum, drumEnd);
    }

    private Quaternionf getRotation(Direction.Axis axis)
    {
        return switch (axis)
        {
            case X -> new Quaternionf();
            case Y -> new Quaternionf();
            case Z -> Axis.YN.rotationDegrees(90);
        };
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {
        consumer.accept(drum);
        consumer.accept(drumEnd);
    }
}
