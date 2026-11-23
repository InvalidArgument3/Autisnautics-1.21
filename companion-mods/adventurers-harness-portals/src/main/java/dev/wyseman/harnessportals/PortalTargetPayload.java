package dev.wyseman.harnessportals;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public record PortalTargetPayload(
        boolean hasPortalTarget,
        ResourceLocation dimId,
        double eyeX,
        double eyeY,
        double eyeZ
) implements CustomPacketPayload {

    public static final Type<PortalTargetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AdventurersHarnessPortals.MOD_ID, "portal_target")
    );

    public static final StreamCodec<ByteBuf, PortalTargetPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortalTargetPayload decode(ByteBuf buf) {
            boolean has = buf.readBoolean();
            if (!has) {
                return new PortalTargetPayload(false, Level.OVERWORLD.location(), 0.0, 0.0, 0.0);
            }
            ResourceLocation dim = ResourceLocation.STREAM_CODEC.decode(buf);
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            return new PortalTargetPayload(true, dim, x, y, z);
        }

        @Override
        public void encode(ByteBuf buf, PortalTargetPayload val) {
            buf.writeBoolean(val.hasPortalTarget);
            if (val.hasPortalTarget) {
                ResourceLocation.STREAM_CODEC.encode(buf, val.dimId);
                buf.writeDouble(val.eyeX);
                buf.writeDouble(val.eyeY);
                buf.writeDouble(val.eyeZ);
            }
        }
    };

    public record PortalContext(ResourceKey<Level> targetDim, Vec3 transformedEye) {}

    public static final Map<UUID, PortalContext> PENDING_BY_PLAYER = new ConcurrentHashMap<>();
    public static final Map<Object, PortalContext> JOB_CONTEXTS = Collections.synchronizedMap(new WeakHashMap<>());
    public static final ThreadLocal<PortalContext> ACTIVE_CONTEXT = new ThreadLocal<>();

    // Client-side state set by MiningInputMixin during raycast
    public static volatile ResourceKey<Level> clientTargetDim = null;
    public static volatile Vec3 clientTransformedEye = null;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PortalTargetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!payload.hasPortalTarget) {
                PENDING_BY_PLAYER.remove(player.getUUID());
                return;
            }
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, payload.dimId);
            ServerLevel level = player.server.getLevel(key);
            if (level == null || key.equals(player.level().dimension())) {
                PENDING_BY_PLAYER.remove(player.getUUID());
                return;
            }
            PENDING_BY_PLAYER.put(
                    player.getUUID(),
                    new PortalContext(key, new Vec3(payload.eyeX, payload.eyeY, payload.eyeZ))
            );
        });
    }
}
