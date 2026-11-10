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

package com.neep.neepmeat.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatweapons.client.BeamRenderer;
import com.neep.meatweapons.client.FullBeamRenderer;
import com.neep.meatweapons.particle.BeamGraphicsEffect;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.live_machine.process.PhageRayProcess;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PhageRayGraphicsEffect extends BeamGraphicsEffect
{
    public static final RenderType TRAIL_LAYER = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/misc/phage_ray.png"));
    public static final RenderType CRYSTAL_BEAM_LAYER = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/block/phage_ray/beam.png"));

    protected PhageRayProcess.PhageRay parent = PhageRayProcess.PhageRay.EMPTY;

    private final RandomSource random = RandomSource.create();

    public PhageRayGraphicsEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, start, end, col, scale, maxTime, buf);
        if (world.getEntity(buf.readVarInt()) instanceof PhageRayProcess.PhageRay phageRay)
        {
            this.parent = phageRay;
        }
        else
        {
            remove();
        }
    }

    protected PhageRayGraphicsEffect(Level world, PhageRayProcess.PhageRay parent, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, start, end, col, scale, maxTime, buf);
        this.parent = parent;
    }

    protected RenderType getTrailLayer()
    {
        return TRAIL_LAYER;
    }

    protected RenderType getTubeLayer()
    {
        return CRYSTAL_BEAM_LAYER;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (parent.trigger() && random.nextInt(8) == 0)
        {

            double f = 2;
            double d = 0.1f;

            var ox = end.x + random.triangle(0, f);
            var oy = end.y + random.triangle(0, f);
            var oz = end.z + random.triangle(0, f);

//        var direction = end.subtract(start).normalize();
            world.addParticle(
                    MWParticles.LONG_BOI_HIT,
                    end.x,
                    end.y,
                    end.z,
//                ox,
//                oy,
//                oz,
//                end.x - ox,
//                end.y - oy,
//                end.z - oz
                    random.triangle(0, d),
                    random.triangle(0, d),
                    random.triangle(0, d)
            );
        }
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        if (!parent.trigger())
            return;

        this.start = parent.getBeamOrigin();

        // Raycast must be done every frame to prevent jerkiness
        this.end = parent.getClientBeamEnd(start, tickDelta);

        matrices.pushPose();
        Vec3 ray = end.subtract(start);
        var camPos = camera.getPosition();
        float pitch = (float) Math.atan2(ray.x(), ray.z());
        float yaw = (float) Math.asin(-ray.y() / ray.length());


        matrices.pushPose();
        matrices.translate(start.x - camPos.x, start.y - camPos.y, start.z - camPos.z);
        matrices.mulPose(Axis.YP.rotation(pitch));
        matrices.mulPose(Axis.XP.rotation(yaw - Mth.HALF_PI));

        VertexConsumer beamConsumer = consumers.getBuffer(RenderType.lightning());
        FullBeamRenderer.renderFullBeam(matrices, scale / 2, (float) ray.length(), 6, 1, 1, 1, 1, beamConsumer);
        matrices.popPose();

        // Old stupid beam
//        BeamRenderer.renderBeam(matrices, consumer, camera.getPos(),
//                start, end, (MathHelper.wrapDegrees(world.getTime()) + tickDelta) * 30, 255, 255, 255,
//               255, scale, 255);

        VertexConsumer tubeConsumer = consumers.getBuffer(getTrailLayer());
        BeamRenderer.renderTube(camera.getPosition(), start, end, tickDelta, parent.getAge(), matrices, consumers.getBuffer(getTubeLayer()), 255, 0.75f * scale, 0);

        matrices.popPose();
    }
}
