package dev.wyseman.fwachainfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.wyseman.fwachainfix.ChainColumn;
import fr.madu59.fwa.anims.AbstractChainAnimation;
import fr.madu59.fwa.rendering.AnimationRenderer;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops a chain end from allocating a column it is not going to draw, and
 * flushes that column's quads before the next chain (or the next portal pass)
 * appends more.
 *
 * <p>{@code update()} always allocates {@code poses}, {@code states},
 * {@code quads} and {@code lightValues} at {@code chainCount}, and
 * {@code ChainAnimation.tick} calls it whenever {@code isLast} is still null.
 * After {@code updateChainCount} has decided this link is not the rendering
 * end, the rest of that allocation is skipped.
 *
 * <p>{@code render} line 185 is the pose rebuild ({@code PoseStack.mulPose}).
 * The quads it is about to submit stay in {@code AnimationRenderer.buffers}
 * until the level pass returns. Flushing at the end of this chain drops them
 * immediately. A settled swing reuses the poses already stored on the chain.
 */
@Mixin(value = AbstractChainAnimation.class, remap = false)
public abstract class AbstractChainAnimationMixin {
    @Shadow
    protected boolean updatePoses;

    @Shadow
    protected boolean needUpdate;

    @Shadow
    protected float tiltX;

    @Shadow
    protected float tiltZ;

    @Shadow
    protected float spin;

    @Shadow
    protected PoseStack.Pose[] poses;

    @Shadow
    public abstract boolean isLast();

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lfr/madu59/fwa/anims/AbstractChainAnimation;updateChainCount()V",
            shift = At.Shift.AFTER
        ),
        cancellable = true,
        remap = false
    )
    private void fwachainfix$skipColumnAllocation(CallbackInfo ci) {
        if (!this.isLast()) {
            this.needUpdate = false;
            ci.cancel();
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lfr/madu59/fwa/anims/AbstractChainAnimation;updateCache(J)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private void fwachainfix$reuseSettledPoses(AnimationRenderingContext context, CallbackInfo ci) {
        if (this.poses != null
            && !this.needUpdate
            && ChainColumn.resting(this.tiltX, this.tiltZ, this.spin)) {
            this.updatePoses = false;
        }
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void fwachainfix$flushChain(AnimationRenderingContext context, CallbackInfo ci) {
        if (this.isLast()) {
            AnimationRenderer.renderBuffers();
        }
    }
}
