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

package com.neep.neepmeat.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.meatlib.client.flw.RelightingEntityVisual;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.entity.LimbEntity;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.List;

public class LimbEntityVisual extends RelightingEntityVisual<LimbEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance model;
    private final PoseStack matrices = new PoseStack();

    private static final List<PartialModel> LIMBS = Lists.newArrayList();

    static
    {
        LIMBS.add(NMExtraModels.COW_LIMB);
        LIMBS.add(NMExtraModels.COW_HEAD);
        LIMBS.add(NMExtraModels.PIG_HEAD);
        LIMBS.add(NMExtraModels.PIG_HEAD_1);
        LIMBS.add(NMExtraModels.UNKNOWN_LIMB_1);
        LIMBS.add(NMExtraModels.UNKNOWN_LIMB_2);
        LIMBS.add(NMExtraModels.UNKNOWN_BODY_1);
    }

    public LimbEntityVisual(VisualizationContext ctx, LimbEntity entity, float tickDelta)
    {
        super(ctx, entity, tickDelta);

        model = createModel();
    }

    private TransformedInstance createModel()
    {
        int id = (int) Math.round(Math.random() * (LIMBS.size() - 1));

        return visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(LIMBS.get(id)))
                .createInstance();
    }

//    private static ModelPart createModel()
//    {
//        return ModelPart.builder("bell", 64, 64)
//                .cuboid()
//                .start(6, 0.0F, 2.0F)
//                .size(4, 4, 14F)
//                .textureOffset(0, 0)
//                .endCuboid()
//                .build();
//    }

    @Override
    public void _delete()
    {
        model.delete();
    }

    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        matrices.pushPose();
        float tickDelta = context.partialTick();
        var pos = getVisualPosition(tickDelta);
        matrices.translate(pos.x, pos.y, pos.z);

        matrices.mulPose(Axis.YP.rotationDegrees(entity.getViewYRot(tickDelta)));
        matrices.translate(-0.5, 0, -0.5);

        model.setTransform(matrices);
        matrices.popPose();

        model.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(v, model);
    }


//    @Override
//    public void updateLight(float v)
//    {
//        relight(v, model);
//    }
}
