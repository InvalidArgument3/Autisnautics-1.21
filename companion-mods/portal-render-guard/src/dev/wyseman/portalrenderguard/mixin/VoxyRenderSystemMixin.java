package dev.wyseman.portalrenderguard.mixin;

import dev.wyseman.portalrenderguard.GlStateGuard;
import me.cortex.voxy.client.core.rendering.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Voxy draws from inside Sodium's solid pass, which is also the portal
 * world's solid pass. Put the fixed-function state back before cutout,
 * entities, and the portal framebuffer copy run.
 */
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public class VoxyRenderSystemMixin {
    @Inject(method = "renderOpaque", at = @At("HEAD"), remap = false)
    private void portalgl$beforeOpaque(Viewport<?> viewport, CallbackInfo ci) {
        GlStateGuard.push();
    }

    @Inject(method = "renderOpaque", at = @At("RETURN"), remap = false)
    private void portalgl$afterOpaque(Viewport<?> viewport, CallbackInfo ci) {
        GlStateGuard.pop();
    }

    @Inject(method = "renderPortalOpaque", at = @At("HEAD"), remap = false)
    private void portalgl$beforePortal(Viewport<?> viewport, CallbackInfo ci) {
        GlStateGuard.push();
    }

    @Inject(method = "renderPortalOpaque", at = @At("RETURN"), remap = false)
    private void portalgl$afterPortal(Viewport<?> viewport, CallbackInfo ci) {
        GlStateGuard.pop();
    }
}
