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

package com.neep.neepmeat.machine.phage_ray;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

@OnlyIn(Dist.CLIENT)
public class PhageRayVisual extends AbstractEntityVisual<PhageRayEntity> implements SimpleDynamicVisual, LightUpdatedVisual
{
    private final TransformedInstance base;
    private final TransformedInstance barrel;
    private final PoseStack matrices = new PoseStack();

    public PhageRayVisual(VisualizationContext ctx, PhageRayEntity entity, float partialTick)
    {
        super(ctx, entity, partialTick);
        this.base = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.PHAGE_RAY_BASE)).createInstance();
        this.barrel = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.PHAGE_RAY_BARREL)).createInstance();
        barrel.translate(getVisualPosition()).setChanged();
        base.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        base.delete();;
        barrel.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        boolean bottom = entity.isBottom();

        matrices.pushPose();
        matrices.translate(getVisualPosition().x, getVisualPosition().y - 0.5, getVisualPosition().z);

        float tickDelta = Minecraft.getInstance().getFrameTime();;
        float pitch = entity.getViewXRot(tickDelta);
//        float yaw = entity.getYaw(tickDelta);
        float yaw = Mth.rotLerp(tickDelta, entity.yRotO, entity.getYRot());

        matrices.mulPose(Axis.YN.rotationDegrees(yaw));
        matrices.translate(-0.5, 0, -0.5);

        if (bottom)
            matrices.translate(0, -2 / 16f, 0);

        matrices.pushPose();
        if (bottom)
        {
            matrices.translate(0.5, 0, 0.5);
            matrices.mulPose(Axis.XP.rotationDegrees(180));
            matrices.translate(-0.5, -4, -0.5);
        }
        base.setTransform(matrices);
        matrices.popPose();

        matrices.translate(0, 2, 0.5);
        matrices.mulPose(Axis.XP.rotationDegrees(pitch));
        matrices.translate(0, -2, -0.5);

        barrel.setTransform(matrices);

        matrices.popPose();

        base.setChanged();
        barrel.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(0, base, barrel);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector)
    {

    }

    //    @Override
//    public void updateLight(float v)
//    {
//        relight(entity.getBlockPos().up(), base);
//        relight(pos.up(), barrel);
//    }

   
}
