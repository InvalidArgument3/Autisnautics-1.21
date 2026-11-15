package dev.wyseman.portalrenderguard.mixin;

import dev.wyseman.portalrenderguard.GlStateGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code blit_texture_depth_cutout.frag} tests the reconstructed point, which
 * {@code inverse(projection * modelView)} puts back in world space. The
 * equation Voxy copies from Immersive Portals is camera-relative.
 */
@Mixin(targets = "me.cortex.voxy.client.compat.ImmPortalsCompat", remap = false)
public class VoxyClipMixin {
    @Inject(method = "activeClipEquation", at = @At("RETURN"), cancellable = true, remap = false)
    private static void portalgl$worldClip(CallbackInfoReturnable<float[]> callback) {
        float[] equation = callback.getReturnValue();
        float[] shifted = GlStateGuard.shiftClipToWorldFromCamera(equation);
        if (shifted != equation) {
            callback.setReturnValue(shifted);
        }
    }
}
