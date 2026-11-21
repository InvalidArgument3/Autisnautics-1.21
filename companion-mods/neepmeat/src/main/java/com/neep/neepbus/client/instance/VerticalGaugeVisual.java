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

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepbus.block.VerticalGaugeBlock;
import com.neep.neepbus.block.entity.VerticalGaugeBlockEntity;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.Direction;

public class VerticalGaugeVisual extends AbstractBlockEntityVisual<VerticalGaugeBlockEntity> implements TickableVisual
{
    private final TransformedInstance indicator;
    private float prevValue;

    public VerticalGaugeVisual(VisualizationContext ctx, VerticalGaugeBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        indicator = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.VERTICAL_GAUGE_INDICATOR)).createInstance();
        indicator.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        indicator.delete();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, indicator);
    }

    public void tick()
    {
        float length = 12 / 16f;

        float f = LinearLeverVisual.clamp(
                (float) ((float) (blockEntity.getValue() - blockEntity.getMinValue()) / (blockEntity.getMaxValue() - blockEntity.getMinValue())),
                0, 1);

        Direction facing = blockState.getValue(VerticalGaugeBlock.FACING);
        indicator
                .setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateToFace(facing)
                .translate(0, -length / 2, 0)
                .scale(1, f, 1)
                .translate(0, length / 2, 0)
                .uncenter()
                ;

        indicator.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

    @Override
    public Plan<Context> planTick()
    {
        return RunnablePlan.of(this::tick);
    }
}
