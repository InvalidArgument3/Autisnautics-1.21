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

package com.neep.neepmeat.machine.grinder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class CrusherVisual extends AbstractBlockEntityVisual<CrusherBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance jawModel;
    private final PoseStack matrixStack;

    public CrusherVisual(VisualizationContext ctx, CrusherBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);

        matrixStack = new PoseStack();
        matrixStack.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        jawModel = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.P_CRUSHER_JAW, true)).createInstance();

        jawModel.setIdentityTransform().translate(getVisualPosition());
        jawModel.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, jawModel);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        float tickDelta = Minecraft.getInstance().getFrameTime();
//        double sinTime = Math.sin(blockEntity.getWorld().getTime()) * Math.cos(tickDelta) + Math.cos(blockEntity.getWorld().getTime()) * Math.sin(tickDelta);

        matrixStack.pushPose();
        Direction facing = blockEntity.getBlockState().getValue(CrusherBlock.FACING);

        if (blockEntity.progressIncrement() > 0)
        {
            var unit = facing.step();
            double magnitude = 0.01 + 0.025 * Math.sin(blockEntity.sinAngle);
            matrixStack.translate(unit.x * magnitude, 0, unit.z * magnitude);
        }
        BERenderUtils.rotateFacing(facing, matrixStack);
        jawModel.setTransform(matrixStack);
        matrixStack.popPose();
        jawModel.setChanged();
    }

    @Override
    public void _delete()
    {
        jawModel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
