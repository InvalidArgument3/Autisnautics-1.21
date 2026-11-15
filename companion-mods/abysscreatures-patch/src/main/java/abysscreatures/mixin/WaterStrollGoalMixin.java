package abysscreatures.mixin;

import abysscreatures.ai.RoamPoint;
import abysscreatures.ai.WaterStrollGoal;
import abysscreatures.entity.WaterAbyssCreature;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Retarget from the mob's current position. RandomStrollGoal stops choosing points once noActionTime passes 100, and a stuck node then never moves. */
@Mixin(WaterStrollGoal.class)
public abstract class WaterStrollGoalMixin {
    @Shadow
    private WaterAbyssCreature fish;

    @Unique
    private int abyss$age;

    @Unique
    private boolean abyss$force = true;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void roam(CallbackInfoReturnable<Boolean> call) {
        if (!this.fish.canWander() || !this.fish.isInWater() || this.fish.getTarget() != null) {
            call.setReturnValue(false);
            return;
        }
        StrollAccess stroll = (StrollAccess) (Object) this;
        if (!this.abyss$force && stroll.abyss$mob().getRandom().nextInt(25) != 0) {
            call.setReturnValue(false);
            return;
        }
        Vec3 pos = RoamPoint.pick(stroll.abyss$mob(), 6.0, 14.0);
        stroll.abyss$wantedX(pos.x);
        stroll.abyss$wantedY(pos.y);
        stroll.abyss$wantedZ(pos.z);
        this.abyss$age = 0;
        this.abyss$force = false;
        call.setReturnValue(true);
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void giveUpStuckPoint(CallbackInfoReturnable<Boolean> call) {
        if (!this.fish.canWander() || !this.fish.isInWater() || this.fish.getTarget() != null) {
            this.abyss$force = true;
            call.setReturnValue(false);
            return;
        }
        this.abyss$age++;
        if (this.abyss$age > 70) {
            this.abyss$force = true;
            call.setReturnValue(false);
        }
    }
}
