package dev.wyseman.portallinker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Sneak-scroll is client input. The server decides whether a link or the item changes. */
public final class PortalLinkerNet {
    public static final int REACH_SQR = 8 * 8;

    private PortalLinkerNet() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CycleLinkPayload.TYPE, CycleLinkPayload.STREAM_CODEC, PortalLinkerNet::handle);
    }

    private static void handle(CycleLinkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                apply(player, payload.pos(), payload.targeted(), payload.steps());
            }
        });
    }

    /** Sneak-scroll, or the same check from a test. {@code steps} is +1 or -1. */
    public static void apply(ServerPlayer player, BlockPos pos, boolean targeted, int steps) {
        if (!player.isShiftKeyDown() || player.level().isClientSide()) {
            return;
        }
        ItemStack stack = linker(player);
        if (stack == null) {
            return;
        }
        int direction = steps >= 0 ? 1 : -1;
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (targeted && pos != null && player.distanceToSqr(Vec3.atCenterOf(pos)) <= REACH_SQR && level.isLoaded(pos)) {
            Links.Link link = Links.get(level.getServer()).linkAt(level, pos);
            if (link != null) {
                LinkService.Attempt attempt = LinkService.retune(level, pos, direction);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(attempt.key(), attempt.args()), true);
                return;
            }
        }
        LinkOrientation next = LinkerItems.cycle(stack, direction);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("portal_linker.orient.pending", next.label()), true);
    }

    private static ItemStack linker(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof PortalLinkerItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof PortalLinkerItem) {
            return off;
        }
        return null;
    }

    public record CycleLinkPayload(BlockPos pos, boolean targeted, int steps) implements CustomPacketPayload {
        public static final Type<CycleLinkPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PortalLinker.ID, "cycle_link"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CycleLinkPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                CycleLinkPayload::pos,
                ByteBufCodecs.BOOL,
                CycleLinkPayload::targeted,
                ByteBufCodecs.VAR_INT,
                CycleLinkPayload::steps,
                CycleLinkPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
