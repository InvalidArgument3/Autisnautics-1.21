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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.entity.ShockStaffProjectileEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ShockStaffProjectileEntityRenderer extends EntityRenderer<ShockStaffProjectileEntity>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/entity/shock_staff_projectile.png");

    private static final float MIN_DISTANCE = 12.25F;
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean lit;

    public ShockStaffProjectileEntityRenderer(EntityRendererProvider.Context ctx, float scale, boolean lit)
    {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.scale = scale;
        this.lit = lit;
    }

    public ShockStaffProjectileEntityRenderer(EntityRendererProvider.Context context)
    {
        this(context, 1.0F, false);
    }

    @Override
    protected int getBlockLight(ShockStaffProjectileEntity entity, BlockPos pos)
    {
        return this.lit ? 15 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    public void render(ShockStaffProjectileEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
        if (entity.tickCount >= 3 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25))
        {
            matrices.pushPose();
            matrices.scale(this.scale, this.scale, this.scale);
            matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
            matrices.mulPose(Axis.ZP.rotation((float) Minecraft.getInstance().getFrameTimeNs() / 2));

            VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
            float f = 0;
            float g = 0;
            float h = 0;

            float i = 0.5f;
            Vector3f[] vector3fs = new Vector3f[]{
                    new Vector3f(-1.0F, -1.0F, 0F),
                    new Vector3f(-1.0F, 1.0F, 0F),
                    new Vector3f(1.0F, 1.0F, 0F),
                    new Vector3f(1.0F, -1.0F, 0F)};

            for (int j = 0; j < 4; ++j)
            {
                Vector3f vector3f = vector3fs[j];
                vector3f.mul(i);
                vector3f.add(f, g, h);

                var v4 = new Vector4f(vector3f, 1);
                Matrix4f mat = new Matrix4f(matrices.last().pose());
                v4.mul(mat);
                vector3f.set(v4.x, v4.y, v4.z);
            }

            float k = 0;
            float l = 16 / 64f;
            float m = 0;
            float n = 16 / 64f;

            float red = 1;
            float green = 1;
            float blue = 1;
            float alpha = 1;

            int overlay = OverlayTexture.NO_OVERLAY;

            consumer.addVertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
                    .setColor(red, green, blue, alpha)
                    .setUv(k, m)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0)
                    ;
            consumer.addVertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
                    .setColor(red, green, blue, alpha)
                    .setUv(l, m)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0)
                    ;
            consumer.addVertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
                    .setColor(red, green, blue, alpha)
                    .setUv(l, n)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0)
                    ;
            consumer.addVertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
                    .setColor(red, green, blue, alpha)
                    .setUv(k, n)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0)
                    ;

            matrices.popPose();
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ShockStaffProjectileEntity entity)
    {
        return TEXTURE;
    }
}
