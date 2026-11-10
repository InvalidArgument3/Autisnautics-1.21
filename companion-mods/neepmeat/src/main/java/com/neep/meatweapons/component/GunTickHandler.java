package com.neep.meatweapons.component;

import com.neep.meatweapons.init.MWComponents;
import com.neep.neepmeat.util.Easing;
import com.neep.neepmeat.util.NMMaths;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

/// Attempts to provide a smooth-ish ADS transition animation.
public class GunTickHandler implements Component, CommonTickingComponent
{
    private final AdsCounter ads;
    private final LivingEntity entity;
    private float prevRecoilPitch;
    private float lastImpulse = 0;
    private float recoilPitch;
    private int ticksWithoutImpulse = 0;
    private float prevEntityPitch;

    public GunTickHandler(LivingEntity entity)
    {
        ads = new AdsCounter(entity);
        this.entity = entity;
    }

    public static GunTickHandler get(Player player)
    {
        // Players should always provide this component
        return MWComponents.GUN_TICK_HANDLER.get(player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag nbtCompound) { }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbtCompound) { }

    public AdsCounter getAds()
    {
        return ads;
    }

    /// Degrees
    public float getRecoilAngle(float tickDelta)
    {
//        if (MeatLib.isClient())
//            return -Math.lerp(prevRecoilAngle, recoilAngle, tickDelta);
//        else return  0;
//        return 0;

        // Upward pitch is negative
//        return -Math.lerp(prevRecoilAngle, recoilAngle, tickDelta);
        return 0;
    }

    @Override
    public void tick()
    {
        ads.tick();

        // Deal with the counter.
        // Wait 4 ticks before removing recoil.
        int maxTicksWithoutImpulse = 4;
        if (lastImpulse == 0)
            ticksWithoutImpulse = Math.min(ticksWithoutImpulse + 1, maxTicksWithoutImpulse);
        else
            ticksWithoutImpulse = 0;

        // Player is fighting the recoil. Reset pitch offset.
        float entityPitch = entity.getXRot();
        if (ticksWithoutImpulse == 0 && entityPitch > prevEntityPitch)
        {
            recoilPitch = 0;
            prevRecoilPitch = 0;
        }

        prevRecoilPitch = recoilPitch;

        if (ticksWithoutImpulse >= maxTicksWithoutImpulse)
        {
            // Bring recoilPitch back to zero.
            // This will gradually move the player's pitch to where it was before shooting.
            float error = 0 - recoilPitch;
            if (error != 0)
            {
                // Find the direction to return back to zero
                error = NMMaths.minMag(error, 5 * Math.signum(error));
                recoilPitch = recoilPitch + error;
            }
        }
        else
        {
            // Apply impulses
            recoilPitch = recoilPitch + lastImpulse;
        }

        lastImpulse = 0;
        prevEntityPitch = entityPitch;

        // Wrap for safety.
        // Entity pitch is clamped between [-90 90], so there shouldn't be any discontinuities
        recoilPitch = Mth.wrapDegrees(recoilPitch);
    }

    public void addRecoil(float cameraAmountDegrees)
    {
        this.lastImpulse -= cameraAmountDegrees;
    }

    /// Gives the constant angular velocity at which to rotate the camera.
    /// @return velocity in degrees per tick
    private float getScreenRecoilSpeed()
    {
        return recoilPitch - prevRecoilPitch;
    }

    private float getScreenRecoilSpeed(float tickDelta)
    {
        return getScreenRecoilSpeed();
//        return (float) Easing.dEaseInOutCubic(tickDelta) * (recoilPitch - prevRecoilPitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Client
    {
        private static float smoothed = 0;

        public static void init()
        {
            HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
            {
                Minecraft client = Minecraft.getInstance();
                float dt = client.getDeltaFrameTime();

                if (!client.isPaused())
                {
                    var handler = client.player.getComponent(MWComponents.GUN_TICK_HANDLER);
                    float speed = handler.getScreenRecoilSpeed(tickDelta);

                    float prevPitch = client.player.getViewXRot(tickDelta);
                    smoothed = Math.lerp(smoothed, speed, 0.97f * dt);
                    client.player.setXRot(prevPitch + smoothed * dt);
                }
            });
        }
    }
}
