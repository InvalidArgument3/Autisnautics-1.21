package me.cortex.voxy.client;

import java.lang.reflect.Field;
import me.cortex.voxy.common.config.VoxyConfig;

/** One client line every five seconds when terrain-sync debug is on. */
public final class TerrainClientStats {
    private TerrainClientStats() {
    }

    public static boolean enabled() {
        try {
            if (VoxyConfig.CONFIG != null && VoxyConfig.CONFIG.serverTerrainSyncDebug) {
                return true;
            }
        } catch (Throwable ignored) {
            return false;
        }
        return false;
    }

    public static String line() {
        long received = field("received");
        long applied = field("applied");
        long completed = field("completed");
        long retried = field("retried");
        long dropped = field("dropped");
        long errors = field("errors");
        return "received=" + received
                + " applied=" + applied
                + " completed=" + completed
                + " retried=" + retried
                + " dropped=" + dropped
                + " rejected=" + errors
                + " " + ClientColumnCoverage.stats();
    }

    private static long field(String name) {
        try {
            Class<?> handler = Class.forName("me.cortex.voxy.common.network.TerrainSyncClientHandler");
            Field field = handler.getDeclaredField(name);
            field.setAccessible(true);
            return field.getLong(null);
        } catch (Throwable ignored) {
            return -1L;
        }
    }
}
