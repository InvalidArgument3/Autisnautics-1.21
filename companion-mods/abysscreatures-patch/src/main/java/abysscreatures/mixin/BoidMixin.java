package abysscreatures.mixin;

import abysscreatures.ai.Boid;
import abysscreatures.ai.SchoolRoam;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Schooling keeps the 0.2.3 heading gate. The seek point is the player, or a shared point that moves. */
@Mixin(Boid.class)
public class BoidMixin {
    @Shadow
    public Mob mob;

    @Shadow
    public Vec3 target;

    @Inject(method = "update", at = @At("HEAD"))
    private void seek(float flockRadius, float maxVelocity, CallbackInfo call) {
        LivingEntity living = this.mob.getTarget();
        if (living != null && living.isAlive()) {
            this.target = living.position();
            return;
        }
        this.target = SchoolRoam.point(this.mob);
    }
}
