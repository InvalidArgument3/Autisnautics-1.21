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

package com.neep.neepmeat.implant.entity;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpiderLegsImplant implements EntityImplant
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "spider_legs");

    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(NeepMeat.NAMESPACE, "default");
    public static final ResourceLocation TH_0 = new ResourceLocation(NeepMeat.NAMESPACE, "th_0");
    public static final ResourceLocation TH_1 = new ResourceLocation(NeepMeat.NAMESPACE, "th_1");
    public static final ResourceLocation BLACK_BOOTS = new ResourceLocation(NeepMeat.NAMESPACE, "black_boots");

    private ResourceLocation modelId = DEFAULT_MODEL;

    private final List<Leg> legs = new ObjectArrayList<>();
    private final Player player;

    private double meanFootHeight;
    private double prevMeanFootHeight;

    public float smoothEyeHeight = 1.7f;
    public float prevSmoothEyeHeight = 1.7f;

    public SpiderLegsImplant(Entity entity)
    {
        this((Player) entity);
    }

    public SpiderLegsImplant(Player player)
    {
        this.player = player;
        this.meanFootHeight = player.getY(); // Initialise to sensible value for rendering
        // Small
        float step = 0.8f;
        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(0.3, 0.6, 0.2), new Vec3(0.7, 0, 0.9)));
        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(-0.3, 0.6, 0.2), new Vec3(-0.7, 0, 0.9)));

        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(-0.4, 0.5, 0)));
        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(0.4, 0.5, 0)));

        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(0.3, 0.6, -0.6)));
        legs.add(new SpiderLegsImplant.Leg(player, 1, step, new Vec3(-0.3, 0.6, -0.6)));

