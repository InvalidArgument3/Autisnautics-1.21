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

package com.neep.neepmeat.machine.flywheel;

import com.neep.meatlib.client.flw.MeatModels;
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

// Wow! It's a flywheel rendered with Flywheel. It's flywheels all the way down!
public class FlywheelVisual extends AbstractBlockEntityVisual<FlywheelBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance rotor;

    public FlywheelVisual(VisualizationContext ctx, FlywheelBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.FLYWHEEL)).createInstance();
        rotor.setIdentityTransform().translate(getVisualPosition());
    }

    @Override
    public void _delete()
    {
        rotor.delete();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, rotor);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
