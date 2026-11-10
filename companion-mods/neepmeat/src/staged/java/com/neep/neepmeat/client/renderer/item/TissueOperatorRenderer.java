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

package com.neep.neepmeat.client.renderer.item;

import com.neep.meatlib.client.event.UseAttackCallback;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.util.Easing;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import static com.neep.neepmeat.client.renderer.item.DrillChassisItemRenderer.renderItem;

import com.mojang.blaze3d.vertex.PoseStack;

public class TissueOperatorRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer
{
    public static final TissueOperatorRenderer INSTANCE = new TissueOperatorRenderer(Minecraft.getInstance());

    private final Minecraft client;

    // Cheating
    // Animations will be shared by all players, but it's fine if they only display in first person.
    private float filteredSwing = 0;
    private float endRotSpeed = 0;
    private float endAngle = 0;

    private TissueOperatorRenderer(Minecraft client)
    {
        this.client = client;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        BakedModel main = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(NMExtraModels.TISSUE_OPERATOR_MAIN);
        BakedModel end = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(NMExtraModels.TISSUE_OPERATOR_FRONT);
        BakedModel wheel = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(NMExtraModels.TISSUE_OPERATOR_WHEEL);

        if (client.player.isUsingItem())
        {
            increaseSwing(0.3f);
        }
        else if (filteredSwing > 0)
        {
            increaseSwing(1);
        }

        if (filteredSwing == 1)
            filteredSwing = 0;

        if (mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
        {
            // Remove all the other transformations including the equip animation and display settings.
            matrices.popPose();
            matrices.pushPose();

            HumanoidArm arm = client.player.getMainHandItem() == stack == (client.player.getMainArm() == HumanoidArm.RIGHT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;

            ItemTransforms modelTransformation = client.getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms();
            ItemTransform transformation = modelTransformation.getTransform(mode);

            matrices.pushPose();
//            applyEquipOffset(matrices, arm, 0);
            transformation.apply(false, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            float towardsCentre = animateSwing(filteredSwing);
            matrices.translate(towardsCentre * i * -2 / 16f,
                    towardsCentre * (2 / 16f + 0.006 * Mth.sin(client.getFrameTimeNs() * 2)), towardsCentre * -2 / 16f);

            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main, false, false);

            if (!client.isPaused())
            {
                endRotSpeed = (float) Mth.lerp(0.2f, endRotSpeed, Easing.easeOutCubic(filteredSwing));
                endAngle += endRotSpeed * client.getDeltaFrameTime();
            }

            // Local transformations for the wheel
            matrices.pushPose();
            Matrix4f wheelLocal = new Matrix4f();
            wheelLocal.translate(0, 2 / 16f, 0);
            wheelLocal.rotateZ(endAngle);
            wheelLocal.translate(0, -2 / 16f, 0);
            matrices.translate(0.5, 0, 1);
            matrices.mulPoseMatrix(wheelLocal);
            matrices.translate(-0.5, 0, -1);
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, wheel, true, false);
            matrices.popPose();

            // Local transformations for the bit on the end
            float translation = animateOffset(filteredSwing);
            Matrix4f endLocal = new Matrix4f();
            endLocal.translate(0, 2 / 16f, 0);
            endLocal.rotateZ(endAngle);
            endLocal.translate(0, -2 / 16f, translation);
            matrices.translate(0.5, 0, 1);
            matrices.mulPoseMatrix(endLocal);
            matrices.translate(-0.5, 0, -1);

            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, end, true, false);
            matrices.popPose();
        }
//        else if (mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND)
//        {
//            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main);
//            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, end);
//            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, wheel);
//        }
        else
        {
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main, false, false);
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, end, false, false);
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, wheel, false, false);
        }
    }

    private float animateSwing(float x)
    {
        return (float) (x < 0.3f ? Easing.easeOutCubic(x / 0.3f) : Easing.easeInCubic((1 - x) / 0.7f));
//        return (float) (x < 0.3f ? (x / 0.3f) : ((1 - x) / 0.7f));
    }

    private float animateOffset(float x)
    {
        return -4 / 16f * (float) (x < 0.5f ? Easing.easeInOutBack(x * 2) : Easing.easeOutCubic(2 * (1 - x)));
    }

    private void increaseSwing(float max)
    {
        if (!client.isPaused())
            filteredSwing = Math.min(max, filteredSwing + 0.05f * client.getDeltaFrameTime());
    }

    public static boolean isHolding(ItemStack stack)
    {
        return stack.is(NMItems.TISSUE_OPERATOR);
    }

    static
    {
        UseAttackCallback.DO_USE.register((client1, player) ->
        {
            if (isHolding(player.getMainHandItem()) || isHolding(player.getOffhandItem()))
            {
                INSTANCE.filteredSwing = 0;
                INSTANCE.increaseSwing(1);
            }

            return true;
        });

        UseAttackCallback.DO_ATTACK.register((client1, player) ->
        {
            if (isHolding(player.getMainHandItem()) || isHolding(player.getOffhandItem()))
            {
                INSTANCE.increaseSwing(1);
            }

            return true;
        });
    }
}
