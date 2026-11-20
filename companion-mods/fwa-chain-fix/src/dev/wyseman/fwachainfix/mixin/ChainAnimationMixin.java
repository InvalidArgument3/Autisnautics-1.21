package dev.wyseman.fwachainfix.mixin;

import dev.wyseman.fwachainfix.ChainColumn;
import fr.madu59.fwa.anims.ChainAnimation;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps one rendering end per vertical chain.
 *
 * <p>FWA stores a {@code ChainAnimation} on every chain block so the original
 * model stays hidden. Only the end should tick into {@code update()}, which
 * allocates a column the size of every block above it. A link with a vertical
 * chain below it is not that end: {@code setLast(false)} and skip the tick.
 * A block that is no longer a vertical chain is marked for removal. An entity
 * inside the column asks the end to rebuild, which is the same hook a block
 * update uses.
 */
@Mixin(value = ChainAnimation.class, remap = false)
public abstract class ChainAnimationMixin extends ChainAnimation {
    protected ChainAnimationMixin(
        net.minecraft.core.BlockPos position,
        double startTick,
        boolean oldIsOpen,
        boolean newIsOpen,
        net.minecraft.world.level.block.state.BlockState oldState,
        net.minecraft.world.level.block.state.BlockState newState
    ) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void fwachainfix$oneEndPerChain(double nowTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        net.minecraft.core.BlockPos pos = this.getPos();
        if (!SwingingBlockHelper.isVerticalChain(minecraft.level.getBlockState(pos))) {
            this.markForRemoval();
            ci.cancel();
            return;
        }
        if (SwingingBlockHelper.isVerticalChain(minecraft.level.getBlockState(pos.below()))) {
            this.setLast(false);
            ci.cancel();
            return;
        }
        // The wind sway does not rest. This only matters when the sampled swing
        // is already ~0, so a touch can mark the column dirty without scanning
        // every swinging chain.
        if (this.isLast() && ChainColumn.resting(this.tiltX, this.tiltZ, this.spin) && (int) nowTick % 20 == 0) {
            AABB box = this.getBoundingBox();
            if (box != null && !minecraft.level.getEntities((Entity) null, box.inflate(0.25), entity -> true).isEmpty()) {
                this.needUpdate();
            }
        }
    }
}
