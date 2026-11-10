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

package com.neep.neepmeat.mixin;

import com.neep.neepmeat.implant.entity.ClawsImplant;
import com.neep.neepmeat.implant.entity.ExplosiveImplant;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity
{
    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (ExplosiveImplant.onInteract(player, hand, this, stack, random))
            cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "tryAttack", at = @At("HEAD"))
    private void onTryAttack(Entity target, CallbackInfoReturnable<Boolean> cir)
    {
        ClawsImplant.onTryAttack(this, target);
    }
}
