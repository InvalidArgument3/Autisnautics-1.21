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

import com.neep.meatlib.mixin.EntityMixin;
import com.neep.neepbus.input.InputInterceptor;
import com.neep.neepbus.input.InputInterceptorPlayer;
import com.neep.neepmeat.block.ShortBromeBlock;
import com.neep.neepmeat.implant.entity.CatEarsImplant;
import com.neep.neepmeat.implant.entity.ImplantRenderingHelper;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.implant.entity.SpiderLegsImplant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends EntityMixin implements ImplantRenderingHelper, InputInterceptorPlayer
{
    @Shadow
    @Final
    private Inventory inventory;
    // So that I don't have to do a hash lookup multiple times every tick
    @Unique @Nullable private SpiderLegsImplant spiderLegsImplant;
    @Unique @Nullable private CatEarsImplant catEars;

    @Unique @Nullable private InputInterceptor interceptor;

//    @Inject(method = "initDataTracker", at = @At("TAIL"))
//    void onInitDataTracker(CallbackInfo ci)
//    {
//        dataTracker.startTracking(SpiderLegsImplant.MEAN_FOOT_HEIGHT, 0.0);
//    }

    @Inject(method = "jump", at = @At("HEAD"))
    void onJump(CallbackInfo ci)
    {
        SpiderLegsImplant implant = PlayerImplantManager.get((Player) (Object) this).getImplant(SpiderLegsImplant.ID);
        if (implant != null)
        {
            implant.onJump();
        }
    }

    @Override
    public void neepmeat$setSpiderLegs(SpiderLegsImplant implant)
    {
        this.spiderLegsImplant = implant;

    }

    @Override
    public SpiderLegsImplant neepmeat$getSpiderLegs()
    {
        return spiderLegsImplant;
    }

    @Override
    public void neepmeat$setCatEars(CatEarsImplant catEars)
    {
        this.catEars = catEars;
    }

    @Override
    public CatEarsImplant neepmeat$getCatEars()
    {
        return catEars;
    }

    // Alter the raycast origin when spider legs are in use

    // Not needed anymore, superseded by onGetStandingEyeHeight
//    @Inject(method = "getActiveEyeHeight", at = @At("RETURN"), cancellable = true)
//    void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir)
//    {
//        // This runs in the constructor, before components are accessible.
//        // That's why the boolean flag is necessary. It'll be set to true in the tick loop by the implant if present.
//        if (spiderLegsImplant != null && (pose == EntityPose.CROUCHING || pose == EntityPose.STANDING))
//        {
//            cir.setReturnValue(cir.getReturnValueF() + spiderLegsImplant.modifyEyeHeight(((PlayerEntity) (Object) this)));
//        }
//    }

    @Override
    protected float onGetStandingEyeHeight(float original)
    {
        if (spiderLegsImplant != null)
        {
            return original + spiderLegsImplant.modifyEyeHeight(((Player) (Object) this));
        }

        return original;
    }

    @Override
    protected double onGetEyeY(double original)
    {
        if (spiderLegsImplant != null)
        {
            return original + spiderLegsImplant.modifyEyeHeight(((Player) (Object) this));

        }

        return original;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onEndTick(CallbackInfo ci)
    {
    }

    @Override
    public @Nullable InputInterceptor neepmeat$getInterceptor()
    {
        return interceptor;
    }

    @Override
    public void neepmeat$etInterceptor(@Nullable InputInterceptor inputInterceptor)
    {
        this.interceptor = inputInterceptor;
    }
}
