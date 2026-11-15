package abysscreatures.mixin;

import abysscreatures.ai.DirectSwimNavigation;
import abysscreatures.entity.NecroshellEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Necroshell replaces the shared swim navigation with a ground navigator, which cannot leave the seabed. */
@Mixin(NecroshellEntity.class)
public class NecroshellNavigationMixin {
    @Inject(method = "createNavigation", at = @At("HEAD"), cancellable = true)
    private void swim(Level level, CallbackInfoReturnable<PathNavigation> call) {
        call.setReturnValue(new DirectSwimNavigation((Mob) (Object) this, level));
    }
}
