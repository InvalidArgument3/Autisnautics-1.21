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

package com.neep.neepmeat.machine.charnel_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class CharnelPumpVisual extends AbstractBlockEntityVisual<CharnelPumpBlockEntity> implements SimpleDynamicVisual
{
    private final PoseStack matrices = new PoseStack();
    private final TransformedInstance plunger;

    public CharnelPumpVisual(VisualizationContext ctx, CharnelPumpBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        plunger = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.CHARNEL_PUMP_PLUNGER)).createInstance();
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        plunger.setTransform(matrices).setChanged();
    }

    @Override
    public void _delete()
    {
        plunger.delete();
    }

    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        matrices.pushPose();

//        float s = (NMMaths.sin(blockEntity.getWorld().getTime(), AnimationTickHolder.getPartialTicks(), 0.1f) + 1) / 2;
        float t = blockEntity.animationTicks > 0 ? 100 - blockEntity.animationTicks + context.partialTick() * blockEntity.progressIncrement() : 0;
        float s = CharnelPumpRenderer.plungerAnimation(blockEntity, t);

        BERenderUtils.rotateFacing(blockEntity.getBlockState().getValue(CharnelPumpBlock.FACING), matrices);
        matrices.translate(0, 3 + 3 * s, 0);

        plunger.setTransform(matrices);
        plunger.setChanged();
        matrices.popPose();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos.above(1), plunger);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
