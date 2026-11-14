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

package com.neep.neepmeat.entity.hound;

import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.SightUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HoundEntity extends FlyingMob implements Enemy
{
    public double smoothRenderX;
    public double smoothRenderY;
    public double smoothRenderZ;
    public HoundEntity(EntityType<? extends FlyingMob> entityType, Level world)
    {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createLivingAttributes()
    {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2f)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public boolean isInvisibleTo(Player player)
    {
        return super.isInvisibleTo(player) || !SightUtil.canPlayerSee(player);
    }

    @Override
    protected void registerGoals()
    {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
//        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, false, false, livingEntity -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));

//        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.addGoal(2, new HoundAttackGoal(this, 1.0));
        goalSelector.addGoal(2, new HoundMoveToTargetGoal(this, 2, false));
    }

    @Override
    protected PathNavigation createNavigation(Level world)
    {
        FlyingPathNavigation navigation1 = new FlyingPathNavigation(this, world)
        {
            public boolean isStableDestination(BlockPos pos)
            {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };

        navigation1.setCanOpenDoors(false);
        navigation1.setCanFloat(false);
        navigation1.setCanPassDoors(true);
        return navigation1;
    }

    @Override
    public boolean isNoGravity()
    {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return NMSounds.HOUND_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return NMSounds.HOUND_DEATH.get();
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return super.requiresCustomPersistence();
    }

    private class HoundMoveControl extends net.minecraft.world.entity.ai.control.MoveControl
    {
        public HoundMoveControl(Mob entity)
        {
            super(entity);
        }

        @Override
        public void tick()
        {
            super.tick();
        }
    }
}
