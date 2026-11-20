package dev.wyseman.fwachainfix.mixin;

import dev.wyseman.fwachainfix.ChainColumn;
import dev.wyseman.fwachainfix.ColumnLinks;
import fr.madu59.fwa.anims.ChainAnimation;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps one rendering end per column of chains and hanging lanterns.
 *
 * <p>FWA stores a {@code ChainAnimation} on every chain block so the original
 * model stays hidden. Only the lowest link should tick into {@code update()}.
 * A chain with a chain or hanging lantern below it is not that end. A block
 * that is no longer a vertical chain is marked for removal. An entity inside
 * a settled column asks the end to rebuild, which is the same hook a block
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
        BlockPos pos = this.getPos();
        if (!SwingingBlockHelper.isVerticalChain(minecraft.level.getBlockState(pos))) {
            this.markForRemoval();
            ci.cancel();
            return;
        }
        ChainColumn.Link below = ColumnLinks.of(minecraft.level.getBlockState(pos.below()));
        if (!ChainColumn.isRenderingEnd(ChainColumn.Link.CHAIN, below)) {
            this.setLast(false);
            ci.cancel();
            return;
        }
        this.setLast(true);
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
