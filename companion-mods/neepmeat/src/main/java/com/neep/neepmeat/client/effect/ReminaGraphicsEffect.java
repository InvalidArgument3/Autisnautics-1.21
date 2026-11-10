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

package com.neep.neepmeat.client.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.neep.meatlib.graphics.GraphicsEffect;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public class ReminaGraphicsEffect implements GraphicsEffect
{
    protected final Level world;

    private static final ResourceLocation EYE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/world/object_3.png");
    private boolean removed;

    private float alpha = 0;
    private float targetAlpha;

    public ReminaGraphicsEffect(Level world, FriendlyByteBuf buf)
    {
        this.world = world;

        targetAlpha = 0.5f;
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
    }

    @Override
    public void renderAfter(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        if (!Minecraft.getInstance().isPaused())
            alpha = Mth.lerp(0.0005f, alpha, targetAlpha);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
//        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

//        RenderSystem.enableTexture();
//            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

        matrices.pushPose();
//            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1);
        double div = 60;
        double yRot = Mth.wrapDegrees(Minecraft.getInstance().level.getGameTime() / div) + tickDelta / div;
        matrices.mulPose(Axis.YP.rotationDegrees((float) yRot));
        matrices.mulPose(Axis.XP.rotationDegrees(0f));
        Matrix4f matrix4f2 = matrices.last().pose();

        float k = 400f;
        double playerY = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y;
        float thingY = 200;

//        float y = (float) (thingY - playerY);
        float y = 300;

        float r = 1;
        float g = 1;
        float b = 1;
        float a = alpha;

        RenderSystem.setShaderColor(r, g, b, a);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShaderTexture(0, EYE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix4f2, -k, y, -k).setUv(0.0f, 0.0f).setColor(r, g, b, 1);
        bufferBuilder.addVertex(matrix4f2, k, y, -k).setUv(1.0f, 0.0f).setColor(r, g, b, 1);
        bufferBuilder.addVertex(matrix4f2, k, y, k).setUv(1.0f, 1.0f).setColor(r, g, b, 1);
        bufferBuilder.addVertex(matrix4f2, -k, y, k).setUv(0.0f, 1.0f).setColor(r, g, b, 1);
        var built = bufferBuilder.end();
        BufferUploader.drawWithShader(built);
        matrices.popPose();

        // Hopefully no weirdness
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void tick()
    {
        if (!EnlightenmentManager.get(Minecraft.getInstance().player).objectAbove)
        {
            targetAlpha = 0;

            if (Mth.equal(alpha, targetAlpha))
            {
                removed = true;
            }
        }
    }

    @Override
    public boolean isRemoved()
    {
        return removed;
    }

    @Override
    public Level getWorld()
    {
        return world;
    }
}
