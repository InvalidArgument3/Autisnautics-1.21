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

package com.neep.meatweapons.client.renderer;

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.meatlib.client.flw.RelightingEntityVisual;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.entity.BounceGrenadeEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;

public class BounceGrenadeVisual extends RelightingEntityVisual<BounceGrenadeEntity>
{
    private final TransformedInstance model;

    private float renderTime = 0;

    public BounceGrenadeVisual(VisualizationContext ctx, BounceGrenadeEntity entity, float partialTick)
    {
        super(ctx, entity, partialTick);
        this.model =ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(MWExtraModels.BOUNCE_GRENADE)).createInstance();
        model.translate(getVisualPosition());
    }

    @Override
    public void _delete()
    {
        model.delete();
    }

    @Override
    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        float tickDelta = context.partialTick();

        if (!entity.isInGround())
            renderTime += Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        model.setIdentityTransform()
                .translate(getVisualPosition(tickDelta))
                .rotate(renderTime / 4, Direction.UP.step())
                .rotate(renderTime / 5, Direction.NORTH.step())
        ;

        model.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(v, model);
    }
}
