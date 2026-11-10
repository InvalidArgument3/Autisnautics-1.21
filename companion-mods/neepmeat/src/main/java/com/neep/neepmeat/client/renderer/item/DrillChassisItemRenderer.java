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

import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.client.renderer.MeatgunPistolRenderer;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunItemRenderer;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunModuleRenderer;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.mixin.client.HeldItemRendererAccessor;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.item.DrillChassisItem;
import com.neep.neepmeat.meatgun.module.MiningMeatgunModule;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import static com.neep.neepmeat.client.item.ItemRenderUtil.getEquipProgress;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class DrillChassisItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, MeatgunItemRenderer
{
    private final Minecraft client;

    public DrillChassisItemRenderer(Minecraft client)
    {
        this.client = client;
    }

    public static void renderItem(
            ItemStack stack,
            ItemDisplayContext renderMode,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay,
            BakedModel model,
            boolean flat,
            boolean raise // I accidentally made a lot of my OBJ models centered on the correct Y coordinate but not X and Z.
    )
    {
        if (!stack.isEmpty())
        {
            matrices.pushPose();

            // OBJ models have
//            if (model instanceof MyronBakedModel)
//            {
//                matrices.translate(0.5, raise ? 0.5f : 0, 0.5);
//            }

            boolean bl2 = false;

            RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, bl2);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

            MeatgunModuleRenderer.renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer, flat);

            matrices.popPose();
        }
    }

    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        float tickDelta = client.getFrameTime();

        MeatgunComponent component = MWComponents.MEATGUN.get(stack);

        boolean canRun = false;
        var head = ((DrillChassisItem) stack.getItem()).getHead(stack);
        if (head instanceof MiningMeatgunModule mining)
            canRun = mining.canRun();

        ResourceLocation modelId = canRun ? NMExtraModels.ROCK_DRILL_ON : NMExtraModels.ROCK_DRILL_OFF;
        BakedModel main = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(modelId);

        if (mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
        {
            // Remove all the other transformations including the equip animation and display settings.
            matrices.popPose();
            matrices.pushPose();

            boolean mainHand = ((HeldItemRendererAccessor) client.gameRenderer.itemInHandRenderer).getMainHand().equals(stack);
            boolean leftHanded = mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            ItemTransform transformation = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms().getTransform(mode);
            transformation.apply(leftHanded, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);

            matrices.translate(0, 0.5, 0);

            boolean using = canRun && DrillChassisItem.Client.using(stack, mode.firstPerson());

            // Vertical shake
            float timeDegrees = client.level.getGameTime() % 360 + tickDelta;
            float shake = (float) (0.01f * Math.sin(Math.toRadians(timeDegrees) * 140));
            if (!using)
                shake = 0;

            HumanoidArm arm = mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? HumanoidArm.LEFT : HumanoidArm.RIGHT;

            float equipProgress = 0;
            if (!client.player.swinging)
                equipProgress = getEquipProgress(client.gameRenderer.itemInHandRenderer, leftHanded && client.player.getMainArm() == HumanoidArm.RIGHT, tickDelta);
            applyEquipOffset(matrices, arm, equipProgress);

            matrices.translate(0, shake, 0);

            // Apply animations
            MeatgunPistolRenderer.applyAnimations(matrices, mode, component, tickDelta, leftHanded);

            // Step recoil
            RecoilManager recoil = component.getRecoil();
            recoil.step(client);
            transformRecoil(matrices, recoil, leftHanded);
            recoil.shakeTranslate(matrices);

            // Body
            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, false);

            // Recursive meatgun
            renderMeatgunParts(stack, client.player, mode, matrices, vcp, false, true, false, tickDelta, light, overlay);

            // Screen-space particles
            renderParticles(matrices, mode, new Matrix4f(), vcp, light, overlay, mainHand, tickDelta);

            matrices.pushPose();
            matrices.translate((leftHanded ? 2 : -2) / 16f, 0, -14 / 16f);
            renderScreen(client, matrices, mode, vcp, component.getRootHolder(), tickDelta, leftHanded);
            matrices.popPose();
        }
        else if (mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
        {
            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, false);
//                renderItem(stack, mode, matrices, vertexConsumers, light, overlay, rod, false, false);
            renderMeatgunParts(stack, client.player, mode, matrices, vcp, false, true, false, tickDelta, light, overlay);
        }
        else
        {
            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, false);
//                renderItem(stack, mode, matrices, vertexConsumers, light, overlay, rod, false, false);
            renderMeatgunParts(stack, client.player, mode, matrices, vcp, false, true, false, tickDelta, light, overlay);
        }
    }

    protected void renderMeatgunParts(ItemStack stack, AbstractClientPlayer player, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, boolean showArm, boolean mainHand, boolean leftHanded, float tickDelta, int light, int overlay)
    {
        matrices.pushPose();

        MeatgunComponent component = MWComponents.MEATGUN.get(stack);

//        // Recursive module rendering
        MeatgunModule root = component.getRootHolder().root;
        renderRecursive(matrices, root, stack, component, mode, vcp,
                Minecraft.getInstance().level.getGameTime(),
                Minecraft.getInstance().getFrameTime(), light, overlay);

        matrices.popPose();
    }

    private void applyEquipOffset(PoseStack matrices, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate(0, equipProgress * -0.6F, 0);
    }
}
