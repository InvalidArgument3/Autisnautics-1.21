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

package com.neep.meatweapons.client.renderer.meatgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.client.network.MeatgunClient;
import com.neep.meatweapons.component.GunTickHandler;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.mixin.DrawContextAccessor;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public interface MeatgunItemRenderer
{
    default float modelScale()
    {
        return 1.2f;
    }

    default Matrix4f getFirstPersonModelTransform()
    {
        float modelScale = modelScale();
        return new Matrix4f()
                .scaleAround(modelScale, modelScale, modelScale, 8 / 16f, 0 / 16f, 0 / 16f)
                .translate(0, 3.5f / 16f, -9 / 16f)
                ;
    }

    default void transformRecoil(PoseStack matrices, RecoilManager recoil, boolean leftHand)
    {
        matrices.translate(0, 0, recoil.horAmount);
        matrices.translate(0, 0, 1.4);
        matrices.mulPose(Axis.XP.rotationDegrees(
                recoil.direction == MeatgunNetwork.RecoilDirection.DOWN ? -recoil.amount : recoil.amount));
        matrices.translate(0, 0, -1.4);
    }

    default void applyAds(PoseStack matrices, Player player, boolean mainHand, boolean leftHanded, float tickDelta)
    {
        // ADS
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

    default void renderParticles(PoseStack matrices, ItemDisplayContext mode, Matrix4f modelTransform, MultiBufferSource vcp, int light, int overlay, boolean mainHand, float tickDelta)
    {
        if (mode.firstPerson())
        {
            matrices.pushPose();
            matrices.mulPoseMatrix(modelTransform);
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            matrices.translate(0.5, 0, 1); // No idea why this transform is necessary, but this puts (0,0,0) at (8,0,16) in model coords.

            VertexConsumer consumer = vcp.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_PARTICLES));
            for (var particle : MeatgunParticleManager.getParticles())
            {
                if (particle.hand() == (mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND))
                    particle.render(matrices, camera, consumer, overlay, tickDelta);
            }
            matrices.popPose();
        }
    }

    default void renderScreen(Minecraft client, PoseStack matrices, ItemDisplayContext mode, MultiBufferSource vcp, RootModuleHolder holder, float tickDelta, boolean leftHanded)
    {
        if (mode == ItemDisplayContext.GROUND || mode == ItemDisplayContext.GUI || mode == ItemDisplayContext.FIXED || mode == ItemDisplayContext.HEAD || mode == ItemDisplayContext.NONE)
            return;

        if (!NeepMeat.CONFIG.meatgunAmmoOnModel.getRealValue())
            return;

        matrices.pushPose();
//        float sf1 = MathHelper.sin(AnimationTickHolder.getRenderTime() / 10);
        float sf1 = 0.004f;
        matrices.translate((leftHanded ? 10 : 2) / 16f, 2 / 16f, 18 / 16f);
        matrices.scale(sf1, -sf1, 1);
        GuiGraphics drawContext = DrawContextAccessor.init(client, matrices, client.renderBuffers().bufferSource());
        MeatgunClient.renderHud(drawContext, tickDelta, holder, false);
        drawContext.flush();
        matrices.popPose();
    }

    default <T extends MeatgunModule> void renderRecursive(PoseStack matrices, T module, ItemStack stack, MeatgunComponent component,
                                                           ItemDisplayContext mode, MultiBufferSource vcp, long time, float tickDelta, int light, int overlay)
    {
        if (module == MeatgunModule.DEFAULT)
            return;

        MeatgunModuleRenderer<T> renderer = MeatgunModuleRenderers.get(module);
        if (renderer != null)
            renderer.render(stack, component, module, mode, matrices, vcp, time, tickDelta, light, overlay);

        for (var child : module.getChildren())
        {
            if (!child.shouldRender())
                continue;

            matrices.pushPose();
            matrices.translate(0.5, 0, 1);
            Matrix4f matrix4f = child.transform(tickDelta);
            matrices.mulPoseMatrix(matrix4f);
            matrices.translate(-0.5, 0, -1);
            renderRecursive(matrices, child.get(), stack, component, mode, vcp, time, tickDelta, light, overlay);
            matrices.popPose();
        }
    }
}
