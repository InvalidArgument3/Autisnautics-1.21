package com.neep.neepmeat.entity.path_legion;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.entity.PlayerAwareWhileSpawning;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.entity.HitScan;
import com.neep.meatweapons.entity.MeatgunProjectileEntity;
import com.neep.meatweapons.init.MWSounds;
import com.neep.meatweapons.meatgun.module.ShooterModule;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.entity.weapon_switching.*;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class PathSoldierEntity extends WeaponSwitchingHostileEntity implements PlayerAwareWhileSpawning
{
    protected final GunAttackGoal<PathSoldierEntity> rangedAttackGoal = new GunAttackGoal<>(this, () -> this.gun, 1.0, 15.0f);
    protected final BetterMeleeGoal meleeAttackGoal = new BetterMeleeGoal(this, 1.65f, false);

    private final ShotProvider gun = PISTOL;

    public PathSoldierEntity(EntityType<? extends WeaponSwitchingHostileEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(1, new SwitchWeaponGoal(this, 7));
//        goalSelector.add(1, new KeeperCritGoal(this, () -> this.meleeAttackGoal, 4));

        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(2, new ChargeGoal(this, 3));
//        goalSelector.add(4, new KeeperCritGoal(this, 8f));
//        goalSelector.add(3, new KeeperDodgeGoal(this, 8f));
//        goalSelector.add(3, new KeeperRetreatGoal(this, 8f, 16));
//        goalSelector.add(4, new KeeperHealGoal(this, 8f, 16));
    }

    @Override
    public void tick()
    {
        super.tick();
//        if (!getWorld().isClient())
//            System.out.println(goalSelector.getRunningGoals().map(g -> g.getGoal().toString() + ", ").collect(Collectors.joining()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        updateAttackType();
    }

    public void updateAttackType()
    {
        if (this.level() == null || this.level().isClientSide)
            return;

        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.rangedAttackGoal);

        if (getWeaponMode() == WeaponType.RANGED)
        {
            this.goalSelector.addGoal(4, this.rangedAttackGoal);
        }
        else
        {
            this.goalSelector.addGoal(4, this.meleeAttackGoal);
        }
    }

    @Override
    public float getSpeed()
    {
//        return getWeaponMode() == WeaponType.RANGED ? 0.2f : 0.25f;
//        return 0.25f;
        return super.getSpeed();
    }

    public static AttributeSupplier.Builder createLivingAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25f)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress)
    {
        if (getWeaponMode() == WeaponType.RANGED)
        {
            gun.fireBeam(level(), this, this, target, getMainHandItem());
        }
//        ItemStack itemStack = getStackInHand(Hand.MAIN_HAND);
//        if (!getWorld().isClient() && itemStack.isOf(MWItems.FUSION_CANNON))
//        {
//            ((FusionCannonItem) itemStack.getItem()).fireBeam(getWorld(), this, target, itemStack);
//        }
    }

    @Override
    public boolean doHurtTarget(Entity target)
    {
        return super.doHurtTarget(target);
    }

    @Override
    public boolean isValidSpawn(ServerLevel world, Player nearestPlayer, double squaredDistance)
    {
        return EnlightenmentManager.get(nearestPlayer).getTotal() > 10;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt)
    {
        var data = super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);

        var player = world.getNearestPlayer(this, 100);
        if (player != null)
        {
            float f1 = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            String name = "Player modifier";
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                    new AttributeModifier(name, -0.1f, AttributeModifier.Operation.MULTIPLY_BASE));
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                    new AttributeModifier(name, -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
            float f2 = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // Player-specific properties
            initEquipment(random, difficulty, player);
        }
        else
        {
            populateDefaultEquipmentSlots(random, difficulty);
        }

        return data;
    }

    protected void initEquipment(RandomSource random, DifficultyInstance localDifficulty, Player playerEntity)
    {
        super.populateDefaultEquipmentSlots(random, localDifficulty);
//        internalInventory.setStack(0, preferredRangedWeapon().getDefaultStack());
//        internalInventory.setStack(1, preferredMeleeWeapon().getDefaultStack());
    }

    // Player-unaware loadout
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty)
    {
        super.populateDefaultEquipmentSlots(random, localDifficulty);
        internalInventory.setItem(0, preferredRangedWeapon().getDefaultInstance());
        internalInventory.setItem(1, preferredMeleeWeapon().getDefaultInstance());
    }

    protected Item preferredRangedWeapon()
    {
        return MWItems.ALMOST_PISTOL;
    }

    protected Item preferredMeleeWeapon()
    {
        return MWItems.KNIFE;
//        return Items.NETHERITE_AXE;
    }

    private static final ShotProvider PISTOL = new ShotProvider()
    {
        @Override
        public void fireBeam(Level world, AccuracyAware accuracyAware, LivingEntity shooter, LivingEntity target, ItemStack currentItem)
        {
            var direction = target.getEyePosition().subtract(shooter.getEyePosition());
            var py = NMMaths.rectToPol(direction);
            var origin = ShooterModule.getFireOrigin(shooter, shooter.getRandom(), py.x * Mth.DEG_TO_RAD, py.y * Mth.DEG_TO_RAD,
                    8 * Mth.DEG_TO_RAD);
            var result = new HitScan(30)
                    .ricochets(1)
                    .maxPierce(0)
                    .beamEffect(this::syncBeamEffect)
                    .apply(world, shooter, origin, shooter.getRandom());

            boolean hit = false;
            for (var r : result.hits())
            {
                if (r.getEntity().equals(target))
                    hit = true;
                applyDamageEffects(world, shooter, r);
            }

            world.playSound(null, shooter, MWSounds.GUN_FIRE_0, SoundSource.PLAYERS, 2f,
                    (float) shooter.getRandom().triangle(1.0f, 0.05f));

            if (result.ricochet())
                world.playSound(null, shooter, NMSounds.BULLET_RICOCHET.get(), SoundSource.PLAYERS, 1f, (float) shooter.getRandom().triangle(0.9f, 0.4f));

            accuracyAware.addShot(hit);
        }

        @Override
        public int shotCooldown()
        {
            return 10;
        }

        @Override
        public int maxShots()
        {
            return 6;
        }

        public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
        {
            Vec3 col = new Vec3(214, 175, 32);
            for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
            {
                MWGraphicsEffects.syncBeamEffect(player, full ? MWGraphicsEffects.RICOCHET_BULLET_TRAIL : MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col, 0.1f, 1);
            }
        }

        private void applyDamageEffects(Level world, LivingEntity shooter, HitScan.HitScanResult result)
        {
            Entity entity = result.getEntity();
            float damage = 1;
            float f = MeatgunProjectileEntity.modifyDamage(world, damage, result, result.getDirection());
            boolean crit = f > 1;
            damage = f * damage;

            if (!(entity instanceof LivingEntity living && living.isDeadOrDying()))
            {
                boolean damaged = entity.hurt(BulletDamageSource.create(world, shooter, 0.1f, 20, crit, 10), damage);
            }

        }
    };

    public static boolean canSpawnInDark1(EntityType<? extends Monster> type, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(world, pos, random) && checkMobSpawnRules(type, world, spawnReason, pos, random);
    }

    // Run at the target, causing weapons to switch
    public static class ChargeGoal extends Goal
    {
        private final WeaponSwitchingHostileEntity entity;
        private final float speed;

        protected int activeTicks = 0;

        public ChargeGoal(WeaponSwitchingHostileEntity entity, float speed)
        {
            this.entity = entity;
            this.speed = speed;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse()
        {
            return entity.getTarget() != null
                    && entity.getWeaponMode() == WeaponType.RANGED
                    && entity.accuracy.get() <= 0.25f;
//                    && entity.getRandom().nextInt(10) == 0;
        }

        @Override
        public boolean isInterruptable()
        {
            return activeTicks > adjustedTickDelay(60)
                    || entity.distanceTo(entity.getTarget()) < 7;
        }

        @Override
        public void stop()
        {
            super.stop();
            entity.accuracy.reset();
        }

        @Override
        public void tick()
        {
            super.tick();
            var livingEntity = entity.getTarget();
            entity.switchWeapon(WeaponType.MELEE);

            if (entity.getSensing().hasLineOfSight(livingEntity))
            {
                entity.getNavigation().moveTo(livingEntity, speed);
            }

            activeTicks += 1;
        }
    }
}
