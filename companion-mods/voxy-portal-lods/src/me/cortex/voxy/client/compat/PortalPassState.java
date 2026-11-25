package me.cortex.voxy.client.compat;

/**
 * Per-pass GL state handed from VoxyRenderSystem.renderOpaque to the render pipeline while Voxy draws LODs
 * inside an Immersive Portals portal pass. Only touched on the render thread.
 */
public final class PortalPassState {
    private PortalPassState() {}

    /** True while the current Voxy pass is a nested portal pass. */
    public static boolean active;
    /**
     * True during a cross-dimension secondary renderer's first portal pass in the current main frame,
     * allowing AbstractRenderPipeline.innerPrimaryWork to tick its nodeManager and nodeCleaner.
     */
    public static boolean tickSecondaryWorld;
    /** Immersive Portals' stencil state at Voxy entry; re-applied for the final composite blit. */
    public static boolean stencilEnabled;
    public static int stencilFunc, stencilRef, stencilValueMask;
    /** Portal inner clipping plane (camera-relative world space), or null. */
    public static float[] clipEquation;

    public static void reset() {
        active = false;
        tickSecondaryWorld = false;
        stencilEnabled = false;
        clipEquation = null;
    }
}
