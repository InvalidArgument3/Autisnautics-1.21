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

package com.neep.neepmeat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DummyEntityRenderer<T extends Entity> extends EntityRenderer<T>
{
    public DummyEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z)
    {
//        return false;
        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
        if (this.shouldShowName(entity) && entity == entityRenderDispatcher.crosshairPickEntity)
        {
            this.renderNameTag(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity)
    {
        return null;
    }
}
