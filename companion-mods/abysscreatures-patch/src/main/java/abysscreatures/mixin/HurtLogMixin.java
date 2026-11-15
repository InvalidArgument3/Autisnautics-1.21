package abysscreatures.mixin;

import abysscreatures.ai.AbyssAiLog;
import abysscreatures.entity.WaterAbyssCreature;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class HurtLogMixin {
    @Inject(method = "hurt", at = @At("RETURN"))
    private void logAbyssDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> call) {
        if (!Boolean.TRUE.equals(call.getReturnValue())) {
            return;
        }
        Entity direct = source.getDirectEntity();
        Entity attacker = direct instanceof WaterAbyssCreature ? direct : source.getEntity();
        if (attacker instanceof WaterAbyssCreature mob) {
            AbyssAiLog.damage(mob, (LivingEntity) (Object) this, amount, source);
        }
    }
}
