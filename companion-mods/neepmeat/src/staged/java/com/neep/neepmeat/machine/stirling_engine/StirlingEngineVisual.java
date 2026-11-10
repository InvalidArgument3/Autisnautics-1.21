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

package com.neep.neepmeat.machine.stirling_engine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(value = Dist.CLIENT)
public class StirlingEngineVisual extends AbstractBlockEntityVisual<StirlingEngineBlockEntity> implements SimpleDynamicVisual
{
    private final PoseStack matrices = new PoseStack();
    private final TransformedInstance rotor;

    public StirlingEngineVisual(VisualizationContext ctx, StirlingEngineBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.STIRLING_ENGINE_ROTOR))
                .createInstance()
                .setTransform(matrices);
        rotor.setChanged();

    }

    @Override
    public void _delete()
    {
        rotor.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        Direction facing = blockEntity.getBlockState().getValue(StirlingEngineBlock.FACING);
        matrices.pushPose();
        BERenderUtils.rotateFacing(facing, matrices);

        matrices.translate(0.5, 0.5, 0.5);

        float delta = Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaFrameTime();
        blockEntity.angle = Mth.wrapDegrees(blockEntity.angle + blockEntity.getSpeed() * delta);
        matrices.mulPose(Axis.ZN.rotationDegrees(blockEntity.angle));
        matrices.translate(-0.5, -0.5, -0.5);

        rotor.setTransform(matrices);

        matrices.popPose();
        rotor.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, rotor);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
