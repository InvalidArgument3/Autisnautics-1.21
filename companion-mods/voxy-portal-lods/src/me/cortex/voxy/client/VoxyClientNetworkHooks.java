package me.cortex.voxy.client;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.VoxelSectionWireCodec;
import me.cortex.voxy.common.network.TerrainSyncClientHandler;
import me.cortex.voxy.common.network.VoxyChunkAckPayload;
import me.cortex.voxy.common.network.VoxyHelloPayload;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sends one {@link VoxyHelloPayload} per logical connection so the server can gate terrain broadcasts.
 */
public final class VoxyClientNetworkHooks {
    private static final int MAX_ACKS_PER_PACKET = 128;
    private static Object lastConnection;
    private static final Map<ResourceLocation, LongOpenHashSet> PENDING_ACKS = new HashMap<>();
    /** Default off; enable with {@code -Dvoxy.terrainSyncDebug=true}. */
    private static final boolean TERRAIN_SYNC_DEBUG = VoxyCommon.isVerificationFlagOn("terrainSyncDebug", false);

    private VoxyClientNetworkHooks() {}

    private static boolean lastEnabled;
    public static void onClientTick() {
        var mc=Minecraft.getInstance(); if(mc.player==null||mc.getConnection()==null)return;
        var conn=mc.getConnection(); boolean enabled=VoxyConfig.CONFIG.serverTerrainSyncReceive;
        boolean changed=conn!=lastConnection;
        if(changed){TerrainSyncClientHandler.clearPendingQueue();PENDING_ACKS.clear();ClientColumnCoverage.reset();}
        if(changed||enabled!=lastEnabled){
            if(enabled&&serverAcceptsPlayPayload(VoxyHelloPayload.TYPE.id())){
                int distance=Math.max(0,Math.round(VoxyConfig.CONFIG.sectionRenderDistance*32.0f));
                PacketDistributor.sendToServer(new VoxyHelloPayload(VoxelSectionWireCodec.PROTOCOL_VERSION,distance));
                if(TERRAIN_SYNC_DEBUG||VoxyConfig.CONFIG.serverTerrainSyncDebug)Logger.info("[terrain-sync][client-v2] hello protocol=",VoxelSectionWireCodec.PROTOCOL_VERSION);
            } else if(!enabled&&!changed&&lastEnabled&&serverAcceptsPlayPayload(VoxyChunkAckPayload.TYPE.id())) {
                PacketDistributor.sendToServer(new VoxyChunkAckPayload(0,3));TerrainSyncClientHandler.clearPendingQueue();
            }
            lastConnection=conn;lastEnabled=enabled;
        }
        if(enabled){
            ClientColumnCoverage.onClientTick();
        }
    }
    private static boolean serverAcceptsPlayPayload(ResourceLocation id){var l=Minecraft.getInstance().getConnection();return l!=null&&NetworkRegistry.hasChannel(l.getConnection(),ConnectionProtocol.PLAY,id);}
    /** No whole-column ACK for vanilla loads: it does not prove a complete Voxy column was persisted. */
    public static void queueSeenChunk(ResourceLocation dimension,int x,int z){}
    public static void queueSeenChunk(ResourceLocation dimension,long key){}
    public static void resetConnectionState(){lastConnection=null;lastEnabled=false;PENDING_ACKS.clear();TerrainSyncClientHandler.clearPendingQueue();ClientColumnCoverage.reset();}
}
