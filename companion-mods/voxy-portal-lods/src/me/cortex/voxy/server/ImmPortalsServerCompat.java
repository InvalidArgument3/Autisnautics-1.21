package me.cortex.voxy.server;

import me.cortex.voxy.common.Logger;
import me.cortex.voxy.commonImpl.PlatformUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Soft server-side compat with Immersive Portals (mod id "imm_ptl" / "immersive_portals")
 * to discover adjacent dimensional-stack ServerLevels connected via GlobalPortalStorage.
 */
public final class ImmPortalsServerCompat {
    public static final int ADJACENT_DIM_MAX_RADIUS_CHUNKS = Math.max(8, Math.min(256,
            Integer.getInteger("voxy.terrainSyncAdjacentRadiusChunks", 128)));

    private static volatile boolean initialised;
    private static MethodHandle GET_GLOBAL_PORTALS;
    private static MethodHandle GET_DEST_WORLD;

    private ImmPortalsServerCompat() {}

    private static void init() {
        if (initialised) return;
        synchronized (ImmPortalsServerCompat.class) {
            if (initialised) return;
            try {
                if (PlatformUtil.isModLoaded("imm_ptl") || PlatformUtil.isModLoaded("immersive_portals")) {
                    var lookup = MethodHandles.publicLookup();
                    Class<?> storage = Class.forName("qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage");
                    Class<?> portal = Class.forName("qouteall.imm_ptl.core.portal.Portal");
                    GET_GLOBAL_PORTALS = lookup.findStatic(storage, "getGlobalPortals",
                            MethodType.methodType(List.class, Level.class));
                    GET_DEST_WORLD = lookup.findVirtual(portal, "getDestinationWorld",
                            MethodType.methodType(Level.class)).asType(MethodType.methodType(Level.class, Object.class));
                    Logger.info("Immersive Portals server-side dimstack pre-streaming hooks resolved (maxRadius=" + ADJACENT_DIM_MAX_RADIUS_CHUNKS + " chunks)");
                }
            } catch (Throwable t) {
                Logger.warn("Immersive Portals server-side global portal hooks unavailable: " + t);
                GET_GLOBAL_PORTALS = null;
                GET_DEST_WORLD = null;
            }
            initialised = true;
        }
    }

    /**
     * Returns distinct adjacent ServerLevels reachable from {@code player.serverLevel()} via global portals
     * (e.g. dimensional stack ceiling and floor portals). Never includes {@code player.serverLevel()} itself.
     */
    public static List<ServerLevel> getAdjacentDimstackLevels(ServerPlayer player) {
        if (player == null) return Collections.emptyList();
        return getAdjacentDimstackLevels(player.serverLevel());
    }

    public static List<ServerLevel> getAdjacentDimstackLevels(ServerLevel level) {
        if (level == null) return Collections.emptyList();
        if (!initialised) init();
        var getPortals = GET_GLOBAL_PORTALS;
        var getDest = GET_DEST_WORLD;
        if (getPortals == null || getDest == null) return Collections.emptyList();
        try {
            @SuppressWarnings("unchecked")
            List<Object> portals = (List<Object>) getPortals.invokeExact((Level) level);
            if (portals == null || portals.isEmpty()) return Collections.emptyList();
            List<ServerLevel> result = null;
            for (Object portal : portals) {
                if (portal == null) continue;
                Level dest = (Level) getDest.invokeExact(portal);
                if (dest instanceof ServerLevel destLevel && destLevel != level) {
                    if (result == null) result = new ArrayList<>(2);
                    if (!result.contains(destLevel)) {
                        result.add(destLevel);
                    }
                }
            }
            return result != null ? result : Collections.emptyList();
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    /** True if {@code target} is an adjacent dimstack ServerLevel reachable from {@code player.serverLevel()}. */
    public static boolean isAdjacentDimstackLevel(ServerPlayer player, ServerLevel target) {
        if (player == null || target == null || target == player.serverLevel()) return false;
        for (ServerLevel adj : getAdjacentDimstackLevels(player)) {
            if (adj == target) return true;
        }
        return false;
    }
}
