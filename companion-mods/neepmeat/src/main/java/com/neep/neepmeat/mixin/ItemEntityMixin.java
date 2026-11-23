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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.neep.meatweapons.damage.MWDamageSources;
import com.neep.neepmeat.init.NMItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.neep.neepmeat.entity.skyworm.SkywormEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
    public ItemEntityMixin(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Shadow public abstract @Nullable Entity getOwner();

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    boolean modifyInvulnerable(ItemEntity instance, DamageSource damageSource, Operation<Boolean> original)
    {
        return original.call(instance, damageSource) || damageSource.is(MWDamageSources.IGNORES_ITEMS);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    void onOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        if (getStack().is(NMItems.REANIMATED_HEART) && source.is(DamageTypes.LAVA))
        {
            SkywormEntity.spawnEvent((ItemEntity) (Object) this, level(), blockPosition(), getOwner());
        }
    }
}
