package com.neep.neepmeat.entity.weapon_switching;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// Describes the effects of a ranged weapon that an entity can use
public interface ShotProvider
{
    void fireBeam(Level world, AccuracyAware accuracyAware, LivingEntity shooter, LivingEntity target, ItemStack currentItem);

    int shotCooldown();

    int maxShots();
}
