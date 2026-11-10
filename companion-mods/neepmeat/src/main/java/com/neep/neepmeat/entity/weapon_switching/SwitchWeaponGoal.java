package com.neep.neepmeat.entity.weapon_switching;

import com.neep.meatweapons.item.BaseGunItem;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

public class SwitchWeaponGoal extends Goal
{
    protected final WeaponSwitchingHostileEntity entity;
    protected float meleeRange;

    public SwitchWeaponGoal(WeaponSwitchingHostileEntity entity, float meleeRange)
    {
        this.entity = entity;
        this.meleeRange = meleeRange;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        boolean hasTarget = entity.getTarget() != null;
        var currentMode = entity.getWeaponMode();
        boolean inMeleeRange = hasTarget && entity.closerThan(entity.getTarget(), meleeRange);

        if (!hasTarget)
        {
            entity.switchWeapon(WeaponSwitchingHostileEntity.WeaponType.NONE);
        }
        else if (inMeleeRange && currentMode != WeaponSwitchingHostileEntity.WeaponType.MELEE  && entity.hasMeleeStored())
        {
            entity.switchWeapon(WeaponSwitchingHostileEntity.WeaponType.MELEE);
        }
        else if (!inMeleeRange && currentMode != WeaponSwitchingHostileEntity.WeaponType.RANGED && entity.hasRangedStored())
        {
            entity.switchWeapon(WeaponSwitchingHostileEntity.WeaponType.RANGED);
        }

        return false;
    }

    @Override
    public void tick()
    {

    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
