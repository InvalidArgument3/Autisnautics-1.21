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

package com.neep.neepmeat.machine.large_crusher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.meatlib.client.visual.DynamicBlockEntityVisual;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import com.neep.neepmeat.util.NMMaths;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CrusherSegmentVisual extends DynamicBlockEntityVisual<CrusherSegmentBlockEntity>
{
    private final TransformedInstance sheathModel;
    private final PoseStack matrixStack;

    public CrusherSegmentVisual(VisualizationContext ctx, CrusherSegmentBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.sheathModel = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.CRUSHER_SEGMENT_SHEATH, true)).createInstance();

        matrixStack = new PoseStack();
        matrixStack.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        sheathModel.setTransform(matrixStack);
        sheathModel.setChanged();
    }

    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        float tickDelta = context.partialTick();
        float sinTime2 = NMMaths.sin(blockEntity.getLevel().getGameTime(), tickDelta, 2);

        matrixStack.pushPose();
        if (blockEntity.progressIncrement() > blockEntity.minIncrement())
        {
            matrixStack.translate(0, sinTime2 * 0.03, 0);
        }

        sheathModel.setTransform(matrixStack);
        matrixStack.popPose();

        sheathModel.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, sheathModel);
    }

    @Override
    public void _delete()
    {
        sheathModel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