//        float step = 1f;
//        legs.add(new Leg(player, 2, step, new Vec3d(0.8, 0.6, 0.7), new Vec3d(1.6, 0, 1.4)));
//        legs.add(new Leg(player, 2, step, new Vec3d(-0.8, 0.6, 0.7), new Vec3d(-1.6, 0, 1.4)));
//
//        legs.add(new Leg(player, 2, step, new Vec3d(-1, 0.5, 0)));
//        legs.add(new Leg(player, 2, step, new Vec3d(1, 0.5, 0)));
//
//        legs.add(new Leg(player, 2, step, new Vec3d(0.8, 0.6, -1)));
//        legs.add(new Leg(player, 2, step, new Vec3d(-0.8, 0.6, -1)));
    }

    private static final Multimap<Attribute, AttributeModifier> MODIFIERS = ImmutableMultimap.<Attribute, AttributeModifier>builder()
            .put(Attributes.MOVEMENT_SPEED, new AttributeModifier("Spider legs modifier", 0.01, AttributeModifier.Operation.ADDITION))
            .build();

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers()
    {
        return MODIFIERS;
    }

    public float modifyEyeHeight(Player player)
    {
        return getMeanBodyOffset();
    }

    public boolean swimOrFly()
    {
        return player.isVisuallySwimming() || player.isFallFlying();
    }

    public float getMeanBodyOffset()
    {
        return (float) (meanFootHeight - player.getY());
    }

    public double getMeanBodyOffset(float tickDelta)
    {
        return getMeanFootHeight(tickDelta) - Mth.lerp(tickDelta, player.yo, player.getY());
    }

    public double getMeanFootHeight(float tickDelta)
    {
        return Mth.lerp(tickDelta, prevMeanFootHeight, meanFootHeight);
    }

    public void setModelId(ResourceLocation modelId)
    {
        this.modelId = modelId;
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {
        ((ImplantRenderingHelper) player).neepmeat$setSpiderLegs(this);

        if (player.level().getGameTime() % 40 == 0)
        {
            if (player.maxUpStep() < PoweredKneesImplant.ACTIVE_STEP_HEIGHT)
            {
                player.setMaxUpStep(PoweredKneesImplant.ACTIVE_STEP_HEIGHT);
            }
        }

        double meanHeight = 0;
        for (Leg leg : legs)
        {
            leg.tick();

            if (!leg.offGround && !swimOrFly())
                meanHeight += leg.position.y;
            else
                meanHeight += player.getY();
        }

        meanHeight /= legs.size();

//        if (!player.getWorld().isClient())
//        {
            prevMeanFootHeight = meanFootHeight;

            Level world = player.level();
            double playerY = player.getY();
            float offset = (float) (meanHeight - playerY);

            // Check if there is a block above the player that the camera will enter.
            // Use standing bounding box to prevent the camera from moving upwards when sneaking.
            // Disable camera repositioning if underwater to prevent jerky motion
            if ((offset > 0 && !world.noCollision(player, player.getLocalBoundsForPose(Pose.STANDING).move(player.position()).move(0, offset, 0)))
                    || player.isInWater())
            {
                meanFootHeight = playerY;
            }
            else
            {
                meanFootHeight = meanHeight;
            }

//            player.getDataTracker().set(MEAN_FOOT_HEIGHT, meanFootHeight);
//        }
//        else
//        {
//            prevMeanFootHeight = meanFootHeight;
//            meanFootHeight = player.getDataTracker().get(MEAN_FOOT_HEIGHT);
//        }

        boolean ascended = player.yo < player.getY();

        // Do not smooth over large distances to prevent jerking when stepping up blocks
        prevSmoothEyeHeight = smoothEyeHeight;
        float target = player.getEyeHeight();
        float difference = target - smoothEyeHeight;
        float increment = (ascended && difference < 0.4) ? difference : difference * 0.5f;
        smoothEyeHeight = smoothEyeHeight + increment;
    }

    @Override
    public void clientTick()
    {
        tick(() -> {});
    }

    @Override
    public void onInstall()
    {
        if (player.maxUpStep() < PoweredKneesImplant.ACTIVE_STEP_HEIGHT)
            player.setMaxUpStep(PoweredKneesImplant.ACTIVE_STEP_HEIGHT);
    }

    @Override
    public void onUninstall()
    {
        ((ImplantRenderingHelper) player).neepmeat$setSpiderLegs(null);

        // What are you doing step height?
        if (player.maxUpStep() == PoweredKneesImplant.ACTIVE_STEP_HEIGHT)
            player.setMaxUpStep(0.6f);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("model_id", modelId.toString());
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.modelId = ResourceLocation.tryParse(nbt.getString("model_id"));
    }

    public List<Leg> getLegs()
    {
        return legs;
    }

    public void onJump()
    {
        legs.forEach(l ->
        {
            l.offGround = true;
            l.jumpDelayTicks = 7;
        });
    }

    public ResourceLocation getModelId()
    {
        return modelId;
    }

    public static class Leg
    {
        private final Player player;

        private Vec3 position = Vec3.ZERO;
        private Vec3 prevPosition0 = Vec3.ZERO;
        private Vec3 prevPosition1 = Vec3.ZERO;

        private final float length;
        private final Vec3 offset;
        private final Vec3 medianOffset;
        private boolean offGround;

        private int offGroundTicks = 0;;
        private int jumpDelayTicks = 0;

        // What are you doing, stepRadius?
        private final float stepRadius;
//        private final float stepThreshold = 0.6f;

        public Leg(Player player, float length, float stepRadius, Vec3 offset, Vec3 medianOffset)
        {
            this.player = player;
            this.length = length;
            this.offset = offset;
            this.medianOffset = medianOffset;
            this.stepRadius = stepRadius;
            position = player.position();
        }

        public Leg(Player player, float length, float stepRadius, Vec3 offset)
        {
            this(player, length, stepRadius, offset, new Vec3(offset.x * 2, 0, offset.z * 2));
        }

        public Vec3 rotatedOffset(float tickDelta)
        {
            return offset.yRot(-Mth.lerp(tickDelta, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD);
        }

        public Vec3 medianOffset(float tickDelta)
        {
            return medianOffset.yRot(-Mth.lerp(tickDelta, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD);
        }

        public Vec3 getPosition()
        {
            return position;
        }

        public Vec3 getPositionSmooth(float tickDelta)
        {
            return prevPosition1.lerp(prevPosition0, tickDelta);
        }

        private void moveSwimming(Level world, Vec3 median, Vec3 currentOrigin)
        {
            offGround = false;
            if (currentOrigin.distanceTo(position) > segmentLength() * 2)
            {
                Vec3 to = position.subtract(currentOrigin).normalize().scale(segmentLength() * 2);
                position = currentOrigin.add(to);
            }
        }

        private void moveWalking(Level world, Vec3 median, Vec3 currentOrigin)
        {
            if (NMMaths.distance2D(median.x, median.z, position.x, position.z) > stepRadius || Math.abs(median.y - position.y) > 1)
            {
                Vec3 velocity = player.getDeltaMovement();

                Vec3 direction = Vec3.ZERO;
                if (velocity.length() > 0.01)
                {
                    direction = velocity.multiply(1, 0, 1).normalize();
                }

                double newX = median.x + direction.x * stepRadius;
                double newZ = median.z + direction.z * stepRadius;

                Double ground = findGround(world, newX, Math.floor(currentOrigin.y) + 1, newZ,  3);

                if (ground != null)
                {
                    position = new Vec3(newX, ground, newZ);
                }
                else
                {
                    offGround = true;
                }
            }
        }

        public void tick()
        {
            Level world = player.level();

            prevPosition1 = prevPosition0;
            prevPosition0 = position;

            Vec3 currentOrigin = player.position().add(rotatedOffset(1));

            Vec3 median = player.position().add(medianOffset(1));

            if (!canLand())
            {
                offGround = true;
            }
            else if (player.isVisuallySwimming() || player.isFallFlying())
            {
                moveSwimming(world, median, currentOrigin);
            }
            else
            {
                moveWalking(world, median, currentOrigin);
            }


            if (offGround)
            {
                position = median.add(0, 0.3, 0);

                if (canLand() && offGroundTicks % 5 == 0 && jumpDelayTicks == 0)
                {
                    Double ground = findGround(world, median.x, Math.floor(currentOrigin.y) + 1, median.z, 3);
                    if (ground != null)
                    {
                        position = new Vec3(median.x, ground, median.z);
                        offGround = false;
                        offGroundTicks = 0;
                    }
                }

                offGroundTicks++;
                jumpDelayTicks = Math.max(0, jumpDelayTicks - 1);
            }
        }

        public boolean canLand()
        {
            return !player.getAbilities().flying && !player.onClimbable() && !player.isPassenger();
        }

        @Nullable
        private Double findGround(Level world, double x, double yStart, double z, int depth)
        {
            var result = world.clip(new ClipContext(new Vec3(x, yStart, z), new Vec3(x, yStart - depth, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if ((Math.abs(result.getLocation().y - yStart) < 0.05 && world.getBlockState(BlockPos.containing(x, yStart, z)).blocksMotion())
                    || result.getLocation().y <= yStart - depth)
                return null;

            return result.getLocation().y;
        }

        public float segmentLength()
        {
            return length;
        }
    }
}
