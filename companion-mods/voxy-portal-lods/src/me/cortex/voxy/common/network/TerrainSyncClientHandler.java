package me.cortex.voxy.common.network;

import io.netty.buffer.Unpooled;
import me.cortex.voxy.client.compat.ImmPortalsCompat;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public final class TerrainSyncClientHandler {
    private static final TerrainApplyQueue<SectionSyncBatchPayload> QUEUE = new TerrainApplyQueue<>(128, 12 * 1024 * 1024);
    private static final boolean DEBUG_PROP = VoxyCommon.isVerificationFlagOn("terrainSyncDebug", false);
    private static long received, applied, completed, retried, dropped, bytesReceived, errors, decodeNs, applyNs, lastReport;
    private static int sampleLogs;

    private TerrainSyncClientHandler() {}

    private static boolean isDebug() {
        return DEBUG_PROP || VoxyConfig.CONFIG.serverTerrainSyncDebug;
    }

    public static void clearPendingQueue() {
        QUEUE.clear();
    }

    private static void ack(long id, int status) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new VoxyChunkAckPayload(id, status));
        }
    }

    private static WorldIdentifier world(SectionSyncBatchPayload p) {
        return new WorldIdentifier(
                ResourceKey.create(Registries.DIMENSION, p.levelDimensionId()),
                p.worldSeed(),
                ResourceKey.create(Registries.DIMENSION_TYPE, p.dimensionTypeId()));
    }

    /**
     * Resolves the loaded ClientLevel matching {@code payload}. Checks the player's own level first
     * (immune to Immersive Portals swapping {@code mc.level} during portal passes), then {@code mc.level},
     * then Immersive Portals' ClientWorldLoader so streamed LODs for adjacent dimstack dimensions apply directly.
     */
    private static ClientLevel resolveClientLevel(Minecraft mc, SectionSyncBatchPayload p) {
        WorldIdentifier target = world(p);
        if (mc.player != null && mc.player.level() instanceof ClientLevel playerLevel) {
            if (target.equals(WorldIdentifier.of(playerLevel))) {
                return playerLevel;
            }
        }
        if (mc.level != null && target.equals(WorldIdentifier.of(mc.level))) {
            return mc.level;
        }
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, p.levelDimensionId());
        ClientLevel ipLevel = ImmPortalsCompat.getClientLevel(dimKey);
        if (ipLevel != null && target.equals(WorldIdentifier.of(ipLevel))) {
            return ipLevel;
        }
        return null;
    }

    public static void handleSectionSync(SectionSyncPayload p, IPayloadContext c) {
        handleSectionSyncBatch(new SectionSyncBatchPayload(
                p.transferId(), p.levelDimensionId(), p.dimensionTypeId(), p.worldSeed(), List.of(p.sectionData())), c);
    }

    public static void handleSectionSyncBatch(SectionSyncBatchPayload p, IPayloadContext c) {
        c.enqueueWork(() -> {
            if (!VoxyConfig.CONFIG.serverTerrainSyncReceive) {
                dropped += p.sectionDatas().size();
                ack(p.transferId(), 2);
                return;
            }
            received += p.sectionDatas().size();
            bytesReceived += p.totalSectionBytes();
            var mc = Minecraft.getInstance();
            if (mc.level == null || VoxyCommon.getInstance() == null) {
                retried++;
                ack(p.transferId(), 1);
                return;
            }
            if (resolveClientLevel(mc, p) == null) {
                dropped += p.sectionDatas().size();
                ack(p.transferId(), 2);
                return;
            }
            var result = QUEUE.offer(p.transferId(), p, p.totalSectionBytes(), p.sectionDatas().size(), (System.nanoTime() / 1_000_000L));
            if (result == TerrainApplyQueue.Offer.COMPLETED) {
                ack(p.transferId(), 0);
            } else if (result == TerrainApplyQueue.Offer.FULL) {
                retried++;
                ack(p.transferId(), 1);
            }
        });
    }

    public static void tickDrainApplyQueue() {
        if (!VoxyConfig.CONFIG.serverTerrainSyncReceive) {
            QUEUE.clear();
            report();
            return;
        }
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            report();
            return;
        }
        int max = VoxyConfig.CONFIG.serverTerrainSyncClientApplySectionsPerTick;
        max = max <= 0 ? 64 : Math.min(Math.max(max, 64), 128);
        int ms = VoxyConfig.CONFIG.serverTerrainSyncClientApplyMaxMsPerTick;
        long deadline = ms > 0 ? System.nanoTime() + Math.min(Math.max(ms, 8), 50) * 1_000_000L : Long.MAX_VALUE;
        for (int attempts = 0; attempts < max && System.nanoTime() < deadline; attempts++) {
            var item = QUEUE.peek();
            if (item == null) break;
            var p = item.payload;
            ClientLevel targetLevel = resolveClientLevel(mc, p);
            if (targetLevel == null) {
                QUEUE.remove(item);
                dropped += item.sections - item.progress;
                ack(item.id, 2);
                continue;
            }
            try {
                var instance = VoxyCommon.getInstance();
                if (instance == null) break;
                var engine = instance.getOrCreate(world(p));
                var inner = new RegistryFriendlyByteBuf(
                        Unpooled.wrappedBuffer(p.sectionDatas().get(item.progress)),
                        targetLevel.registryAccess());
                try {
                    long t = System.nanoTime();
                    var section = VoxelSectionWireCodec.readSection(inner, engine);
                    if (section.y < targetLevel.getMinSection() || section.y >= targetLevel.getMaxSection()) {
                        throw new IllegalArgumentException("Terrain section outside dimension height");
                    }
                    WorldVoxilizedSectionMipper.mipSection(section, engine.getMapper());
                    decodeNs += System.nanoTime() - t;
                    t = System.nanoTime();
                    WorldUpdater.insertUpdate(engine, section);
                    applyNs += System.nanoTime() - t;
                    if (isDebug() && sampleLogs < 8 && section.lvl0NonAirCount > 0) {
                        sampleLogs++;
                        var vrs = me.cortex.voxy.client.core.IGetVoxyRenderSystem.getNullable();
                        boolean sameEngine = (vrs != null && vrs.getEngine() == engine);
                        Logger.info("[terrain-sync][client-v2] sample dim=", p.levelDimensionId(),
                                " sec=(", section.x, ",", section.y, ",", section.z, ") nonAir=", section.lvl0NonAirCount,
                                " sameMainEngine=", sameEngine);
                    }
                } finally {
                    inner.release();
                }
                applied++;
                if (QUEUE.advance(item, (System.nanoTime() / 1_000_000L))) {
                    completed++;
                    ack(item.id, 0);
                }
            } catch (Exception e) {
                errors++;
                retried++;
                QUEUE.remove(item);
                ack(item.id, 1);
                if (isDebug() && errors <= 3) {
                    Logger.error("Terrain v2 batch application failed; scheduled bounded retry", e);
                }
            }
        }
        report();
    }

    public static String status() {
        return "recvSections=" + received
                + " appliedSections=" + applied
                + " completedBatches=" + completed
                + " retry=" + retried
                + " dropped=" + dropped
                + " bytesRecv=" + bytesReceived
                + " queue=" + QUEUE.size()
                + " queueBytes=" + QUEUE.bytes()
                + " ageMs=" + QUEUE.oldestAge((System.nanoTime() / 1_000_000L))
                + " errors=" + errors
                + " decodeMs=" + decodeNs / 1_000_000
                + " applyMs=" + applyNs / 1_000_000
                + " cov={" + me.cortex.voxy.client.ClientColumnCoverage.stats() + "}";
    }

    private static void report() {
        long now = System.nanoTime() / 1_000_000L;
        if (isDebug() && now - lastReport >= 5000) {
            lastReport = now;
            Logger.info("[terrain-sync][client-v2] ", status());
        }
    }
}
