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

import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.block.LargeFanBlock;
import com.neep.neepmeat.block.entity.LargeFanBlockEntity;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class LargeFanVisual extends AbstractBlockEntityVisual<LargeFanBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance vanes;

    public LargeFanVisual(VisualizationContext ctx, LargeFanBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.vanes =ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LARGE_FAN_VANES)).createInstance();
        vanes.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        vanes.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        Direction.Axis axis = blockEntity.getBlockState().getValue(LargeFanBlock.AXIS);

        float renderTime = AnimationTicks.getRenderTime() / 20;
        float rotation = Mth.wrapDegrees(blockEntity.rotationOffset + renderTime);

        vanes.setIdentityTransform()
                .translate(getVisualPosition())
                .center();
        rotateAxis(vanes, axis);
        vanes.rotateY(rotation);
        vanes.uncenter();

        vanes.setChanged();
    }

    private void rotateAxis(TransformedInstance model, Direction.Axis axis)
    {
        switch (axis)
        {
            case X -> model.rotate(Mth.HALF_PI, Direction.SOUTH.step());
            case Z -> model.rotate(Mth.HALF_PI, Direction.EAST.step());
            case Y -> {}
        }
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, vanes);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
