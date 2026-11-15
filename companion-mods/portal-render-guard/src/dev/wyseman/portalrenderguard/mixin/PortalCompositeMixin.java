package dev.wyseman.portalrenderguard.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.wyseman.portalrenderguard.GlStateGuard;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * The framebuffer renderer copies the portal view with
 * {@code drawPortalAreaWithFramebuffer}. That draw enables the depth test and
 * writes depth, and it leaves blending however the destination translucent
 * pass left it, so the portal quad blends with the blocks in front of it.
 * It also changes the viewport to the target size and does not put it back.
 */
@Mixin(targets = "qouteall.imm_ptl.core.render.MyRenderHelper", remap = false)
public class PortalCompositeMixin {
    @Inject(method = "drawPortalAreaWithFramebuffer", at = @At("HEAD"), remap = false)
    private static void portalgl$beforeCopy(Portal portal, RenderTarget target, Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {
        GlStateGuard.push();
        GlStateGuard.disableBlend();
    }

    @Inject(method = "drawPortalAreaWithFramebuffer", at = @At("RETURN"), remap = false)
    private static void portalgl$afterCopy(Portal portal, RenderTarget target, Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {
        GlStateGuard.pop();
    }
}
