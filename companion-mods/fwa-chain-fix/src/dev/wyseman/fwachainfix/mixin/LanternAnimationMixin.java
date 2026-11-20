package dev.wyseman.fwachainfix.mixin;

import dev.wyseman.fwachainfix.ChainColumn;
import dev.wyseman.fwachainfix.ColumnLinks;
import fr.madu59.fwa.anims.LanternAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code LanternAnimation.isLast} is hardcoded true, so every lantern renders
 * and each one builds the chain run above it. This returns the real column
 * end instead: the lowest hanging lantern or chain. A lantern that is no
 * longer hanging (broken, or placed standing) is marked for removal.
 */
@Mixin(value = LanternAnimation.class, remap = false)
public abstract class LanternAnimationMixin extends LanternAnimation {
    protected LanternAnimationMixin(
        BlockPos position,
        double startTick,
        boolean oldIsOpen,
        boolean newIsOpen,
        BlockState oldState,
        BlockState newState
    ) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
    }

    @Inject(method = "isLast", at = @At("HEAD"), cancellable = true, remap = false)
    private void fwachainfix$geometricEnd(CallbackInfoReturnable<Boolean> ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ci.setReturnValue(false);
            return;
        }
        BlockPos pos = this.getPos();
        ChainColumn.Link self = ColumnLinks.of(minecraft.level.getBlockState(pos));
        ChainColumn.Link below = ColumnLinks.of(minecraft.level.getBlockState(pos.below()));
        ci.setReturnValue(ChainColumn.isRenderingEnd(self, below));
    }

    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void fwachainfix$oneEndPerLantern(double nowTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        BlockPos pos = this.getPos();
        BlockState state = minecraft.level.getBlockState(pos);
        ChainColumn.Link self = ColumnLinks.of(state);
        if (self != ChainColumn.Link.HANGING_LANTERN && self != ChainColumn.Link.SOUL_LANTERN) {
            this.markForRemoval();
            return;
        }
        ChainColumn.Link below = ColumnLinks.of(minecraft.level.getBlockState(pos.below()));
        if (!ChainColumn.isRenderingEnd(self, below)) {
            this.setLast(false);
            return;
        }
        this.setLast(true);
        if (ChainColumn.resting(this.tiltX, this.tiltZ, this.spin) && (int) nowTick % 20 == 0) {
            AABB box = this.getBoundingBox();
            if (box != null && !minecraft.level.getEntities((Entity) null, box.inflate(0.25), entity -> true).isEmpty()) {
                this.needUpdate();
            }
        }
    }
}
