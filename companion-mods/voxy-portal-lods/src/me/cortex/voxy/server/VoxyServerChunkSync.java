package me.cortex.voxy.server;

import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.SectionSyncPayload;
import me.cortex.voxy.common.network.VoxySessionSupport;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.common.world.service.VoxelIngestService;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VoxyServerChunkSync {
    private VoxyServerChunkSync() {}
    /** Default off; enable with {@code -Dvoxy.terrainSyncDebug=true}. */
    private static final boolean TERRAIN_SYNC_DEBUG = VoxyCommon.isVerificationFlagOn("terrainSyncDebug", false);
    private static final long DEBUG_REPORT_INTERVAL_MS = 5000L;
    private static long debugLastReportMs;
    private static long debugChunksSeen;
    private static long debugChunksWithPayload;
    private static long debugSectionsProduced;
    private static long debugPayloadsSent;
    private static long debugPayloadBytesSent;
    private static long debugLastChunksSeen;
    private static long debugLastPayloadsSent;

    public static void register() {
        ServerTerrainSyncBudget.register();
        ServerTerrainTransport.register();
        NeoForge.EVENT_BUS.addListener(VoxyServerChunkSync::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(VoxyServerChunkSync::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(VoxyServerChunkSync::onServerStopped);
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        VoxySessionSupport.clearAll();
        ServerTerrainSyncDeferredQueue.clear();
    }

    private static void maybeLogDebugSummary() {
        if (!TERRAIN_SYNC_DEBUG && !VoxyConfig.CONFIG.serverTerrainSyncDebug) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - debugLastReportMs < DEBUG_REPORT_INTERVAL_MS) {
            return;
        }
        debugLastReportMs = now;
        if (debugChunksSeen == debugLastChunksSeen && debugPayloadsSent == debugLastPayloadsSent) {
            return;
        }
        debugLastChunksSeen = debugChunksSeen;
        debugLastPayloadsSent = debugPayloadsSent;
        Logger.info("[terrain-sync][server] chunksSeen=", debugChunksSeen,
                " chunksWithPayload=", debugChunksWithPayload,
                " sectionsProduced=", debugSectionsProduced,
                " transport=", ServerTerrainTransport.status());
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            VoxySessionSupport.clearPlayer(sp);
            ServerTerrainTransport.clearPlayer(sp);
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || serverLevel.isClientSide()) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (!serverLevel.getServer().isDedicatedServer()) {
            // Integrated singleplayer shares the client Voxy instance; terrain is ingested from the client chunk pipeline.
            // Dedicated server push is implemented here; SP reuse of the same transport can be layered on later.
            return;
        }
        if (!VoxyConfig.CONFIG.serverTerrainSyncBroadcast) {
            return;
        }
        var instance = VoxyCommon.getInstance();
        if (instance == null) {
            return;
        }
        WorldIdentifier wid = WorldIdentifier.of(serverLevel);
        if (wid == null) {
            return;
        }
        ChunkPos cpos=chunk.getPos();
        java.util.List<ServerPlayer> recipients=new java.util.ArrayList<>();
        for(ServerPlayer pl:serverLevel.players()){
            int inner=ServerTerrainTransport.effectiveInnerRadius(pl);
            int distance=cpos.getChessboardDistance(pl.chunkPosition());int max=VoxySessionSupport.getMaxTerrainDistanceChunks(pl);
            if(VoxySessionSupport.wantsTerrainPackets(pl)&&distance>inner&&max>0&&distance<=max)recipients.add(pl);
        }
        for(ServerLevel adj:ImmPortalsServerCompat.getAdjacentDimstackLevels(serverLevel)){
            for(ServerPlayer pl:adj.players()){
                if(!VoxySessionSupport.wantsTerrainPackets(pl))continue;
                int max=Math.min(VoxySessionSupport.getMaxTerrainDistanceChunks(pl),ImmPortalsServerCompat.ADJACENT_DIM_MAX_RADIUS_CHUNKS);
                int distance=cpos.getChessboardDistance(pl.chunkPosition());
                if(max>0&&distance<=max&&!recipients.contains(pl))recipients.add(pl);
            }
        }
        if(recipients.isEmpty())return;
        WorldEngine engine=instance.getOrCreate(wid);
        java.util.List<byte[]> wires=new java.util.ArrayList<>();debugChunksSeen++;
        VoxelIngestService.forEachMippedSectionForTerrainSync(engine,chunk,section->{
            WorldUpdater.insertUpdate(engine,section);
            wires.add(SectionSyncPayload.encodeSectionWireBytes(serverLevel,engine,section));debugSectionsProduced++;
        });
        for(ServerPlayer pl:recipients)ServerTerrainTransport.offerColumn(pl,serverLevel,cpos,wires);
        if(!wires.isEmpty())debugChunksWithPayload++;
        maybeLogDebugSummary();
    }

    private static boolean hasTerrainSyncRecipients(ServerLevel level) {
        for (ServerPlayer pl : level.players()) {
            if (VoxySessionSupport.wantsTerrainPackets(pl)) {
                return true;
            }
        }
        return false;
    }
}
