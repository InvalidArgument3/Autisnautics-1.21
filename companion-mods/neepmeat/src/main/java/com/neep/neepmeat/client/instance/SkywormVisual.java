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

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.meatlib.client.flw.RelightingEntityVisual;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.entity.skyworm.SkywormEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class SkywormVisual extends RelightingEntityVisual<SkywormEntity> implements SimpleDynamicVisual
{
    private final ObjectArrayList<TransformedInstance> body;

    private final FloatList scales = new FloatArrayList();

    public SkywormVisual(VisualizationContext ctx, SkywormEntity entity, float partialTick)
    {
        super(ctx, entity, partialTick);

        body = new ObjectArrayList<>();
        for (int i = 0; i < entity.getLength(); i++)
        {
            body.add(ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.SKYWORM_SEGMENT)).createInstance());
            scales.add(1);
        }
    }

    @Override
    public void beginFrame(Context context)
    {
        int segmentHeight = 5;
        float tickDelta = context.partialTick();
        float twistOffset = 0.4f;

        float wobbleMagnitude = entity.allowWobble() ? 0.3f : 0.1f;
        float wobbleX = Mth.sin((entity.tickCount + tickDelta) / 10) * wobbleMagnitude;
        float wobbleZ = Mth.cos((entity.tickCount + tickDelta) / 15) * wobbleMagnitude;

        boolean below = entity.getFromBelow();
        int m = below ? -1 : 1;

        int bulgeIdx = entity.getBulge();

        for (int i = 0; i < body.size(); i++)
        {
            float scale = scales.getFloat(i);
            if (i == bulgeIdx)
                scale = 1.2f;

            scale = Mth.lerp(0.1f, scale, 1f);
            scales.set(i, scale);

//            float angle = i * MathHelper.PI / 4;
            float angle = 0;
            float twist = getVisualPosition(tickDelta).y / 10f + i * Mth.PI / 5;
            var segment = body.get(i);
            segment
                    .setIdentityTransform()
                    .translate(getVisualPosition(tickDelta)
                            .add(0, m * i * segmentHeight - (below ? segmentHeight - 1 : 0), 0)
                    )
                    .translate(wobbleX, 0, wobbleZ)
                    .scale(scale, scale, scale)

                    .translate(twistOffset, 0, 0)
                    .rotate(twist, Direction.Axis.Y)
                    .translate(-twistOffset, 0, 0)

                    .rotate(angle, Direction.Axis.Y)
                    .translate(-0.5,  0, -0.5)
            ;

            relight(tickDelta, segment);

            segment.setChanged();
        }
    }

    @Override
    protected void _delete()
    {
        for (var thing : body)
        {
            thing.delete();
        }
    }

    @Override
    public void updateLight(float v)
    {
        for (TransformedInstance transformedInstance : body)
        {
            relight(v, transformedInstance);
        }
    }
}
