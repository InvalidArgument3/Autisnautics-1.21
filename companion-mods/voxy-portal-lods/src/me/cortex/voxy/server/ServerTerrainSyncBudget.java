package me.cortex.voxy.server;

import me.cortex.voxy.common.config.VoxyConfig;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Per-server-tick caps for terrain sync sends (live chunk sync, deferred queue, walker).
 */
public final class ServerTerrainSyncBudget {
    private static int sectionsUsed;
    private static long bytesUsed;

    private ServerTerrainSyncBudget() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTerrainSyncBudget::onServerTickPre);
    }

    private static void onServerTickPre(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) {
            return;
        }
        sectionsUsed = 0;
        bytesUsed = 0;
    }

    /**
     * @return true if this send fits the remaining budget and was reserved.
     */
    public static boolean tryConsume(int sections, long bytes) {
        int maxSections = VoxyConfig.CONFIG.serverTerrainSyncMaxSectionsPerTick;
        long maxBytes = Integer.toUnsignedLong(VoxyConfig.CONFIG.serverTerrainSyncMaxBytesPerTick);
        if (maxSections <= 0 && maxBytes <= 0L) {
            return true;
        }
        if (maxSections > 0 && sectionsUsed + sections > maxSections) {
            return false;
        }
        if (maxBytes > 0L && bytesUsed + bytes > maxBytes) {
            return false;
        }
        sectionsUsed += sections;
        bytesUsed += bytes;
        return true;
    }
}
