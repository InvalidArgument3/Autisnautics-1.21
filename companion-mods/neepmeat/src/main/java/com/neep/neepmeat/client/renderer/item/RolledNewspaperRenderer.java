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
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.neep.meatlib.client.RotationAxis;
import com.neep.meatlib.client.event.UseAttackCallback;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.item.RolledNewspaperItem;
import com.neep.neepmeat.util.Easing;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import static com.neep.neepmeat.client.renderer.item.DrillChassisItemRenderer.renderItem;

import com.mojang.blaze3d.vertex.PoseStack;

public class RolledNewspaperRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer
{
    public static final RolledNewspaperRenderer INSTANCE = new RolledNewspaperRenderer(Minecraft.getInstance());

    private final Minecraft client;

    private float swing;

    private RolledNewspaperRenderer(Minecraft client)
    {
        this.client = client;
    }

    private float ease(float x)
    {
        if (x <= 0.5)
        {
            return (float) Easing.easeInCubic(x * 2);
        }
        else
        {
            return (float) Easing.easeInCubic( (1 - (x - 0.5) * 2));
        }
    }

    private void swing()
    {
        swing = 1;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        BakedModel main = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(NMExtraModels.ROLLED_NEWSPAPER));

        swing = Math.max(0, swing - 0.1f * client.getTimer().getGameTimeDeltaPartialTick(true));

        if (mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
        {
            matrices.popPose();
            matrices.pushPose();

            HumanoidArm arm = client.player.getMainHandItem() == stack == (client.player.getMainArm() == HumanoidArm.RIGHT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;

            ItemTransforms modelTransformation = client.getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms();

            matrices.pushPose();
            modelTransformation.getTransform(mode).apply(arm == HumanoidArm.LEFT, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);

            if (swing > 0)
            {
//                float s = MathHelper.sin(AnimationTickHolder.getRenderTime());
                float time = 5.0f;
                float rotation = ease(1 - swing);
                Quaternionf quat = RotationAxis.NEGATIVE_X.rotation(rotation);
                matrices.translate(0, 0, 0.5);
                matrices.mulPose(quat);
                matrices.translate(0, 0, -0.5);
            }

            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main, false, false);

            matrices.popPose();
        }
        else if (mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
        {
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main, false, false);
        }
        else
        {
            renderItem(stack, mode, matrices, vertexConsumers, light, overlay, main, false, false);
        }
    }

    static
    {
        UseAttackCallback.DO_ATTACK.register((client1, player) ->
        {
            if (INSTANCE.swing == 0
                    && player.swinging
                    && (RolledNewspaperItem.isHolding(player.getMainHandItem())))
            {
                INSTANCE.swing();
            }

            return true;
        });
    }
}
