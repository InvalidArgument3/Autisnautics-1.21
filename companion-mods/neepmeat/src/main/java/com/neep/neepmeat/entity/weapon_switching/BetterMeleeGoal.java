package com.neep.neepmeat.entity.weapon_switching;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class BetterMeleeGoal extends MeleeAttackGoal
{
    private int critTimer;

    public BetterMeleeGoal(PathfinderMob mob, double speed, boolean pauseWhenMobIdle)
    {
        super(mob, speed, pauseWhenMobIdle);
    }

    @Override
    public double getAttackReachSqr(LivingEntity entity)
    {
        return super.getAttackReachSqr(entity);
    }

    @Override
    public boolean canContinueToUse()
    {
        LivingEntity livingEntity = this.mob.getTarget();

//        boolean inWalkRange = this.mob.isInWalkTargetRange(livingEntity.getBlockPos());
//        boolean other = (!(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity) livingEntity).isCreative());
//        boolean navigationNotIdle = !this.mob.getNavigation().isIdle(); // Stop if navigation is idle

//        boolean ret = true;
//        if (livingEntity == null) {
//            ret = false;
//        } else if (!livingEntity.isAlive()) {
//            ret = false;
//        } else if (paus) {
//            ret = navigationNotIdle;
//        } else {
//            ret = !this.mob.isInWalkTargetRange(livingEntity.getBlockPos())
//                    ? false
//                    : !(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity)livingEntity).isCreative();
//        }
//
//        if (ret == false)
//            System.out.println("FALSE");
//        return ret;
        return super.canContinueToUse();
    }

    public void scheduleCrit(int ticks)
    {
        // Goal runs every tick
        critTimer = ticks;
    }

    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public void tick()
    {
        super.tick();
        critTimer--;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target, double squaredDistance)
    {
        double d = this.getAttackReachSqr(target);

        if (squaredDistance <= d && this.getTicksUntilNextAttack() <= 0 && (critTimer <= 0 || mob.getDeltaMovement().y < 0))
//        if (squaredDistance <= d && this.getCooldown() <= 0 && (critTimer > 0 && mob.getVelocity().y < -0.1))
        {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }
}
