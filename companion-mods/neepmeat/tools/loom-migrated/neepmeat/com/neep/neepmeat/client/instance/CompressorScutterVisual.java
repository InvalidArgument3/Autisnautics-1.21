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
import com.neep.neepmeat.entity.compressor_scutter.CompressorScutterEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;

public class CompressorScutterVisual extends RelightingEntityVisual<CompressorScutterEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance body;

    public CompressorScutterVisual(VisualizationContext ctx, CompressorScutterEntity entity, float delta)
    {
        super(ctx, entity, delta);
        body = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.COMPRESSOR_SCUTTER)).createInstance();
        body.setIdentityTransform().translate(entity.position());
        body.setChanged();

        entity.xOld = entity.getX();
        entity.yOld = entity.getY();
        entity.zOld = entity.getZ();
    }

    @Override
    public void beginFrame(Context ctx)
    {
        float tickDelta = ctx.partialTick();

//        float speed = (float) entity.getVelocity().length();
//        float mag = MathHelper.clamp(speed, 0, entity.getMovementSpeed()) / entity.getMovementSpeed();
//        float offset = (float) (0.14 * MathHelper.sin((entity.age + tickDelta) / 2f) * mag);

        body
                .setIdentityTransform()
                .translate(getVisualPosition(tickDelta))
                .rotate((float) Math.toRadians(180 - entity.getViewYRot(tickDelta)), Direction.UP)
                .translate(-0.5,  0, -0.5)
        ;

        body.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(v, body);
    }

    @Override
    protected void _delete()
    {
        body.delete();
    }
}
