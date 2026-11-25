package me.cortex.voxy.common.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class VoxyNetworking {
    public static final String CHANNEL_VERSION = "2";

    private VoxyNetworking() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(VoxyNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Optional: connections succeed when the remote has no Voxy (no matching handlers/channels).
        // Server still only sends terrain payloads to players that negotiated support (see VoxySessionSupport).
        PayloadRegistrar registrar = event.registrar(CHANNEL_VERSION).optional();
        // Only evaluate one handler reference: TerrainSyncClientHandler pulls client classes and must
        // not load on dedicated server. Still register the clientbound payload on the server so codecs
        // exist for PacketDistributor.sendToPlayer.
        registrar.playToClient(
                SectionSyncPayload.TYPE,
                SectionSyncPayload.STREAM_CODEC,
                FMLEnvironment.dist.isClient()
                        ? TerrainSyncClientHandler::handleSectionSync
                        : SectionSyncPayload::registerServerPlaceholderHandler);
        registrar.playToClient(
                SectionSyncBatchPayload.TYPE,
                SectionSyncBatchPayload.STREAM_CODEC,
                FMLEnvironment.dist.isClient()
                        ? TerrainSyncClientHandler::handleSectionSyncBatch
                        : SectionSyncBatchPayload::registerServerPlaceholderHandler);
        registrar.playToServer(VoxyHelloPayload.TYPE, VoxyHelloPayload.STREAM_CODEC, VoxyHelloPayload::handleServer);
        registrar.playToServer(VoxyChunkAckPayload.TYPE, VoxyChunkAckPayload.STREAM_CODEC, VoxyChunkAckPayload::handleServer);
        ColumnCoveragePayload.register(registrar);
    }
}
