package me.cortex.voxy.client.compat;

import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.commonImpl.PlatformUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Soft compat with Immersive Portals / Immersive Aeronautics (mod id "imm_ptl").
 * IP renders portal contents by recursively calling GameRenderer.renderLevel with
 * Minecraft.levelRenderer swapped to the destination dimension's LevelRenderer and the GL stencil
 * (or a secondary framebuffer) restricting output to the portal area.
 *
 * PORTAL-LOD PROTOTYPE (phase 1): for a SAME-dimension portal (the current LevelRenderer still owns the
 * Voxy render system), first portal layer only, no mirror / scaling portal, no shaderpack, Voxy renders its
 * LODs from the portal camera through a dedicated per-portal Viewport (shares geometry, nodes and
 * traversal with the main view, so no second geometry buffer). Kill switch: -Dvoxy.portalLods=false
 */
public final class ImmPortalsCompat {
    private ImmPortalsCompat() {}

    public static final boolean PORTAL_LODS_ENABLED = !"false".equalsIgnoreCase(System.getProperty("voxy.portalLods", "true"));
    public static final boolean CROSS_DIM_PORTAL_LODS_ENABLED = PORTAL_LODS_ENABLED
            && !"false".equalsIgnoreCase(System.getProperty("voxy.portalCrossDim", "true"));

    private static volatile boolean initialised;
    private static MethodHandle IS_RENDERING;
    private static MethodHandle GET_LAYER;
    private static MethodHandle ODD_MIRRORS;
    private static MethodHandle GET_RENDERING_PORTAL;
    private static MethodHandle HAS_SCALING;
    private static MethodHandle CLIP_ENABLED;
    private static MethodHandle CLIP_EQUATION;
    private static MethodHandle BEGIN_RENDERER_CREATION;
    private static MethodHandle END_RENDERER_CREATION;
    private static MethodHandle GET_CLIENT_WORLD;

    private static void init() {
        if (initialised) return;
        synchronized (ImmPortalsCompat.class) {
            if (initialised) return;
            if (net.neoforged.fml.ModList.get() == null) return;//Too early, retry later
            try {
                if (PlatformUtil.isModLoaded("imm_ptl") || PlatformUtil.isModLoaded("immersive_portals")) {
                    var lookup = MethodHandles.publicLookup();
                    Class<?> c = Class.forName("qouteall.imm_ptl.core.render.context_management.PortalRendering");
                    IS_RENDERING = lookup.findStatic(c, "isRendering", MethodType.methodType(boolean.class));
                    Logger.info("Immersive Portals detected");
                    try {
                        Class<?> portal = Class.forName("qouteall.imm_ptl.core.portal.Portal");
                        Class<?> fc = Class.forName("qouteall.imm_ptl.core.render.FrontClipping");
                        GET_LAYER = lookup.findStatic(c, "getPortalLayer", MethodType.methodType(int.class));
                        ODD_MIRRORS = lookup.findStatic(c, "isRenderingOddNumberOfMirrors", MethodType.methodType(boolean.class));
                        GET_RENDERING_PORTAL = lookup.findStatic(c, "getRenderingPortal", MethodType.methodType(portal)).asType(MethodType.methodType(Object.class));
                        HAS_SCALING = lookup.findVirtual(portal, "hasScaling", MethodType.methodType(boolean.class)).asType(MethodType.methodType(boolean.class, Object.class));
                        CLIP_ENABLED = lookup.findStaticGetter(fc, "isClippingEnabled", boolean.class);
                        CLIP_EQUATION = lookup.findStatic(fc, "getActiveClipPlaneEquationBeforeModelView", MethodType.methodType(double[].class));
                        try {
                            Class<?> cwl = Class.forName("qouteall.imm_ptl.core.ClientWorldLoader");
                            GET_CLIENT_WORLD = lookup.findStatic(cwl, "getWorld",
                                    MethodType.methodType(net.minecraft.client.multiplayer.ClientLevel.class, net.minecraft.resources.ResourceKey.class));
                        } catch (Throwable ignored) {
                            GET_CLIENT_WORLD = null;
                        }
                        Logger.info("Immersive Portals portal-LOD hooks resolved; portal LODs " + (PORTAL_LODS_ENABLED ? "enabled (max portal viewports " + me.cortex.voxy.client.core.rendering.ViewportSelector.MAX_PORTAL_VIEWPORTS + ")" : "disabled (-Dvoxy.portalLods=false)"));
                        if (PORTAL_LODS_ENABLED) {
                            disableForkPortalView();
                        }
                    } catch (Throwable t) {
                        Logger.warn("Immersive Portals portal-LOD hooks unavailable, LODs will not render inside portals: " + t);
                        GET_LAYER = null;
                    }
                }
            } catch (Throwable t) {
                Logger.warn("Immersive Portals present but PortalRendering hook unavailable: " + t);
                IS_RENDERING = null;
            }
            initialised = true;
        }
    }

