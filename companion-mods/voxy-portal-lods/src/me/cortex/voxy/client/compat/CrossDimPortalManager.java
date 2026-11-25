package me.cortex.voxy.client.compat;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages VRAM-budgeted secondary VoxyRenderSystem instances for cross-dimension Immersive Portals
 * (such as dimensional stack portals). All methods are called exclusively on the render thread.
 *
 * Lifecycle:
 * - Never creates a VoxyRenderSystem mid-portal-pass. During a cross-dimension portal pass whose
 *   destination LevelRenderer has no VoxyRenderSystem yet, {@link #requestSecondary} queues it.
 * - On the next main (non-portal) pass, {@link #onMainFrame} creates the budgeted secondary
 *   renderer safely outside IP's stencil/framebuffer state, evicting LRU secondaries if over cap.
 * - Secondary renderers idle for more than {@link #IDLE_EVICT_FRAMES} main frames are shut down
 *   and freed so VRAM returns to baseline when not viewing cross-dimension portals.
 */
public final class CrossDimPortalManager {
    private CrossDimPortalManager() {}

    public static final int MAX_SECONDARY_RENDERERS = clampInt(
            Integer.getInteger("voxy.portalCrossDimMaxRenderers", 2), 1, 4);

    public static final long SECONDARY_GEOMETRY_BUDGET_BYTES = (long) clampInt(
            Integer.getInteger("voxy.portalCrossDimBudgetMB", 384), 128, 1024) * 1024L * 1024L;

    public static final float SECONDARY_MAX_RENDER_DISTANCE = clampFloat(
            parseFloatProp("voxy.portalCrossDimRenderDistance", 8.0f), 2.0f, 16.0f);

    public static final long IDLE_EVICT_FRAMES = clampInt(
            Integer.getInteger("voxy.portalCrossDimIdleFrames", 600), 60, 36000);

    /** Never evict an LRU secondary that was actively rendered within the last 60 main frames (prevents thrashing). */
    private static final long MIN_LRU_EVICT_AGE_FRAMES = 60L;

    private static long globalMainFrameCounter = 0L;
    private static WeakReference<IGetVoxyRenderSystem> primaryOwner = new WeakReference<>(null);
    private static final LinkedHashMap<IGetVoxyRenderSystem, Long> secondaries = new LinkedHashMap<>();
    private static IGetVoxyRenderSystem pendingSecondary = null;
    private static int creationFailureCooldown = 0;

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float clampFloat(float v, float lo, float hi) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return lo;
        return Math.max(lo, Math.min(hi, v));
    }

    private static float parseFloatProp(String key, float def) {
        String raw = System.getProperty(key, "");
        if (raw == null || raw.isEmpty()) return def;
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static float effectiveSecondaryRenderDistance() {
        return Math.min(VoxyConfig.CONFIG.sectionRenderDistance, SECONDARY_MAX_RENDER_DISTANCE);
    }

    public static long currentMainFrame() {
        return globalMainFrameCounter;
    }

    /** Called before creating a full-budget primary VoxyRenderSystem on {@code newPrimary}. */
    public static void onPrimaryCreating(IGetVoxyRenderSystem newPrimary) {
        secondaries.remove(newPrimary);
        if (pendingSecondary == newPrimary) {
            pendingSecondary = null;
        }
        var prev = primaryOwner.get();
        if (prev != null && prev != newPrimary && prev.voxy$getRenderSystem() != null) {
            Logger.info("Shutting down Voxy renderer of previous primary LevelRenderer (dimension switch via portal)");
            prev.voxy$shutdownRenderer();
        }
    }

    /** Called right after {@code owner} finishes creating its primary VoxyRenderSystem. */
    public static void setPrimary(IGetVoxyRenderSystem owner) {
        secondaries.remove(owner);
        if (pendingSecondary == owner) {
            pendingSecondary = null;
        }
        primaryOwner = new WeakReference<>(owner);
    }

    /** Called during a cross-dimension portal pass when the destination LevelRenderer has no VoxyRenderSystem. */
    public static void requestSecondary(IGetVoxyRenderSystem lr) {
        if (!ImmPortalsCompat.CROSS_DIM_PORTAL_LODS_ENABLED || lr == null) {
            return;
        }
        if (lr == primaryOwner.get() || secondaries.containsKey(lr)) {
            return;
        }
        pendingSecondary = lr;
    }

    /** Called during a portal pass when a secondary VoxyRenderSystem is actively rendered. */
    public static void markSecondaryUsed(IGetVoxyRenderSystem lr) {
        if (lr != null && secondaries.containsKey(lr)) {
            secondaries.put(lr, globalMainFrameCounter);
        }
    }

    /** Called whenever a LevelRenderer shuts down its VoxyRenderSystem. */
    public static void onShutdown(IGetVoxyRenderSystem lr) {
        if (lr == null) return;
        secondaries.remove(lr);
        if (pendingSecondary == lr) {
            pendingSecondary = null;
        }
        if (primaryOwner.get() == lr) {
            primaryOwner = new WeakReference<>(null);
        }
    }

    /**
     * Called once per main (non-portal) render pass, outside any IP stencil/framebuffer state.
     * Evicts idle secondary renderers and fulfils any pending cross-dimension portal request.
     */
    public static void onMainFrame(IGetVoxyRenderSystem mainLr) {
        globalMainFrameCounter++;
        if (creationFailureCooldown > 0) {
            creationFailureCooldown--;
        }

        // Evict stale or idle secondary renderers
        if (!secondaries.isEmpty()) {
            ArrayList<IGetVoxyRenderSystem> toEvict = null;
            for (Iterator<Map.Entry<IGetVoxyRenderSystem, Long>> it = secondaries.entrySet().iterator(); it.hasNext(); ) {
                var entry = it.next();
                var lr = entry.getKey();
                if (lr == mainLr || lr.voxy$getRenderSystem() == null) {
                    it.remove();
                    continue;
                }
                if ((globalMainFrameCounter - entry.getValue()) > IDLE_EVICT_FRAMES) {
                    if (toEvict == null) toEvict = new ArrayList<>();
                    toEvict.add(lr);
                }
            }
            if (toEvict != null) {
                for (var lr : toEvict) {
                    secondaries.remove(lr);
                    Logger.info("Evicting idle cross-dimension Voxy portal renderer after " + IDLE_EVICT_FRAMES + " frames");
                    lr.voxy$shutdownRenderer();
                }
            }
        }

        // Fulfil pending cross-dimension request safely outside portal pass
        IGetVoxyRenderSystem target = pendingSecondary;
        pendingSecondary = null;
        if (target == null || !ImmPortalsCompat.CROSS_DIM_PORTAL_LODS_ENABLED || creationFailureCooldown > 0) {
            return;
        }
        if (target == mainLr || target.voxy$getRenderSystem() != null) {
            return;
        }

        // Evict LRU secondary renderer if at capacity — but never evict one that was actively rendered
        // within MIN_LRU_EVICT_AGE_FRAMES (prevents frame-by-frame ping-pong thrashing when >cap portals are visible).
        while (secondaries.size() >= MAX_SECONDARY_RENDERERS) {
            IGetVoxyRenderSystem lru = null;
            long oldest = Long.MAX_VALUE;
            for (var entry : secondaries.entrySet()) {
                if (entry.getValue() < oldest) {
                    oldest = entry.getValue();
                    lru = entry.getKey();
                }
            }
            if (lru == null || (globalMainFrameCounter - oldest) < MIN_LRU_EVICT_AGE_FRAMES) {
                return;
            }
            secondaries.remove(lru);
            Logger.info("Evicting LRU cross-dimension Voxy portal renderer (cap=" + MAX_SECONDARY_RENDERERS + ")");
            lru.voxy$shutdownRenderer();
        }

        try {
            target.voxy$createSecondaryRenderer();
            if (target.voxy$getRenderSystem() != null) {
                secondaries.put(target, globalMainFrameCounter);
                Logger.info("Cross-dimension Voxy portal renderer active (budget="
                        + (SECONDARY_GEOMETRY_BUDGET_BYTES / (1024L * 1024L)) + " MB, renderDist="
                        + effectiveSecondaryRenderDistance() + ", activeSecondaries=" + secondaries.size() + ")");
            }
        } catch (Throwable t) {
            creationFailureCooldown = 300;
            Logger.error("Failed to create cross-dimension Voxy portal renderer; backing off 300 frames", t);
        }
    }
}
