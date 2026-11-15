package dev.wyseman.portallinker;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.ModList;

/**
 * Soft check against Open Parties and Claims. Missing mod means the link is allowed.
 * A failed lookup refuses the link.
 */
public final class ClaimGuard {
    public static final int OK = 0;
    public static final int DENY = 1;
    public static final int ERROR = 2;

    private static boolean lookedUp;
    private static Method getApi;
    private static Method getProtection;
    private static Method hasAccess;
    private static Method affected;
    private static boolean broken;

    private ClaimGuard() {}

    public static int check(ServerPlayer player, ServerLevel fromLevel, Iterable<BlockPos> fromBlocks, ServerLevel toLevel, Iterable<BlockPos> toBlocks) {
        if (player == null || !ModList.get().isLoaded("openpartiesandclaims")) {
            return OK;
        }
        lookup();
        if (broken) {
            return ERROR;
        }
        try {
            Object api = getApi.invoke(null, player.getServer());
            Object protection = getProtection.invoke(api);
            Set<ChunkPos> fromChunks = chunks(fromBlocks);
            Set<ChunkPos> toChunks = chunks(toBlocks);
            ResourceLocation fromDim = fromLevel.dimension().location();
            ResourceLocation toDim = toLevel.dimension().location();
            for (ChunkPos chunk : fromChunks) {
                if (!allowed(protection, player, fromDim, chunk)) {
                    return DENY;
                }
            }
            for (ChunkPos chunk : toChunks) {
                if (!allowed(protection, player, toDim, chunk)) {
                    return DENY;
                }
            }
            for (ChunkPos from : fromChunks) {
                for (ChunkPos to : toChunks) {
                    Object blocked = affected.invoke(protection, toLevel, to, fromLevel, from, false, true, true);
                    if (Boolean.TRUE.equals(blocked)) {
                        return DENY;
                    }
                }
            }
            return OK;
        } catch (ReflectiveOperationException e) {
            broken = true;
            PortalLinker.LOG.warn("Open Parties and Claims rejected the lookup; links are refused until restart", e);
            return ERROR;
        }
    }

    private static boolean allowed(Object protection, ServerPlayer player, ResourceLocation dim, ChunkPos chunk) throws ReflectiveOperationException {
        Object result = hasAccess.invoke(protection, player, dim, chunk.x, chunk.z);
        return Boolean.TRUE.equals(result);
    }

    private static Set<ChunkPos> chunks(Iterable<BlockPos> blocks) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (BlockPos pos : blocks) {
            chunks.add(new ChunkPos(pos));
        }
        return chunks;
    }

    private static void lookup() {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        try {
            Class<?> api = Class.forName("xaero.pac.common.server.api.OpenPACServerAPI");
            getApi = api.getMethod("get", net.minecraft.server.MinecraftServer.class);
            Class<?> protection = Class.forName("xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI");
            getProtection = api.getMethod("getChunkProtection");
            hasAccess = protection.getMethod("hasChunkAccess", net.minecraft.world.entity.Entity.class, ResourceLocation.class, int.class, int.class);
            affected = protection.getMethod(
                    "onPosAffectedByAnotherPos",
                    ServerLevel.class,
                    ChunkPos.class,
                    ServerLevel.class,
                    ChunkPos.class,
                    boolean.class,
                    boolean.class,
                    boolean.class);
        } catch (ReflectiveOperationException e) {
            broken = true;
            PortalLinker.LOG.warn("Open Parties and Claims is loaded but its claim API did not match", e);
        }
    }
}
