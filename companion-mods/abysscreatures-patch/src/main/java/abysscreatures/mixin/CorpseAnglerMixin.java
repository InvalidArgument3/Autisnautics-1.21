package abysscreatures.mixin;

import abysscreatures.ai.Bait;
import abysscreatures.entity.CorpseAnglerEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Buried angler stays on the seabed until the lure is hit or a survival player reaches it.
 * Any other hit still deals damage and leaves the burrow set.
 */
@Mixin(CorpseAnglerEntity.class)
public abstract class CorpseAnglerMixin {
    @Shadow
    private static EntityDataAccessor<Boolean> BURROW;

    @Shadow
    public abstract boolean isBurrow();

    @Unique
    private Vec3 abyss$home;

    @Unique
    private boolean abyss$keepBurrow;

    @Inject(method = "tick", at = @At("TAIL"))
    private void holdAndAmbush(CallbackInfo call) {
        CorpseAnglerEntity self = (CorpseAnglerEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        if (!this.isBurrow()) {
            this.abyss$home = null;
            return;
        }
        if (this.abyss$home == null) {
            this.abyss$home = self.position();
        }
        self.setDeltaMovement(Vec3.ZERO);
        self.setPos(this.abyss$home.x, this.abyss$home.y, this.abyss$home.z);
        self.getNavigation().stop();
        if (self.tickCount % 5 != 0) {
            return;
        }
        for (Player player : self.level().getEntitiesOfClass(Player.class, Bait.box(self).inflate(Bait.AMBUSH))) {
            if (Bait.ambush(self, player)) {
                this.abyss$release(player);
                return;
            }
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void lureOrStay(DamageSource source, float amount, CallbackInfoReturnable<Boolean> call) {
        CorpseAnglerEntity self = (CorpseAnglerEntity) (Object) this;
        this.abyss$keepBurrow = false;
        if (!this.isBurrow()) {
            return;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Player player && Bait.hits(self, player)) {
            self.setTarget(player);
            return;
        }
        this.abyss$keepBurrow = true;
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void restoreBurrow(DamageSource source, float amount, CallbackInfoReturnable<Boolean> call) {
        if (!this.abyss$keepBurrow) {
            return;
        }
        this.abyss$keepBurrow = false;
        CorpseAnglerEntity self = (CorpseAnglerEntity) (Object) this;
        ((EntityDataAccess) self).abyss$data().set(BURROW, true);
        self.setAnimationState(0);
        self.setAnimationTick(0);
    }

    @Unique
    private void abyss$release(Player player) {
        CorpseAnglerEntity self = (CorpseAnglerEntity) (Object) this;
        ((EntityDataAccess) self).abyss$data().set(BURROW, false);
        self.setAnimationState(3);
        self.setAnimationTick(15);
        self.setTarget(player);
        this.abyss$home = null;
    }
}
