package abysscreatures.mixin;

import abysscreatures.ai.HuntGoal;
import abysscreatures.entity.SpineWormHeadEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The worm does not call the shared goal setup. It still needs the water hunt. The anchor and the reach attack stay. */
@Mixin(SpineWormHeadEntity.class)
public class SpineWormMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void hunt(CallbackInfo call) {
        SpineWormHeadEntity self = (SpineWormHeadEntity) (Object) this;
        self.targetSelector.addGoal(0, new HuntGoal(self));
    }
}
