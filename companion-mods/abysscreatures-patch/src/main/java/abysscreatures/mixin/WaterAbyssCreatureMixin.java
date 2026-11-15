package abysscreatures.mixin;

import abysscreatures.ai.AbyssAiLog;
import abysscreatures.ai.DirectSwimNavigation;
import abysscreatures.ai.HuntGoal;
import abysscreatures.ai.SwimPursueGoal;
import abysscreatures.entity.ChainFish;
import abysscreatures.entity.CorpseAnglerEntity;
import abysscreatures.entity.FulgastraEntity;
import abysscreatures.entity.GhidruthEntity;
import abysscreatures.entity.GnasherEntity;
import abysscreatures.entity.MutavoreEntity;
import abysscreatures.entity.NecroshellEntity;
import abysscreatures.entity.SiamserpentHeadEntity;
import abysscreatures.entity.SpineWormHeadEntity;
import abysscreatures.entity.WaterAbyssCreature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterAbyssCreature.class)
public abstract class WaterAbyssCreatureMixin {
    @Inject(method = "createNavigation", at = @At("HEAD"), cancellable = true)
    private void directSwim(Level level, CallbackInfoReturnable<PathNavigation> call) {
        call.setReturnValue(new DirectSwimNavigation((Mob) (Object) this, level));
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void huntAndPursue(CallbackInfo call) {
        WaterAbyssCreature self = (WaterAbyssCreature) (Object) this;
        if (!hunts(self)) {
            return;
        }
        if (self instanceof ChainFish chain && !chain.isLead()) {
            return;
        }
        self.targetSelector.addGoal(0, new HuntGoal(self));
        if (!(self instanceof SpineWormHeadEntity)) {
            self.goalSelector.addGoal(4, new SwimPursueGoal(self));
        }
    }

    @Inject(method = "setAnimationState", at = @At("HEAD"))
    private void logAnimation(int state, CallbackInfo call) {
        AbyssAiLog.animation((WaterAbyssCreature) (Object) this, state);
    }

    private static boolean hunts(WaterAbyssCreature fish) {
        return fish instanceof GnasherEntity
                || fish instanceof CorpseAnglerEntity
                || fish instanceof NecroshellEntity
                || fish instanceof FulgastraEntity
                || fish instanceof MutavoreEntity
                || fish instanceof SiamserpentHeadEntity
                || fish instanceof GhidruthEntity
                || fish instanceof SpineWormHeadEntity;
    }
}
