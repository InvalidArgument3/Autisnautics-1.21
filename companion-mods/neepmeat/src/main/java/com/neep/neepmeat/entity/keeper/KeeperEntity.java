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

package com.neep.neepmeat.entity.keeper;

import net.minecraft.core.HolderLookup;

import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.item.FusionCannonItem;
import com.neep.neepmeat.entity.weapon_switching.BetterMeleeGoal;
import com.neep.neepmeat.entity.weapon_switching.SwitchWeaponGoal;
import com.neep.neepmeat.entity.weapon_switching.WeaponSwitchingHostileEntity;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

public class KeeperEntity extends WeaponSwitchingHostileEntity
{
    private final ServerBossEvent bossBar = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    protected final KeeperRangedAttackGoal<KeeperEntity> rangedAttackGoal = new KeeperRangedAttackGoal<>(this, 1.0, 20, 15.0f, MWItems.FUSION_CANNON);
    protected final BetterMeleeGoal meleeAttackGoal = new BetterMeleeGoal(this, 2.2, false){

    };
    protected ItemStack equipped = new ItemStack(MWItems.FUSION_CANNON);

//    protected GoalSelector targetSelector = new GoalSelector(world.getProfilerSupplier());
//    protected GoalSelector goalSelector = new GoalSelector(world.getProfilerSupplier());

    public KeeperEntity(EntityType<? extends Monster> entityType, Level world)
    {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createLivingAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2f)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public float getSpeed()
    {
        return 0.2f;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt)
    {
        updateAttackType();
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void registerGoals()
    {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(1, new SwitchWeaponGoal(this, 7));

        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(4, new KeeperCritGoal(this, () -> this.meleeAttackGoal, 8f));
        goalSelector.addGoal(3, new KeeperDodgeGoal(this, 8f));
        goalSelector.addGoal(3, new KeeperRetreatGoal(this, 8f, 16));
        goalSelector.addGoal(4, new KeeperHealGoal(this, 8f, 16));
    }

    public boolean shouldHeal()
    {
        return getHealth() < 0.75 * getMaxHealth();
    }

    public void updateAttackType()
    {
        if (this.level() == null || this.level().isClientSide) return;

        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.rangedAttackGoal);

        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, MWItems.FUSION_CANNON));
        if (itemStack.is(MWItems.FUSION_CANNON))
        {
            int i = 20;
            this.rangedAttackGoal.setAttackInterval(i);
            this.goalSelector.addGoal(4, this.rangedAttackGoal);
        }
        else
        {
            this.goalSelector.addGoal(4, this.meleeAttackGoal);
        }
    }

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    boolean isPlayerStaring(LivingEntity entity)
    {
        HitResult result = entity.pick(10, 0.5f, false);
        Optional<Vec3> entityHit = getBoundingBox().clip(entity.getEyePosition(0.5f), result.getLocation());

        return entityHit.isPresent();

//        Vec3d vec3d = entity.getRotationVec(1.0f).normalize();
//        Vec3d vec3d2 = new Vec3d(this.getX() - entity.getX(), this.getEyeY() - entity.getEyeY(), this.getZ() - entity.getZ());
//        double distance = vec3d2.length();
//        double e = vec3d.dotProduct(vec3d2.normalize());
//        if (e > 1.0 - 0.025 / distance)
//        {
//            return entity.canSee(this);
//        }
//        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player)
    {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        return super.saveWithoutId(nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        updateAttackType();
    }

    @Override
    public float getHealth()
    {
        return super.getHealth();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.MAINHAND ? equipped : ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack)
    {
        if (slot == EquipmentSlot.MAINHAND)
            equipped = stack;
        if (!level().isClientSide()) updateAttackType();
    }

    @Override
    public HumanoidArm getMainArm()
    {
        return HumanoidArm.LEFT;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress)
    {
        ItemStack itemStack = getItemInHand(InteractionHand.MAIN_HAND);
        if (!level().isClientSide() && itemStack.is(MWItems.FUSION_CANNON))
        {
            ((FusionCannonItem) itemStack.getItem()).fireBeam(level(), this, target, itemStack);
        }
    }
}
