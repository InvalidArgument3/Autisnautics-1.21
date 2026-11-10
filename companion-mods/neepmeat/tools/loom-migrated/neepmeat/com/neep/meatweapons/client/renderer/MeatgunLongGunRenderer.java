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
import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunItemRenderer;
import com.neep.meatweapons.component.GunTickHandler;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.mixin.client.HeldItemRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class MeatgunLongGunRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, MeatgunItemRenderer
{
    private final Minecraft client;

    public MeatgunLongGunRenderer()
    {
        this.client = Minecraft.getInstance();
    }

    @Override
    public float modelScale()
    {
        return 1;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        // Remove the transformations applied by ItemRenderer
        matrices.popPose();

        AbstractClientPlayer player = client.player;
        PlayerRenderer playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(player);

        boolean mainHand = ((HeldItemRendererAccessor) client.gameRenderer.itemInHandRenderer).getMainHand().equals(stack);
        boolean leftHanded = mode.firstPerson()
                && (player.getMainArm() == HumanoidArm.LEFT && mainHand || player.getMainArm() == HumanoidArm.RIGHT && !mainHand);

//        // Apply the equip animation from scratch
//        if (mode.isFirstPerson())
//        {
//            matrices.pop();
//            matrices.push();
//            ItemRenderUtil.defaultItemEquipAnimation(matrices, client, leftHanded);
//        }

        matrices.pushPose();

        // Apply the display transformations
        ItemTransform transformation = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms().getTransform(mode);
        transformation.apply(leftHanded, matrices);
        matrices.translate(-0.5F, -0.5F, -0.5F);

        renderInner(stack, player, playerEntityRenderer, mode, matrices, vcp, true, mainHand, leftHanded, client.getFrameTime(), light, overlay);
    }

    private ItemStack getItemInOtherHand(ItemInHandRenderer renderer, boolean mainHand)
    {
        var accessor = (HeldItemRendererAccessor) renderer;
        if (mainHand)
            return accessor.getOffHand();
        else
            return accessor.getMainHand();
    }

    @Override
    public Matrix4f getFirstPersonModelTransform()
    {
        return MeatgunItemRenderer.super.getFirstPersonModelTransform().translate(0, 0.5f / 16f, 0);
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

            // ADS
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
        {
            float armScale = 1.2f;
            renderMainArm(matrices, mode, player, playerEntityRenderer, vcp, light, leftHanded, armScale);

            // Don't show the main hand when held in the offhand because the player can still hold an item in their main hand
            if (mainHand && getItemInOtherHand(client.gameRenderer.itemInHandRenderer, mainHand).isEmpty())
                renderOffArm(matrices, mode, player, playerEntityRenderer, vcp, light, leftHanded, armScale);
        }
    }

    @Override
    public void applyAds(PoseStack matrices, Player player, boolean mainHand, boolean leftHanded, float tickDelta)
    {
        var ads = GunTickHandler.get(player);
        Vector3f ads1 = new Vector3f(); // No ADS means no transform
        Vector3f adsTransform = new Vector3f((leftHanded ? -1 : 1) * -11.5f / 16f, 1 / 16f, 6 / 16f);

        // Add some wobble
        float t = Mth.TWO_PI * AnimationTicks.getRenderTime() / 20f;
        float f = 0.1f / 16f;
        adsTransform.add((float) (f * Math.sin(0.2f * t)), (float) (f * Math.cos(0.4f * t)), 0);
        ads1.lerp(adsTransform, ads.getAds().getAnimation(tickDelta, mainHand));

        matrices.translate(ads1.x, ads1.y, ads1.z);
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

    protected void renderOffArm(PoseStack matrices, ItemDisplayContext mode, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, MultiBufferSource vcp, int light, boolean leftHanded, float armScale)
    {
        matrices.pushPose();

        float f = leftHanded ? -1 : 1;

        float cx = -4 / 16f;
        float cy = 4 / 16f;
        float cz = (2 - 10) / 16f;

        // 12 2
        float ox = leftHanded ? 20 : -6;
        float oy = -7;
        float oz = 13;

        Matrix4f t = new Matrix4f()
                .translate(cx, cy, cz)
                .scale(armScale, armScale, armScale)
                .translate(-cx, -cy, -cz)
                .translate(ox / 16f / armScale, oy / 16f / armScale, oz / 16f / armScale)
                ;

        matrices.mulPoseMatrix(t);

        if (mode.firstPerson())
        {
            float pitch = (-20 + -90) * Mth.DEG_TO_RAD;
            float yaw = f * 35 * Mth.DEG_TO_RAD;
            float roll = (float) Math.PI;

            if (leftHanded)
            {
                renderArm(playerEntityRenderer.getModel().rightArm, pitch, yaw, roll, matrices, player, vcp, light);
                renderArm(playerEntityRenderer.getModel().rightSleeve, pitch, yaw, roll, matrices, player, vcp, light);
            }
            else
            {
                renderArm(playerEntityRenderer.getModel().leftArm, pitch, yaw, roll, matrices, player, vcp, light);
                renderArm(playerEntityRenderer.getModel().leftSleeve, pitch, yaw, roll, matrices, player, vcp, light);
            }
        }
        matrices.popPose();
    }

    protected void renderMainArm(PoseStack matrices, ItemDisplayContext mode, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, MultiBufferSource vcp, int light, boolean leftHanded, float armScale)
    {
        matrices.pushPose();

        float cx = -4 / 16f;
        float cy = 4 / 16f;
        float cz = (2 - 10) / 16f;

        float ox = leftHanded ? 2 : 13;
        float oy = -4;
        float oz = 20;

        Matrix4f t = new Matrix4f()
                .translate(cx, cy, cz)
                .scale(armScale, armScale, armScale)
                .translate(-cx, -cy, -cz)
                .translate(ox / 16f / armScale, oy / 16f / armScale, oz / 16f / armScale)
                ;

        matrices.mulPoseMatrix(t);

        if (mode.firstPerson())
        {
            float pitch = (float) Math.toRadians(-90f);
            float yaw = 0;
            float roll = (float) Math.PI;
            var model = playerEntityRenderer.getModel();

            if (leftHanded)
            {
                renderArm(model.leftArm, pitch, yaw, roll, matrices, player, vcp, light);
                renderArm(model.leftSleeve, pitch, yaw, roll, matrices, player, vcp, light);
            }
            else
            {
                renderArm(model.rightArm, pitch, yaw, roll, matrices, player, vcp, light);
                renderArm(model.rightSleeve, pitch, yaw, roll, matrices, player, vcp, light);
            }
        }
        matrices.popPose();
    }

    private void renderArm(ModelPart armPart,
                           float pitch,
                           float yaw,
                           float roll,
                           PoseStack matrices,
                           AbstractClientPlayer player,
                           MultiBufferSource vcp,
                           int light)
    {
        armPart.xRot = pitch;
        armPart.yRot = yaw;
        armPart.zRot = roll;
        armPart.render(matrices, vcp.getBuffer(RenderType.entityCutout(player.getSkinTextureLocation())),
                light, OverlayTexture.NO_OVERLAY);
    }
}
