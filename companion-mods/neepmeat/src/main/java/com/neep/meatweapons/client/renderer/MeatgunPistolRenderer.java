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

package com.neep.meatweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunItemRenderer;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.mixin.client.HeldItemRendererAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class MeatgunPistolRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, MeatgunItemRenderer
{
    private final Minecraft client;

    public MeatgunPistolRenderer()
    {
        this.client = Minecraft.getInstance();
    }

    @Override
    public float modelScale()
    {
        return 1;
    }

    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        // Remove the transformations applied by ItemRenderer
        matrices.popPose();
        matrices.pushPose();

        AbstractClientPlayer player = client.player;
        PlayerRenderer playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(player);

        boolean mainHand = ((HeldItemRendererAccessor) client.gameRenderer.itemInHandRenderer).getMainHand().equals(stack);
        boolean leftHanded = mode.firstPerson()
                && (player.getMainArm() == HumanoidArm.LEFT && mainHand || player.getMainArm() == HumanoidArm.RIGHT && !mainHand);

        // Apply the display transformations
        ItemTransform transformation = client.getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms().getTransform(mode);
        transformation.apply(leftHanded, matrices);
        matrices.translate(-0.5F, -0.5F, -0.5F);

        renderInner(stack, player, playerEntityRenderer, mode, matrices, vcp, true, mainHand, leftHanded, client.getFrameTime(), light, overlay);
    }

    // It's easier to override this one
    protected void renderInner(ItemStack stack, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, boolean showArm, boolean mainHand, boolean leftHanded, float tickDelta, int light, int overlay)
    {
        MeatgunComponent component = MWComponents.MEATGUN.get(stack);

        // Apply animations
        showArm = applyAnimations(matrices, mode, component, tickDelta, leftHanded);

        // Step recoil
        RecoilManager recoil = component.getRecoil();
        if (mode.firstPerson())
        {
            recoil.step(client);

            transformRecoil(matrices, recoil, leftHanded);
            recoil.shakeTranslate(matrices);

            applyAds(matrices, player, mainHand, leftHanded, tickDelta);
        }

        Matrix4f firstPersonModelTransform = getFirstPersonModelTransform();

        matrices.pushPose();
        if (mode.firstPerson())
            matrices.mulPoseMatrix(firstPersonModelTransform);

        // Recursive module rendering
        MeatgunModule root = component.getRootHolder().root;
        renderRecursive(matrices, root, stack, component, mode, vcp,
                Minecraft.getInstance().level.getGameTime(),
                Minecraft.getInstance().getFrameTime(), light, overlay);

        renderScreen(client, matrices, mode, vcp, component.getRootHolder(), tickDelta, leftHanded);

        matrices.popPose();

        renderParticles(matrices, mode, firstPersonModelTransform, vcp, light, overlay, mainHand, tickDelta);

        if (showArm)
            renderArms(matrices, mode, player, playerEntityRenderer, vcp, light, mainHand, leftHanded);
    }

    public static boolean applyAnimations(PoseStack matrices, ItemDisplayContext mode, MeatgunComponent component, float tickDelta, boolean leftHanded)
    {
        if (mode.firstPerson())
        {
            var animationManager = component.getAnimationManager();
            if (animationManager != null)
            {
                MeatgunAnimation animation = animationManager.getActive();
                return animation.applyRender(matrices, tickDelta, leftHanded);
            }
        }
        return true;
    }

    protected void renderArms(PoseStack matrices, ItemDisplayContext mode, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, MultiBufferSource vcp, int light, boolean mainHand, boolean leftHanded)
    {
        matrices.pushPose();

        float armScale = 1.2f;

        float cx = -4 / 16f;
        float cy = 4 / 16f;
        float cz = (2 - 10) / 16f;

        Matrix4f t = new Matrix4f()
                .translate(cx, cy, cz)
                .scale(armScale, armScale, armScale)
                .translate(-cx, -cy, -cz)
                .translate((leftHanded ? 2 : 12) / 16f / armScale, -4 / 16f / armScale, 20 / 16f / armScale)
                ;
        matrices.mulPoseMatrix(t);

        if (mode.firstPerson())
//                && player.getStackInHand(otherHand).isEmpty())
//                && hand == Hand.MAIN_HAND) // Prevent three arms when the main hand is empty
        {
            if (leftHanded)
            {
                renderArm(playerEntityRenderer.getModel().leftArm, true, matrices, player, vcp, light);
                renderArm(playerEntityRenderer.getModel().leftSleeve, true, matrices, player, vcp, light);
            }
            else
            {
                renderArm(playerEntityRenderer.getModel().rightArm, false, matrices, player, vcp, light);
                renderArm(playerEntityRenderer.getModel().rightSleeve, false, matrices, player, vcp, light);
            }
        }
        matrices.popPose();
    }

    private void renderArm(ModelPart armPart, boolean leftHanded, PoseStack matrices, AbstractClientPlayer player, MultiBufferSource vcp, int light)
    {
        armPart.xRot = (float) Math.toRadians(-90f);
        armPart.yRot = 0;
        armPart.zRot = (float) Math.PI;
        armPart.render(matrices, vcp.getBuffer(RenderType.entityCutout(player.getSkinTextureLocation())),
                light, OverlayTexture.NO_OVERLAY);
    }
}
