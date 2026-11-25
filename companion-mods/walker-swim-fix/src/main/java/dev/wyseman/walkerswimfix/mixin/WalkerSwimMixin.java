package dev.wyseman.walkerswimfix.mixin;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.walker.LegRuntime;
import dev.letitwalk.walker.WalkerSwim;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.wyseman.walkerswimfix.SwimToggleAccess;
import dev.wyseman.walkerswimfix.WalkerSwimFix;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = WalkerSwim.class, remap = false)
public abstract class WalkerSwimMixin {
    @Shadow
    static void stop(WalkerCoreBlockEntity core, List<LegRuntime> legs) {}

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void walkerswimfix$gateSwim(
        WalkerCoreBlockEntity core,
        ServerSubLevel sub,
        List<LegRuntime> legs,
        Pose3dc pose,
        Vec3 com,
        Vec3 vel,
        Vector3d angVel,
        long tick,
        CallbackInfoReturnable<Boolean> cir
    ) {
        WalkerSwimFix.recordSubLevel(core, sub);
        if (!((SwimToggleAccess) core).walkerswimfix$isSwimEnabled()) {
            if (core.swimming) {
                stop(core, legs);
            }
            cir.setReturnValue(false);
            return;
        }

        if (WalkerSwimFix.USE_MASS_DENSITY.get()) {
            double density = WalkerSwimFix.computeDensity(core, sub, tick);
            if (density > WalkerSwimFix.MAX_BUOYANT_DENSITY.get()) {
                if (core.swimming) {
                    stop(core, legs);
                }
                cir.setReturnValue(false);
            }
        }
    }
}
