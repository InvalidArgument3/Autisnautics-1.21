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

package com.neep.meatweapons.entity;

import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirtruckEntity extends AbstractVehicleEntity implements GeoEntity
{
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);

    public final float maxSpeed = 0.05f;
    protected final float forwardsAccel = 0.004f;
    public float forwardsVelocity;

    protected boolean accelerating;
    protected boolean braking;
    protected short soundStage = 0;

    public AirtruckEntity(EntityType<? extends AbstractVehicleEntity> type, Level world)
    {
        super(type, world);
    }

    public static AirtruckEntity create(Level world)
    {
        return new AirtruckEntity(MeatWeapons.AIRTRUCK, world);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    protected void updateMotion()
    {
        if (!this.isVehicle())
        {
            return;
        }

        float upVelocity = 0.0f;
        if (this.pressingLeft)
            this.yawVelocity -= 1.0f;
        if (this.pressingRight)
            this.yawVelocity += 1.0f;

        this.setYRot(this.getYRot() + this.yawVelocity);

        if (this.pressingForward && !this.pressingBack)
        {
            this.forwardsVelocity = Math.min(this.forwardsVelocity + forwardsAccel, maxSpeed);
            this.accelerating = true;
        }
        else if (!this.pressingForward && this.pressingBack)
        {
            this.forwardsVelocity = Math.max(this.forwardsVelocity - forwardsAccel, -maxSpeed);
        }
        else
        {
            this.forwardsVelocity *= this.velocityDecay;
        }

        if (this.pressingUp)
        {
            upVelocity += 0.08;
        }
        if (this.pressingDown)
        {
            upVelocity -= 0.08;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(Mth.sin(-this.getYRot() * ((float)Math.PI / 180)) * forwardsVelocity,
                upVelocity,
                Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * forwardsVelocity));
    }

    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
//        MinecraftClient.getInstance().getSoundManager().play(new AirtruckSoundInstance(this));
    }

    private PlayState predicate(AnimationState<AirtruckEntity> event)
    {
        event.getController().setAnimation(RawAnimation.begin().thenPlay("animation.airtruck.fly"));
        return PlayState.CONTINUE;
    }

    @Override
    public ItemStack asStack()
    {
        return MWItems.AIRTRUCK.getDefaultInstance();
    }

    @Override
    public SoundEvent getDamageSound()
    {
        return SoundEvents.IRON_GOLEM_DAMAGE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<AirtruckEntity>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }
}
