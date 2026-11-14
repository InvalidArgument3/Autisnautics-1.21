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

package com.neep.neepmeat.entity.follower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FollowerRenderer extends EntityRenderer<FollowerEntity>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/follower/main.png");

    private final Minecraft client = Minecraft.getInstance();

    public FollowerRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public void render(FollowerEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light)
    {
    }

    @Override
    public Vec3 getPositionOffset(FollowerEntity entity, float tickDelta)
    {
        return super.getRenderOffset(entity, tickDelta);
    }

    @Override
    public ResourceLocation getTextureLocation(FollowerEntity entity)
    {
        return TEXTURE;
    }

}
