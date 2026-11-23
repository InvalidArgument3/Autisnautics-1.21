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

package com.neep.neepmeat.entity.follower;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FollowerVisual extends AbstractEntityVisual<FollowerEntity> implements TickableVisual
{
//    private final List<ModelData> bits = new ArrayList<>();

    public FollowerVisual(VisualizationContext ctx, FollowerEntity entity, float tickDelta)
    {
        super(ctx, entity, tickDelta);
    }

    @Override
    public void _delete()
    {

    }

    public void tick()
    {

    }

    @Override
    public Plan<Context> planTick()
    {
        return RunnablePlan.of(this::tick);
    }
}
