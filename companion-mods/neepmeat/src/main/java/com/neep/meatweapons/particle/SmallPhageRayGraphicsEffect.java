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
import com.neep.meatlib.client.RotationAxis;
import com.neep.meatweapons.client.BeamRenderer;
import com.neep.meatweapons.client.particle.PhageRaySplodgeParticle;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.meatweapons.meatgun.module.ShortPhageRayModule;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.particle.PhageRayGraphicsEffect;
import com.neep.neepmeat.machine.live_machine.process.PhageRayProcess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class SmallPhageRayGraphicsEffect extends PhageRayGraphicsEffect
{
    public static final RenderType TRAIL_LAYER = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/misc/phage_ray_beam_2.png"));
    public static final RenderType TUBE_LAYER = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/block/phage_ray/beam_small.png"));

    @Nullable private PhageRaySplodgeParticle particle;
    private final Minecraft client = Minecraft.getInstance();

    public SmallPhageRayGraphicsEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, PhageRayProcess.PhageRay.EMPTY, start, end, col, scale, maxTime, buf);

        InteractionHand hand = buf.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        Entity entity = world.getEntity(buf.readVarInt());
        if (entity instanceof Player playerEntity)
        {
            var component = MWComponents.MEATGUN.getNullable(playerEntity.getItemInHand(hand));

            if (component != null)
            {
                MeatgunModule module = component.getRootHolder().moduleCache().findModule(buf.readUUID());
                if (module instanceof ShortPhageRayModule phageRayModule)
                {
                    parent = new RayDelegate(playerEntity, component, phageRayModule, hand);
                }
                else
                {
                    // UUUUGHj
                    remove();
                }
            }
            else
            {
                // EEEEE It looks horrible, but my brain can't work out how to rearrange this sensibly.
                remove();
            }
        }
        else
        {
            remove();
        }
    }

    @Override
    protected RenderType getTrailLayer()
    {
        return TRAIL_LAYER;
    }

    @Override
    protected RenderType getTubeLayer()
    {
        return TUBE_LAYER;
    }

    @Override
    public void tick()
    {
        if (parent.trigger() && (particle == null || !particle.isAlive()) && PhageRaySplodgeParticle.FACTORY != null)
        {
            this.particle = PhageRaySplodgeParticle.FACTORY.createParticle(client.level, parent);
            client.particleEngine.add(particle);

            Vec3 end = parent.getClientBeamEnd(parent.getBeamOrigin(), 0);
            particle.setPos(end.x, end.y, end.z);
            particle.setPrevPos(end.x, end.y, end.z);
            particle.scale(0.4f);
        }

        // Update the particle's position
        if (particle != null)
        {
            if (!parent.trigger())
                particle.remove();
        }

        super.tick();
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource vcp, float tickDelta)
    {
        if (!parent.trigger())
            return;

        this.start = parent.getBeamOrigin();
        this.end = parent.getClientBeamEnd(start, tickDelta);

        if (particle != null)
        {
            particle.setPos(end.x, end.y, end.z);
            particle.setPrevPos(end.x, end.y, end.z);
        }

        matrices.pushPose();
        VertexConsumer consumer = vcp.getBuffer(getTrailLayer());

        BeamRenderer.renderBeam2(matrices, consumer, camera.getPosition(),
                start, end, (parent.getAge() + tickDelta) * 40, 255, 255, 255,
                255, scale, 255, 0);

        BeamRenderer.renderTube(camera.getPosition(), start, end, tickDelta, (int) parent.getAge(), matrices, vcp.getBuffer(getTubeLayer()), 255, 0.55f * scale, 1);

        matrices.popPose();
    }

    @Override
    public void remove()
    {
        super.remove();
        if (particle != null)
            particle.remove();
    }

    private static class RayDelegate implements PhageRayProcess.PhageRay
    {
        private final Player player;
        private final MeatgunComponent component;
        private final ShortPhageRayModule module;
        private final InteractionHand hand;
        private final Minecraft client = Minecraft.getInstance();

        public RayDelegate(Player player, MeatgunComponent component, ShortPhageRayModule module, InteractionHand hand)
        {
            this.player = player;
            this.component = component;
            this.module = module;
            this.hand = hand;
        }

        @Override
        public boolean trigger()
        {
            return module.triggerHeld();
        }

        public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
        {
            float baseOffset = ((Meatgun) stack.getItem()).getWorldHeightOffset();
            return new Vec3(
                    0,
                    baseOffset,
                    .25);
        }

        @Override
        public Vec3 getBeamOrigin()
        {
            float tickDelta = client.getTimer().getRealtimeDeltaTicks();
//            Vector4f v = new Vector4f(
//                    0,
//                    ((Meatgun) component.getStack().getItem()).getWorldHeightOffset(),
//                    -player.getWidth() / 2 + 0.1f, 1);
            var o = getMuzzleOffset(player, component.getStack());
            Vector4f v = new Vector4f((float) o.x, (float) o.y, (float) o.z, 1f);
            Matrix4f moduleTransform = module.getTransform();
            int rightHand = (player.getMainArm() == HumanoidArm.RIGHT && hand == InteractionHand.MAIN_HAND) ? 1 : -1;
            boolean staff = component.getRootHolder().root.getType() == MeatgunModules.BASE_STAFF;

            Matrix4f transform = new Matrix4f();
            transform.rotate(RotationAxis.NEGATIVE_Y.rotationDegrees(180 + player.getViewYRot(tickDelta)));

            // Assume that PhageRayHoldMeatgunAnimation is active
            if (staff)
            {
                transform.rotate(RotationAxis.NEGATIVE_X.rotationDegrees(90 + player.getViewXRot(tickDelta)));
                // Move the point closer to the centre to align it with the end of the staff
                transform.translate(0, 0, -0.2f);
            }
            else
            {
                transform.rotate(RotationAxis.NEGATIVE_X.rotationDegrees(player.getViewXRot(tickDelta)));
            }
            transform.translate(rightHand * 0.2f, 0, 0);
            transform.mul(moduleTransform);
//            transform.translate(0, 0, 1);
//            transform.scale(1, 1, 0.01f);
            transform.transform(v);

            Vec3 pos = player.getPosition(client.getTimer().getRealtimeDeltaTicks()).add(0, player.getEyeHeight(), 0);
            return new Vec3(pos.x + v.x, pos.y + v.y, pos.z + v.z);
        }

        @Override
        public Vec3 getClientBeamEnd(Vec3 start, float tickDelta)
        {
            // Pont on the plane
            Vec3 direct = player.pick(ShortPhageRayModule.DISTANCE, client.getTimer().getRealtimeDeltaTicks(), false).getLocation();
            Vec3 facing = player.getViewVector(tickDelta); // Normal to the plane

            // Project onto the plane
            Vec3 p2d = direct.subtract(start);
            double dist = facing.dot(p2d) - 0.1;

            return new Vec3(start.x + facing.x * dist, start.y + facing.y * dist, start.z + facing.z * dist);
        }

        @Override
        public int getAge()
        {
            // Hmmm
            return (int) (Minecraft.getInstance().getFrameTimeNs() % Integer.MAX_VALUE);
        }
    }
}
