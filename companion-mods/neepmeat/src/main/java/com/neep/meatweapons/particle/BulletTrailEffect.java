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

package com.neep.meatweapons.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.BeamRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BulletTrailEffect extends BeamGraphicsEffect
{
    public static final ResourceLocation START_TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/bullet_trail.png");
    public static final ResourceLocation FULL_TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/bullet_trail_full.png");

    private final boolean full;

    public BulletTrailEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        this(world, start, end, col, scale, maxTime, buf, false);
    }

    public BulletTrailEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf, boolean full)
    {
        super(world, start, end, col, scale, maxTime, buf);
        this.full = full;
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        matrices.pushPose();
        VertexConsumer consumer = consumers.getBuffer(RenderType.entityTranslucent(full ? FULL_TRAIL_TEXTURE : START_TRAIL_TEXTURE));
        float x = (maxTime - time + 2 - tickDelta) / (float) maxTime;
        BeamRenderer.renderBeam(matrices, consumer, camera.getPosition(),
                start, end, (int) col.x, (int) col.y, (int) col.z,
               255, scale, 255);
        matrices.popPose();

//        214, 175, 32,
    }
}
