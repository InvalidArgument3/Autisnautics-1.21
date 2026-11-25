package dev.wyseman.walkerswimfix;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod(WalkerSwimFix.MODID)
public class WalkerSwimFix {
    public static final String MODID = "walkerswimfix";

    public static final ModConfigSpec.BooleanValue DEFAULT_SWIM_ENABLED;
    public static final ModConfigSpec.BooleanValue USE_MASS_DENSITY;
    public static final ModConfigSpec.DoubleValue MAX_BUOYANT_DENSITY;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("swim");
        DEFAULT_SWIM_ENABLED = builder
            .comment("Default swim state for walkers when first placed or loaded without saved toggle state.")
            .define("defaultSwimEnabled", false);
        USE_MASS_DENSITY = builder
            .comment("If true, even when swim is toggled on, walkers whose average block mass exceeds maxBuoyantDensity will sink.")
            .define("useMassDensityCheck", true);
        MAX_BUOYANT_DENSITY = builder
            .comment("Maximum average Sable mass per non-air block that can float when swim is enabled (normal=1.0, heavy/iron=2.0).")
            .defineInRange("maxBuoyantDensity", 1.0, 0.01, 100.0);
        builder.pop();
        SPEC = builder.build();
    }

    private static final Map<UUID, WeakReference<WalkerCoreBlockEntity>> ACTIVE_WALKERS = new ConcurrentHashMap<>();
    private static final Map<WalkerCoreBlockEntity, WeakReference<ServerSubLevel>> SUBLEVELS = new java.util.WeakHashMap<>();

    public WalkerSwimFix(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        modBus.addListener(this::registerPayloads);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            WalkerSwimClient.init(modBus);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            ToggleSwimPayload.TYPE,
            ToggleSwimPayload.STREAM_CODEC,
            ToggleSwimPayload::handle
        );
    }

    public static void recordActiveWalker(ServerPlayer player, WalkerCoreBlockEntity core) {
        if (player != null && core != null && !core.isRemoved()) {
            ACTIVE_WALKERS.put(player.getUUID(), new WeakReference<>(core));
        }
    }

    public static void recordSubLevel(WalkerCoreBlockEntity core, ServerSubLevel sub) {
        if (core != null && sub != null) {
            synchronized (SUBLEVELS) {
                SUBLEVELS.put(core, new WeakReference<>(sub));
            }
        }
    }

    public static ServerSubLevel getSubLevel(WalkerCoreBlockEntity core) {
        if (core == null) return null;
        synchronized (SUBLEVELS) {
            WeakReference<ServerSubLevel> ref = SUBLEVELS.get(core);
            return ref == null ? null : ref.get();
        }
    }

    public static double computeDensity(WalkerCoreBlockEntity core, ServerSubLevel sub, long gameTime) {
        MassData massData = sub.getMassTracker();
        if (massData == null || massData.isInvalid()) {
            return 0.0;
        }
        int blocks = getOrCountBlocks(core, sub, gameTime);
        if (blocks <= 0) {
            return 0.0;
        }
        return massData.getMass() / blocks;
    }

    private static int getOrCountBlocks(WalkerCoreBlockEntity core, ServerSubLevel sub, long gameTime) {
        SwimToggleAccess access = (SwimToggleAccess) core;
        int cached = access.walkerswimfix$getCachedBlockCount();
        if (cached > 0 && Math.abs(gameTime - access.walkerswimfix$getLastCountTick()) < 20L) {
            return cached;
        }
        BoundingBox3i box = sub.lastNetworkedBoundingBox();
        int count = 0;
        if (box != null && sub.getLevel() != null) {
            for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ()
            )) {
                if (!sub.getLevel().getBlockState(pos).isAir()) {
                    count++;
                }
            }
        }
        access.walkerswimfix$setCachedBlockCount(count, gameTime);
        return count;
    }

    public record ToggleSwimPayload() implements CustomPacketPayload {
        public static final Type<ToggleSwimPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MODID, "toggle_swim")
        );
        public static final StreamCodec<ByteBuf, ToggleSwimPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleSwimPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ToggleSwimPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) {
                    return;
                }
                WeakReference<WalkerCoreBlockEntity> ref = ACTIVE_WALKERS.get(player.getUUID());
                WalkerCoreBlockEntity core = ref == null ? null : ref.get();
                if (core == null || core.isRemoved()) {
                    player.displayClientMessage(Component.literal("No active walker linked to pilot controls."), true);
                    return;
                }
                SwimToggleAccess access = (SwimToggleAccess) core;
                boolean next = !access.walkerswimfix$isSwimEnabled();
                access.walkerswimfix$setSwimEnabled(next);
                core.setChanged();

                if (!next) {
                    player.displayClientMessage(Component.literal("Walker Swim: OFF"), true);
                    return;
                }

                ServerSubLevel sub = getSubLevel(core);
                if (USE_MASS_DENSITY.get() && sub != null && sub.getLevel() != null) {
                    double density = computeDensity(core, sub, sub.getLevel().getGameTime());
                    double max = MAX_BUOYANT_DENSITY.get();
                    if (density > max) {
                        player.displayClientMessage(
                            Component.literal(String.format(
                                Locale.ROOT,
                                "Walker Swim: ON (Too heavy to float: %.2f > %.2f)",
                                density, max
                            )),
                            true
                        );
                        return;
                    }
                    player.displayClientMessage(
                        Component.literal(String.format(
                            Locale.ROOT,
                            "Walker Swim: ON (Density %.2f / %.2f)",
                            density, max
                        )),
                        true
                    );
                    return;
                }

                player.displayClientMessage(Component.literal("Walker Swim: ON"), true);
            });
        }
    }
}
