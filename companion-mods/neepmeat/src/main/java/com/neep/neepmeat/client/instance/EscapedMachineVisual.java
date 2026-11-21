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
import com.neep.neepmeat.entity.MachineWormEntity;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EscapedMachineVisual extends RelightingEntityVisual<MachineWormEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance head;

    private final List<TransformedInstance> body;

    public EscapedMachineVisual(VisualizationContext ctx, MachineWormEntity entity, float par)
    {
        super(ctx, entity, par);

        head = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.ESCAPED_MACHINE_HEAD))
                .createInstance()
                .translate(getVisualPosition())
        ;

        body = new ObjectArrayList<>();
        for (int i = 0; i < entity.getSegments().size(); ++i)
        {
            body.add(ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.ESCAPED_MACHINE_SEGMENT))
                    .createInstance()
                    .translate(getVisualPosition()));
            ;
        }
    }

    @Override
    public void _delete()
    {
        head.delete();
        body.forEach(TransformedInstance::delete);
    }

    public void beginFrame(Context context)
    {
        float tickDelta = context.partialTick();
        head.setIdentityTransform()
                .translate(getVisualPosition(tickDelta))
                .rotate(Mth.DEG_TO_RAD * (180 - entity.getViewYRot(tickDelta)), Direction.UP.step())
                .translate(-0.5, 0, -0.5)
        ;


        float offset = 1;
        List<MachineWormEntity.Segment> segments = entity.getSegments();
        for (int i = 0; i < body.size(); ++i)
        {
            var segment = segments.get(i);

            if (entity.tickCount == 0)
            {
                segment.xOld = entity.getX();
                segment.yOld = entity.getY();
                segment.zOld = entity.getZ();
            }

            var model = body.get(i);

            model.setIdentityTransform()
                    .translate(getVisualPosition(segment, tickDelta))
                    .rotate(Mth.DEG_TO_RAD * (180 - segment.getViewYRot(tickDelta)), Direction.UP.step())
                    .translate(0, 0, -0.5)
                    .scale(segment.scale)
                    .translate(-0.5, 0, 0)
            ;

            offset += segment.scale;

            model.setChanged();
        }

        head.setChanged();
    }

    public Vector3f getVisualPosition(Entity entity, float partialTicks)
    {
        Vec3 pos = entity.position();
        Vec3i origin = renderOrigin();
        return new Vector3f(
                (float) (Mth.lerp(partialTicks, entity.xOld, pos.x) - origin.getX()),
                (float) (Mth.lerp(partialTicks, entity.yOld, pos.y) - origin.getY()),
                (float) (Mth.lerp(partialTicks, entity.zOld, pos.z) - origin.getZ())
        );
    }

    @Override
    public void updateLight(float v)
    {
        relight(v, head);
        FlatLit.relight(this.computePackedLight(v), (Iterator<FlatLit>) (Iterator<?>) body.iterator());
    }

    @Override
    public Plan<Context> planFrame()
    {
        return RunnablePlan.of(this::planFrame);
    }
}
