package com.neep.meatlib.client.flw;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.world.entity.Entity;

public abstract class RelightingEntityVisual<T extends Entity> extends AbstractEntityVisual<T> implements LightUpdatedVisual, SimpleDynamicVisual
{
    public RelightingEntityVisual(VisualizationContext ctx, T entity, float partialTick)
    {
        super(ctx, entity, partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector)
    {

    }
}
