package com.neep.meatlib.client.visual;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.world.level.block.entity.BlockEntity;

// TODO: remove because of SimpleDynamicVisual
public abstract class DynamicBlockEntityVisual<T extends BlockEntity> extends AbstractBlockEntityVisual<T> implements DynamicVisual
{
    public DynamicBlockEntityVisual(VisualizationContext ctx, T blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
    }

    protected boolean shouldUpdate(DynamicVisual.Context context)
    {
        return !(doDistanceLimitThisFrame(context) || !isVisible(context.frustum()));
    }

    protected void beginFrame(DynamicVisual.Context context)
    {

    }

    @Override
    public Plan<Context> planFrame()
    {
        return RunnablePlan.of((c) ->
        {
            if (shouldUpdate(c))
                beginFrame(c);
        });
    }
}
