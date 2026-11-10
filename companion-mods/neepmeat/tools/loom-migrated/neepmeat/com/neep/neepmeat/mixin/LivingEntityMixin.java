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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.entity.effect.NMStatusEffects;
import com.neep.neepmeat.implant.entity.*;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.interfaces.ILivingEntity;
import com.neep.neepmeat.item.EssentialSaltesItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
    public LivingEntityMixin(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Shadow public abstract boolean hasStatusEffect(MobEffect effect);

    @Shadow protected abstract void playBlockFallSound();

    @Shadow public abstract RandomSource getRandom();

    @Shadow public int hurtTime;

    @Shadow protected abstract void playHurtSound(DamageSource source);

    @Shadow protected abstract void playEquipmentBreakEffects(ItemStack stack);

    @Shadow protected abstract void markEffectsDirty();

    @Shadow protected abstract float modifyAppliedDamage(DamageSource source, float amount);

    @Shadow public abstract boolean isMobOrPlayer();

    @Shadow public abstract void endCombat();

    @Shadow protected abstract boolean isImmobile();

    @Shadow
    private Optional<BlockPos> climbingPos;

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci)
    {
//        if (hasStatusEffect(NMStatusEffects.ASH_PEPARATION) && source.isIn(DamageTypeTags.IS_FIRE))
        if (hasStatusEffect(NMStatusEffects.ASH_PEPARATION) && !level().isClientSide())
        {
            EssentialSaltesItem.onEntityDeath((LivingEntity) (Object) this);
        }

        var implantManager = NMComponents.IMPLANT_MANAGER.getNullable(this);
        if (implantManager instanceof EntityImplantManager entityImplantManager)
        {
            entityImplantManager.onDeath();
        }
    }

    @Inject(method = "getNextAirUnderwater", at = @At(value = "RETURN"), cancellable = true)
    public void onGetNextAirUnderwater(int air, CallbackInfoReturnable<Integer> cir)
    {
        if (((Object) this) instanceof Player player)
        {
            ImplantManager manager = player.getComponent(NMComponents.IMPLANT_MANAGER);
            if (manager.getInstalled().contains(LungExtensionsImplant.ID))
            {
                int nextAir = cir.getReturnValueI();
                int rand = getRandom().nextInt(3);
                cir.setReturnValue(rand > 0 ? air : nextAir);
            }
        }
    }

    @ModifyReturnValue(method = "getJumpBoostVelocityModifier", at = @At("TAIL"))
    protected float thing(float original)
    {
        if ((Object) this instanceof Player playerEntity)
        {
            PlayerImplantManager manager = PlayerImplantManager.get(playerEntity);
            PoweredKneesImplant implant = manager.getImplant(PoweredKneesImplant.ID);
            if (implant != null)
                return implant.modifyJumpBoost(original);
        }
        return original;
    }

//    @Inject(method = "applyEnchantmentsToDamage", at = @At(value = "TAIL"), cancellable = true)
//    public void applyEnchantments(DamageSource source, float amount, CallbackInfoReturnable<Float> cir)
//    {
//        if ((LivingEntity) (Object) this instanceof PlayerEntity player)
//        {
//            PlayerUpgradeManager upgradeManager = PlayerUpgradeManager.get(player);
//
//            // Get total protection from enchantments and upgrades
//            float protection = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source)
//                    + upgradeManager.getProtectionAmount(source, amount);
//            float newAmount = DamageUtil.getInflictedDamage(amount, protection + protection);
//
//            // Replace the vanilla protection amount with this one. This may conflict with other mods. Sorry.
//            cir.setReturnValue(newAmount);
//            cir.cancel();
//        }
//    }

//    @WrapOperation(method = "isClimbing", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
//    boolean applyPipeLadder(BlockState instance, TagKey<?> tagKey, Operation<Boolean> original)
//    {
//        return original.call(instance, tagKey) || instance.isIn(NMTags.PIPE_CLIMBABLE);
//    }

    @Inject(method = "isClimbing", at = @At("HEAD"), cancellable = true)
    void applyPipeLadder(CallbackInfoReturnable<Boolean> cir)
    {
        if (!this.isSpectator())
        {
            if ((Object) this instanceof Player playerEntity)
            {
                if (PlayerImplantManager.get(playerEntity).isInstalled(PipeClimbImplant.ID))
                {
                    var pos = PipeClimbImplant.applyPipeClimb(playerEntity);
                    if (pos.isPresent())
                    {
                        this.climbingPos = pos;
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    // The `vanillaMinecraftLadderBlock` here is literally `Blocks.LADDER`, not a useful parameter.
    @WrapOperation(method = "canEnterTrapdoor", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    boolean climbThroughTrapdoors(BlockState instance, Block vanillaMinecraftLadderBlock, Operation<Boolean> original)
    {
        return original.call(instance, vanillaMinecraftLadderBlock) || instance.is(NMTags.LADDERS);
    }
}
