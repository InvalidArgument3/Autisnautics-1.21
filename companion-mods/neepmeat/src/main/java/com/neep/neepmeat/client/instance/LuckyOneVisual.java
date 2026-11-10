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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.live_machine.block.LuckyOneBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.LuckyOneBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class LuckyOneVisual extends AbstractBlockEntityVisual<LuckyOneBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance head;
    private final TransformedInstance arm_l;
    private final TransformedInstance arm_r;
    private final TransformedInstance body;

    private final PoseStack matrixStack = new PoseStack();

    public LuckyOneVisual(VisualizationContext ctx, LuckyOneBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        head = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LUCKY_ONE_HEAD)).createInstance();
        arm_l = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LUCKY_ONE_ARM_L)).createInstance();
        arm_r = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LUCKY_ONE_ARM_R)).createInstance();
        body = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.LUCKY_ONE_BODY)).createInstance();

        matrixStack.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        head.setTransform(matrixStack).setChanged();
        arm_l.setTransform(matrixStack).setChanged();
        arm_r.setTransform(matrixStack).setChanged();
        body.setTransform(matrixStack).setChanged();
    }

    @Override
    public void _delete()
    {
        head.delete();
        arm_l.delete();
        arm_r.delete();
        body.delete();
    }

    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        int offset = blockEntity.animationOffset;
//        float time = AnimationTickHolder.getRenderTime();

        // Hmmmm
        float time = AnimationTicks.getRenderTime();

        float headYaw = 0;
        float headPitch = 4;
        float bodyPitch = 0;

        if (blockEntity.isActive())
        {
            headYaw = 2 * Mth.sin((Mth.sin(time + offset) + 1) * time / (10));
            headPitch = 8 * Mth.cos(time / 4) * Mth.sin((time - offset) / 10 + 2);

            bodyPitch = Mth.clamp(
                    5 * (Mth.cos((time + offset / 2f) / 4 + 2)
                            + Mth.sin(time / 10 + 1) * Mth.sin(time * (offset / 100f - 0.5f)))
                            + Mth.cos(time),
                    0, 20);
        }

        BlockState state = blockEntity.getBlockState();

        matrixStack.pushPose();
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(LuckyOneBlock.FACING).toYRot()));
        matrixStack.translate(0, 0, 2 / 16f);
        matrixStack.translate(0, 0, -0.5);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-8 - bodyPitch));

        matrixStack.translate(-0.5, 0, 0);

        matrixStack.translate(0, -1.5, 0);
        body.setTransform(matrixStack);

        matrixStack.pushPose();
        matrixStack.translate(0.5, 1.5 + (12 / 16f), 0);
        matrixStack.mulPose(Axis.XN.rotationDegrees(headPitch));
        matrixStack.mulPose(Axis.YN.rotationDegrees(headYaw));
        matrixStack.translate(-0.5, -(1.5 + (12 / 16f)), 0);
        head.setTransform(matrixStack);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(0.5 - (4 / 16f), 1.5 + (12 / 16f), 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(3));
        matrixStack.mulPose(Axis.XP.rotationDegrees(-6 - bodyPitch));
        matrixStack.translate(-(0.5 - 4 / 16f), -(1.5 + (12 / 16f)), 0);
        arm_l.setTransform(matrixStack);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(0.5 + (4 / 16f), 1.5 + (12 / 16f), 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-3));
        matrixStack.mulPose(Axis.XP.rotationDegrees(-6 - bodyPitch));
        matrixStack.translate(-(0.5 + 4 / 16f), -(1.5 + (12 / 16f)), 0);
        arm_r.setTransform(matrixStack);
        matrixStack.popPose();

        matrixStack.popPose();

        head.setChanged();
        arm_l.setChanged();
        arm_r.setChanged();
        body.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, head, arm_l, arm_r, body);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }
}
