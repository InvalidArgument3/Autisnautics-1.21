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

import com.mojang.authlib.GameProfile;
import com.neep.neepmeat.block.ShortBromeBlock;
import com.neep.neepmeat.entity.effect.NMStatusEffects;
import com.neep.neepmeat.implant.entity.ClawsImplant;
import com.neep.neepmeat.implant.entity.EntityImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.EssentialSaltesItem;
import com.neep.neepmeat.item.RolledNewspaperItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player
{
    @Shadow public abstract void onLeaveCombat();

    @Shadow public abstract void attack(Entity target);

    public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile)
    {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci)
    {
        var implantManager = NMComponents.IMPLANT_MANAGER.getNullable(this);
        if (implantManager instanceof EntityImplantManager entityImplantManager)
        {
            entityImplantManager.onDeath();
        }

        ShortBromeBlock.onPlayerDeath((Player) (Object) this);
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci)
    {
//        if (RolledNewspaperItem.isHolding(getMainHandStack()))
//        {
//            RolledNewspaperItem.attackEntity(target, (ServerPlayerEntity) (Object)  this);
//        }
        ClawsImplant.onTryAttack(this, target);
    }
}
