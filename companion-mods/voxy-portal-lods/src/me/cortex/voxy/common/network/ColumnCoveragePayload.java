package me.cortex.voxy.common.network;

import me.cortex.voxy.server.ColumnCoverageIndex;
import me.cortex.voxy.server.TerrainDispatch;
import me.cortex.voxy.server.TerrainSentMemory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * One 32×32 chunk region the client already has lod-0 data for.
 * Hello stays the two-varint packet. This packet is optional: an older peer
 * never sends it, and a newer client does not send it unless the server
 * negotiated the channel.
 */
public final class ColumnCoveragePayload implements CustomPacketPayload {
    public static final Type<ColumnCoveragePayload> TYPE = new Type<ColumnCoveragePayload>(ResourceLocation.fromNamespaceAndPath("voxy", "column_coverage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ColumnCoveragePayload> STREAM_CODEC = StreamCodec.of(
            (RegistryFriendlyByteBuf buf, ColumnCoveragePayload payload) -> {
                buf.writeVarInt(payload.regionX);
                buf.writeVarInt(payload.regionZ);
                buf.writeByteArray(payload.columns);
            },
            buf -> new ColumnCoveragePayload(buf.readVarInt(), buf.readVarInt(), buf.readByteArray(ColumnCoverageIndex.BYTE_LENGTH)));

    private final int regionX;
    private final int regionZ;
    private final byte[] columns;

    public ColumnCoveragePayload(int regionX, int regionZ, byte[] columns) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.columns = columns;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, ColumnCoveragePayload::handle);
    }

    public static void handle(ColumnCoveragePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> apply(context, payload));
    }

    private static void apply(IPayloadContext context, ColumnCoveragePayload payload) {
        try {
            Player player = context.player();
            if (!(player instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (!VoxySessionSupport.wantsTerrainPackets(serverPlayer)) {
                return;
            }
            byte[] columns = payload.columns;
            if (columns == null || columns.length != ColumnCoverageIndex.BYTE_LENGTH) {
                return;
            }
            String world = TerrainDispatch.worldKeyOf(serverPlayer.serverLevel());
            TerrainSentMemory.cover(serverPlayer.getUUID().toString(), world, payload.regionX, payload.regionZ, columns);
        } catch (Throwable ignored) {
            // A bad region must not drop the connection. The walker just won't skip it.
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public int regionX() {
        return this.regionX;
    }

    public int regionZ() {
        return this.regionZ;
    }

    public byte[] columns() {
        return this.columns;
    }
}
