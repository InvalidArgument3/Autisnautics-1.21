package com.neep.meatweapons.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.neep.meatweapons.client.network.MeatgunClient;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin
{
    @ModifyReturnValue(method = "getFovMultiplier", at = @At("RETURN"))
    float thing(float prevValue)
    {
        return MeatgunClient.modifyFov(prevValue);
    }
}
