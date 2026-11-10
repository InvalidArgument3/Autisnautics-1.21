package com.neep.neepmeat.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.neep.neepmeat.util.SightUtil;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractZombieRenderer.class)
public class ZombieBaseEntityRendererMixin
{
    @ModifyReturnValue(method = "getTexture(Lnet/minecraft/entity/mob/ZombieEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"))
    private ResourceLocation thing(ResourceLocation original, @Local(argsOnly = true) Zombie entity)
    {
        return SightUtil.getZombieTexture(original);
    }
}
