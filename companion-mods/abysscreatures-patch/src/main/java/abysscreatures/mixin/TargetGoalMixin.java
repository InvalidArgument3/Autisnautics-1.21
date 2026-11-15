package abysscreatures.mixin;

import abysscreatures.ai.HuntGoal;
import abysscreatures.entity.WaterAbyssCreature;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The old player goals search a flat box and never require the player to be in the water. HuntGoal does that job. */
@Mixin(NearestAttackableTargetGoal.class)
public abstract class TargetGoalMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void replaced(CallbackInfoReturnable<Boolean> call) {
        if ((Object) this instanceof HuntGoal) {
            return;
        }
        if (((TargetMobAccess) (Object) this).abyss$mob() instanceof WaterAbyssCreature) {
            call.setReturnValue(false);
        }
    }
}
