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

package com.neep.neepmeat.client.hud;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.item.CompoundInjectorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

@OnlyIn(Dist.CLIENT)
public class HUDOverlays
{
    public final Minecraft client;
    private static final ResourceLocation INJECTOR_VIGNETTE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/heal_vignette.png");
    private static final ResourceLocation PYLON_VIGNETTE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/pylon_vignette.png");
    private static double scaledHeight;
    private static double scaledWidth;
    private static float opacity;
    private static int vignetteState;

    public HUDOverlays(Minecraft client)
    {
        this.client = client;
    }

    public static void renderVignettes(float tickDelta)
    {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Minecraft client = Minecraft.getInstance();
        scaledWidth = client.getWindow().getGuiScaledWidth();
        scaledHeight = client.getWindow().getGuiScaledHeight();
        ItemStack itemStack1 = client.player.getMainHandItem();
        ItemStack itemStack2 = client.player.getOffhandItem();

        for (ItemStack itemStack : ImmutableList.of(itemStack1, itemStack2))
        {
            if (itemStack.getItem() instanceof CompoundInjectorItem && CompoundInjectorItem.getHealsRemaining(itemStack) == ((CompoundInjectorItem) NMItems.COMPOUND_INJECTOR).getHealsFor())
            {
                vignetteState = 1;
                break;
            }
        }

        float f = client.getTimer().getGameTimeDeltaPartialTick(true);

        if (vignetteState == 1)
        {
            // Fade vignette in quickly
            opacity = Mth.lerp(0.8F * f, opacity, 1.00F);
            renderOverlay(INJECTOR_VIGNETTE, opacity, 1.1f);
            if (opacity > 0.99)
                vignetteState = 2;
        }
        if (vignetteState == 2)
        {
            // Fade vignette out slowly
            opacity = Mth.lerp(0.1F * f, opacity, 0.0F);
            renderOverlay(INJECTOR_VIGNETTE, opacity, 1.1f);
            if (opacity < 0.001)
            {
                vignetteState = 0;
                opacity = 0;
            }
        }

        pylonVignette(tickDelta);
    }

    protected static int pylonVignetteState = 0;
    protected static float pylonVignetteEnvelope = 0;

    protected static void pylonVignette(float tickDelta)
    {
        // LPF
        pylonVignetteEnvelope = Mth.lerp(0.04f * Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true),
                pylonVignetteEnvelope,
                pylonVignetteState == 1 ? 1 : 0);

        if (pylonVignetteState == 1 && pylonVignetteEnvelope > 0.9) pylonVignetteState = 0;

        // There may be imprecision problems here when the world time gets too large, but I'm not sure how to fix it.
        double den = 20L;
        long a = Minecraft.getInstance().level.getGameTime();
//        float b = tickDelta / 1;
//        float sin = (float) ((float) Math.sin(a) * Math.cos(b) + (float) Math.cos(a) * Math.sin(b));
//        float cos = (float) ((float) Math.cos(a) * Math.cos(b) - (float) Math.sin(a) * Math.sin(b));
        float sin = (float) Math.sin(a / den + tickDelta / den) + 1;
        float cos = (float) Math.cos(a / den + tickDelta / den) + 1;

        float scale = Mth.lerp(sin / 2, 1.3f, 1.5f);
        float opacity = pylonVignetteEnvelope * Mth.lerp(cos / 2, 0.5f, 1f);
        renderOverlay(PYLON_VIGNETTE, opacity, scale);
    }

    public static void startPylonVignette()
    {
        if (pylonVignetteState == 0) pylonVignetteState = 1;
    }

    private static void renderOverlay(ResourceLocation texture, float opacity, float scale) {
        double midx = scaledWidth / 2;
        double midy = scaledHeight / 2;
        double height = scaledHeight * scale;
        double width = scaledWidth * scale;
        double x1 = midx - width / 2;
        double x2 = midx + width / 2;
        double x3 = x2;
        double x4 = x1;
        double y1 = midy + height / 2;
        double y3 = midy - height / 2;

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        RenderSystem.setShaderTexture(0, texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.addVertex((float) x1, (float) y1, -90.0F).setUv(0.0F, 1.0F);
        bufferBuilder.addVertex((float) x2, (float) y1, -90.0F).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex((float) x3, (float) y3, -90.0F).setUv(1.0F, 0.0F);
        bufferBuilder.addVertex((float) x4, (float) y3, -90.0F).setUv(0.0F, 0.0F);

        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferBuilder.build());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void init()
    {
//        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> renderVignettes(matrixStack, tickDelta));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> renderVignettes(tickDelta));
    }
}