    /**
     * The "Immersive Portals: Aeronautics" fork (mod id ip_aeronautics, IP 6.0.7+ipa) bundles its own Voxy portal
     * view (qouteall.imm_ptl.core.compat.voxy_integration.IPVoxyPortalView + MixinVoxyRenderSystem_PortalView).
     * This build draws portal LODs itself, so switch the fork's portal-view path off (its public ENABLED flag;
     * with it false the fork's setupViewport hook declines portal passes instead of picking its own viewport).
     * The fork's other Voxy mixins (per-dimension creation level, Iris pipeline ownership, shutdown) are left alone.
     * Not done when -Dvoxy.portalLods=false, so the kill switch leaves the fork completely untouched.
     */
    private static void disableForkPortalView() {
        try {
            Class<?> view = Class.forName("qouteall.imm_ptl.core.compat.voxy_integration.IPVoxyPortalView");
            var field = view.getField("ENABLED");
            field.setBoolean(null, false);
            var lookup = MethodHandles.publicLookup();
            try {
                BEGIN_RENDERER_CREATION = lookup.findStatic(view, "beginRendererCreation",
                        MethodType.methodType(void.class, net.minecraft.client.multiplayer.ClientLevel.class));
                END_RENDERER_CREATION = lookup.findStatic(view, "endRendererCreation",
                        MethodType.methodType(void.class));
            } catch (Throwable ignored) {
                BEGIN_RENDERER_CREATION = null;
                END_RENDERER_CREATION = null;
            }
            Logger.info("Immersive Portals: Aeronautics Voxy portal view detected; disabled it, Voxy draws portal LODs itself");
        } catch (ClassNotFoundException e) {
            //Not the ip_aeronautics fork (plain IP / Immersive Aeronautics): nothing to do
        } catch (Throwable t) {
            Logger.warn("Could not disable the Immersive Portals: Aeronautics Voxy portal view (LODs may not show in portals): " + t);
        }
    }

    /** Push {@code level} onto IP:Aeronautics's creation-level stack (if present) when creating a secondary renderer. */
    public static void beginRendererCreation(net.minecraft.client.multiplayer.ClientLevel level) {
        if (!initialised) init();
        var mh = BEGIN_RENDERER_CREATION;
        if (mh != null) {
            try {
                mh.invokeExact(level);
            } catch (Throwable ignored) {}
        }
    }

    /** Pop IP:Aeronautics's creation-level stack (if present) after creating a secondary renderer. */
    public static void endRendererCreation() {
        var mh = END_RENDERER_CREATION;
        if (mh != null) {
            try {
                mh.invokeExact();
            } catch (Throwable ignored) {}
        }
    }

    /** Returns the loaded ClientLevel for {@code dimKey} from Immersive Portals' ClientWorldLoader, or null. */
    public static net.minecraft.client.multiplayer.ClientLevel getClientLevel(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimKey) {
        if (dimKey == null) return null;
        if (!initialised) init();
        var mh = GET_CLIENT_WORLD;
        if (mh == null) return null;
        try {
            return (net.minecraft.client.multiplayer.ClientLevel) mh.invokeExact(dimKey);
        } catch (Throwable t) {
            return null;
        }
    }

    /** True while Immersive Portals is rendering the contents of a portal (nested world pass). */
    public static boolean isRenderingPortal() {
        if (!initialised) init();
        var mh = IS_RENDERING;
        if (mh == null) return false;
        try {
            return (boolean) mh.invokeExact();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isPresent() {
        if (!initialised) init();
        return IS_RENDERING != null;
    }

    /**
     * True if the current nested portal pass is one Voxy can draw LODs into (phase 1 rules).
     * The caller must additionally check that the current LevelRenderer owns a Voxy render system
     * (that is what makes it a same-dimension portal).
     */
    public static boolean canRenderLodsInCurrentPortal() {
        if (!PORTAL_LODS_ENABLED || !isRenderingPortal() || GET_LAYER == null) return false;
        try {
            if ((int) GET_LAYER.invokeExact() != 1) return false;//Only the first portal layer
            if ((boolean) ODD_MIRRORS.invokeExact()) return false;//Mirrors flip winding, unsupported
            Object portal = GET_RENDERING_PORTAL.invokeExact();
            if (portal == null || (boolean) HAS_SCALING.invokeExact(portal)) return false;//Scaling portals unsupported
        } catch (Throwable t) {
            return false;
        }
        return !IrisUtil.irisShaderPackEnabled();//Shaderpacks: phase 3
    }

    /** Key identifying the portal currently being rendered (the portal entity's UUID), or null. */
    public static Object currentPortalKey() {
        if (GET_RENDERING_PORTAL == null) return null;
        try {
            Object portal = GET_RENDERING_PORTAL.invokeExact();
            if (portal instanceof net.minecraft.world.entity.Entity e) {
                return e.getUUID();
            }
            return portal;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * The active portal inner clipping plane as {a,b,c,d} in camera-relative world space
     * (keep where dot(eq, vec4(pos,1)) >= 0), or null if clipping is off.
     */
    public static float[] activeClipEquation() {
        if (CLIP_EQUATION == null) return null;
        try {
            if (!(boolean) CLIP_ENABLED.invokeExact()) return null;
            double[] eq = (double[]) CLIP_EQUATION.invokeExact();
            if (eq == null || eq.length < 4) return null;
            return new float[]{(float) eq[0], (float) eq[1], (float) eq[2], (float) eq[3]};
        } catch (Throwable t) {
            return null;
        }
    }
}
