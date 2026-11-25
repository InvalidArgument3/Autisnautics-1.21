package me.cortex.voxy.common.network;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players have negotiated the Voxy terrain protocol so the server does not
 * send large custom payloads to vanilla clients.
 */
public final class VoxySessionSupport {
    private record Session(int protocolVersion, int maxTerrainDistanceChunks) {}
    private static final Map<UUID, Session> PLAYER_SESSION = new ConcurrentHashMap<>();


    private VoxySessionSupport() {}

    public static void setClientProtocol(ServerPlayer player, int protocolVersion, int maxTerrainDistanceChunks) {
        if (protocolVersion == VoxelSectionWireCodec.PROTOCOL_VERSION) {
            PLAYER_SESSION.put(player.getUUID(), new Session(protocolVersion, TerrainWireLimits.distance(maxTerrainDistanceChunks)));
        }
    }

    public static void clearAll(){PLAYER_SESSION.clear();}

    public static void clearPlayer(ServerPlayer player) {
        PLAYER_SESSION.remove(player.getUUID());

    }

    public static boolean wantsTerrainPackets(ServerPlayer player) {
        return PLAYER_SESSION.containsKey(player.getUUID());
    }

    public static int getMaxTerrainDistanceChunks(ServerPlayer player) {
        Session session = PLAYER_SESSION.get(player.getUUID());
        if (session == null) {
            return 0;
        }
        return session.maxTerrainDistanceChunks;
    }

}
