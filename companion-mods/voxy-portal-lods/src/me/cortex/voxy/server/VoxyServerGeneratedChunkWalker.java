package me.cortex.voxy.server;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.SectionSyncBatchPayload;
import me.cortex.voxy.common.network.SectionSyncPayload;
import me.cortex.voxy.common.network.VoxySessionSupport;
import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.voxelization.WorldConversionFactory;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background dedicated-server walker that streams generated region chunks without forcing normal chunk loading.
 * It keeps track of what each Voxy client already received and pauses naturally once everyone is caught up.
 */
public final class VoxyServerGeneratedChunkWalker {
    private static final boolean TERRAIN_SYNC_DEBUG = VoxyCommon.isVerificationFlagOn("terrainSyncDebug", false);
    private static final long DEBUG_REPORT_INTERVAL_MS = 5000L;
    private static final int MAX_CANDIDATE_ATTEMPTS_PER_PLAYER = 4096;

    private record CursorKey(UUID player,ResourceLocation dimension) {}
    private static final Map<CursorKey, WalkCursor> PLAYER_CURSOR = new ConcurrentHashMap<>();
    private static final Map<Path, CachedRegionHeader> REGION_HEADER_CACHE = new java.util.LinkedHashMap<>(128,0.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Path,CachedRegionHeader> e){return size()>1024;}
    };
    private static final Map<ResourceLocation, SectionConverter> CONVERTERS = new ConcurrentHashMap<>();

    private static int levelRotation;
    private static final Map<ResourceLocation,Integer> PLAYER_ROTATION=new HashMap<>();
    private static long walkerDeadlineNs;
    private static long tickCounter;
    private static long debugLastReportMs;
    private static long debugCandidatesChecked;
    private static long debugGeneratedChunksRead;
    private static long debugChunksSent;
    private static long debugPayloadsSent;
    private static long debugBytesSent;
    private static long debugPausedForLoadTicks;
    private static long debugLastCandidatesChecked;
    private static long debugLastChunksRead;
    private static long debugLastPayloadsSent;
    private static long debugLastPausedForLoadTicks;
    private static volatile Method cachedAverageTickTimeMethod;
    private static volatile boolean didResolveAverageTickTimeMethod;

    private VoxyServerGeneratedChunkWalker() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VoxyServerGeneratedChunkWalker::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(VoxyServerGeneratedChunkWalker::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(VoxyServerGeneratedChunkWalker::onServerStopped);
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PLAYER_CURSOR.keySet().removeIf(k -> k.player().equals(player.getUUID()));
            ServerTerrainTransport.clearPlayer(player);
        }
    }

    /** Main-server-thread only. Cancels producer scheduling without closing engines or dirty save queues. */
    public static void resetScheduling() {
        PLAYER_CURSOR.clear();
        REGION_HEADER_CACHE.clear();
        CONVERTERS.clear();
        tickCounter = 0L;
        levelRotation = 0;
        PLAYER_ROTATION.clear();
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        PLAYER_CURSOR.clear();
        REGION_HEADER_CACHE.clear();
        CONVERTERS.clear();
        ServerTerrainSyncDeferredQueue.clear();
        tickCounter = 0L;
        levelRotation=0;PLAYER_ROTATION.clear();
        debugLastReportMs = 0L;
        debugCandidatesChecked = 0L;
        debugGeneratedChunksRead = 0L;
        debugChunksSent = 0L;
        debugPayloadsSent = 0L;
        debugBytesSent = 0L;
        debugPausedForLoadTicks = 0L;
        debugLastCandidatesChecked = 0L;
        debugLastChunksRead = 0L;
        debugLastPayloadsSent = 0L;
        debugLastPausedForLoadTicks = 0L;
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) {
            return;
        }
        ServerTerrainSyncDeferredQueue.drainForTick();
        if (!VoxyConfig.CONFIG.serverTerrainSyncBroadcast || !VoxyConfig.CONFIG.serverTerrainSyncBackgroundWalkEnabled) {
            return;
        }
        int interval = Math.max(1, VoxyConfig.CONFIG.serverTerrainSyncBackgroundTickInterval);
        tickCounter++;
        if ((tickCounter % interval) != 0) {
            return;
        }
        if (!hasAnyEligibleRecipients(server)) {
            maybeLogDebugSummary();
            return;
        }
        float pauseMsptThreshold = VoxyConfig.CONFIG.serverTerrainSyncBackgroundPauseMspt;
        if (pauseMsptThreshold > 0.0f && getAverageMspt(server) > pauseMsptThreshold) {
            debugPausedForLoadTicks++;
            maybeLogDebugSummary();
            return;
        }
        int budget = Math.max(1, VoxyConfig.CONFIG.serverTerrainSyncBackgroundChunksPerTick);
        if (budget <= 0) {
            return;
        }
        walkerDeadlineNs=System.nanoTime()+5_000_000L;
        List<ServerLevel> levels=new ArrayList<>();server.getAllLevels().forEach(levels::add);
        if(!levels.isEmpty())java.util.Collections.rotate(levels,-Math.floorMod(levelRotation++,levels.size()));
        for (ServerLevel level : levels) {
            if (budget <= 0 || System.nanoTime()>=walkerDeadlineNs) {
                break;
            }
            budget -= processLevel(level, budget);
        }
        maybeLogDebugSummary();
    }

    private static int processLevel(ServerLevel level, int budget) {
        int sentChunks = 0;
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!VoxySessionSupport.wantsTerrainPackets(player)) continue;
            int innerRadius = ServerTerrainTransport.effectiveInnerRadius(player);
            if (VoxySessionSupport.getMaxTerrainDistanceChunks(player) <= innerRadius) continue;
            players.add(player);
        }
        // Also include players standing in adjacent dimstack dimensions (e.g. Overworld looking into Deepsea)
        for (ServerLevel adj : ImmPortalsServerCompat.getAdjacentDimstackLevels(level)) {
            for (ServerPlayer player : adj.players()) {
                if (!VoxySessionSupport.wantsTerrainPackets(player)) continue;
                if (VoxySessionSupport.getMaxTerrainDistanceChunks(player) <= 0) continue;
                if (!players.contains(player)) {
                    players.add(player);
                }
            }
        }
        if (players.isEmpty()) {
            return 0;
        }
        WorldIdentifier wid = WorldIdentifier.of(level);
        var instance = VoxyCommon.getInstance();
        if (wid == null || instance == null) {
            return 0;
        }
        WorldEngine engine = instance.getOrCreate(wid);
        int rotation = PLAYER_ROTATION.getOrDefault(level.dimension().location(), 0);
        if (!players.isEmpty()) java.util.Collections.rotate(players, -Math.floorMod(rotation, players.size()));
        PLAYER_ROTATION.put(level.dimension().location(), rotation + 1);
        boolean madeProgress = true;
        while (sentChunks < budget && System.nanoTime() < walkerDeadlineNs && madeProgress) {
            madeProgress = false;
            for (ServerPlayer player : players) {
                if (sentChunks >= budget || System.nanoTime() >= walkerDeadlineNs) {
                    break;
                }
                boolean isAdjacent = (player.serverLevel() != level);
                int minExclusive = isAdjacent ? -1 : ServerTerrainTransport.effectiveInnerRadius(player);
                int maxDistance = VoxySessionSupport.getMaxTerrainDistanceChunks(player);
                if (isAdjacent) {
                    maxDistance = Math.min(maxDistance, ImmPortalsServerCompat.ADJACENT_DIM_MAX_RADIUS_CHUNKS);
                }
                if (maxDistance <= minExclusive) {
                    continue;
                }
                ChunkCandidate candidate = findNextMissingGeneratedChunk(level, player, minExclusive, maxDistance);
                if (candidate == null) {
                    continue;
                }
                int sendResult = sendChunkColumn(level, engine, player, candidate);
                if (sendResult > 0) {
                    sentChunks++;
                    madeProgress = true;
                } else if (sendResult < 0) {
                    // Queue temporarily full: rewind cursor so the spiral never skips a chunk and leaves gaps.
                    WalkCursor cursor = PLAYER_CURSOR.get(new CursorKey(player.getUUID(), level.dimension().location()));
                    if (cursor != null) {
                        cursor.stepBack(Math.max(1, minExclusive + 1));
                    }
                    return sentChunks;
                }
            }
        }
        return sentChunks;
    }

    private static ChunkCandidate findNextMissingGeneratedChunk(ServerLevel level, ServerPlayer player, int minRadiusExclusive, int maxRadiusInclusive) {
        int startRadius = Math.max(1, minRadiusExclusive + 1);
        WalkCursor cursor = PLAYER_CURSOR.computeIfAbsent(new CursorKey(player.getUUID(), level.dimension().location()),
                key -> new WalkCursor(player.chunkPosition().x, player.chunkPosition().z, startRadius));
        cursor.retargetIfMoved(player.chunkPosition().x, player.chunkPosition().z, startRadius);
        // Keep cursors for the player's current dimension AND its adjacent dimstack dimensions
        if (player.serverLevel() == level) {
            java.util.Set<ResourceLocation> keepDims = new java.util.HashSet<>(4);
            keepDims.add(level.dimension().location());
            for (ServerLevel adj : ImmPortalsServerCompat.getAdjacentDimstackLevels(player)) {
                keepDims.add(adj.dimension().location());
            }
            PLAYER_CURSOR.keySet().removeIf(k -> k.player().equals(player.getUUID()) && !keepDims.contains(k.dimension()));
        }
        // For adjacent dimstack dimensions (minRadiusExclusive == -1), also check the center chunk (dist 0) first
        if (minRadiusExclusive < 0) {
            int cx = player.chunkPosition().x;
            int cz = player.chunkPosition().z;
            if (!ServerTerrainTransport.known(player, level, new ChunkPos(cx, cz)) && hasGeneratedChunk(level, cx, cz)) {
                return new ChunkCandidate(cx, cz, ChunkPos.asLong(cx, cz));
            }
        }
        for (int attempts = 0; attempts < MAX_CANDIDATE_ATTEMPTS_PER_PLAYER && System.nanoTime() < walkerDeadlineNs; attempts++) {
            ChunkCandidate candidate = cursor.next(startRadius, maxRadiusInclusive);
            debugCandidatesChecked++;
            int dist = chessboardDistance(player.chunkPosition().x, player.chunkPosition().z, candidate.x(), candidate.z());
            if (dist <= minRadiusExclusive || dist > maxRadiusInclusive) {
                continue;
            }
            if (ServerTerrainTransport.known(player, level, new ChunkPos(candidate.x(), candidate.z()))) continue;
            if (!hasGeneratedChunk(level, candidate.x(), candidate.z())) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    /**
     * Returns 1 if offered to transport, 0 if chunk had no non-empty sections / not importable,
     * or -1 if transport queue was full (caller should rewind cursor so no chunk is skipped).
     */
    private static int sendChunkColumn(ServerLevel level, WorldEngine engine, ServerPlayer player, ChunkCandidate candidate) {
        CompoundTag chunk = readChunkNbt(level, candidate.x(), candidate.z());
        if (chunk == null || !isImportableChunk(chunk)) {
            return 0;
        }
        debugGeneratedChunksRead++;
        SectionConverter converter = CONVERTERS.computeIfAbsent(level.dimension().location(), key -> new SectionConverter(level));
        java.util.List<byte[]> wires = new java.util.ArrayList<>();
        for (Tag sectionEntry : chunk.getList("sections", Tag.TAG_COMPOUND)) {
            CompoundTag section = (CompoundTag) sectionEntry;
            int sectionY = section.getInt("Y");
            VoxelizedSection voxelized = converter.convertSection(engine, candidate.x(), sectionY, candidate.z(), section);
            if (voxelized != null && voxelized.lvl0NonAirCount > 0) {
                // Encode immediately: converter reuses a ThreadLocal VoxelizedSection across iterations.
                wires.add(SectionSyncPayload.encodeSectionWireBytes(level, engine, voxelized));
            }
        }
        if (wires.isEmpty()) {
            return 0;
        }
        return ServerTerrainTransport.offerColumn(player, level, new ChunkPos(candidate.x(), candidate.z()), wires) ? 1 : -1;
    }

    private static boolean isImportableChunk(CompoundTag chunk) {
        if (!chunk.contains("Status")) {
            return false;
        }
        ChunkStatus status = ChunkStatus.byName(chunk.getString("Status"));
        return status == ChunkStatus.FULL || status == ChunkStatus.EMPTY;
    }

    private static boolean hasGeneratedChunk(ServerLevel level, int chunkX, int chunkZ) {
        Path regionPath = getRegionPath(level, chunkX >> 5, chunkZ >> 5);
        if (!Files.exists(regionPath)) {
            return false;
        }
        int[] headers = loadRegionHeader(regionPath);
        if (headers == null) {
            return false;
        }
        int idx = (chunkX & 31) + ((chunkZ & 31) << 5);
        int entry = headers[idx];
        return entry != 0 && (entry & 0xFF) != 0;
    }

    private static CompoundTag readChunkNbt(ServerLevel level, int chunkX, int chunkZ) {
        Path regionPath = getRegionPath(level, chunkX >> 5, chunkZ >> 5);
        if (!Files.exists(regionPath)) {
            return null;
        }
        int[] headers = loadRegionHeader(regionPath);
        if (headers == null) {
            return null;
        }
        int idx = (chunkX & 31) + ((chunkZ & 31) << 5);
        int entry = headers[idx];
        if (entry == 0) {
            return null;
        }
        int sectorStart = entry >>> 8;
        int sectorCount = entry & 0xFF;
        if (sectorStart <= 0 || sectorCount <= 0) {
            return null;
        }
        try (FileChannel channel = FileChannel.open(regionPath, StandardOpenOption.READ)) {
            ByteBuffer meta = ByteBuffer.allocate(5).order(ByteOrder.BIG_ENDIAN);
            if (channel.read(meta, sectorStart * 4096L) != 5) {
                return null;
            }
            meta.flip();
            int length = meta.getInt();
            int versionId = meta.get() & 0xFF;
            if (length <= 1 || length > (sectorCount * 4096 - 4)) {
                return null;
            }
            if ((versionId & 128) != 0) {
                return null;
            }
            int compressionId = versionId & 0x7F;
            RegionFileVersion version = RegionFileVersion.fromId(compressionId);
            if (version == null) {
                return null;
            }
            byte[] compressed = new byte[length - 1];
            ByteBuffer data = ByteBuffer.wrap(compressed);
            if (channel.read(data, sectorStart * 4096L + 5L) != compressed.length) {
                return null;
            }
            try (DataInputStream in = new DataInputStream(version.wrap(new ByteArrayInputStream(compressed)))) {
                return NbtIo.read(in);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private static int[] loadRegionHeader(Path regionPath) {
        try {
            long mtime = Files.getLastModifiedTime(regionPath).toMillis();
            CachedRegionHeader cached = REGION_HEADER_CACHE.get(regionPath);
            if (cached != null && cached.lastModifiedMs() == mtime) {
                return cached.headers();
            }
            ByteBuffer buffer = ByteBuffer.allocate(4096).order(ByteOrder.BIG_ENDIAN);
            try (FileChannel channel = FileChannel.open(regionPath, StandardOpenOption.READ)) {
                if (channel.read(buffer) < 4096) {
                    return null;
                }
            }
            int[] headers = new int[1024];
            buffer.flip();
            for (int i = 0; i < headers.length; i++) {
                headers[i] = buffer.getInt();
            }
            REGION_HEADER_CACHE.put(regionPath, new CachedRegionHeader(mtime, headers));
            return headers;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Path getRegionPath(ServerLevel level, int regionX, int regionZ) {
        return getRegionDirectory(level).resolve("r." + regionX + "." + regionZ + ".mca");
    }

    private static Path getRegionDirectory(ServerLevel level) {
        Path root = level.getServer().getWorldPath(LevelResource.ROOT);
        if (level.dimension() == Level.OVERWORLD) {
            return root.resolve("region");
        }
        if (level.dimension() == Level.NETHER) {
            return root.resolve("DIM-1").resolve("region");
        }
        if (level.dimension() == Level.END) {
            return root.resolve("DIM1").resolve("region");
        }
        ResourceLocation dimId = level.dimension().location();
        return root.resolve("dimensions").resolve(dimId.getNamespace()).resolve(dimId.getPath()).resolve("region");
    }

    private static int chessboardDistance(int ax, int az, int bx, int bz) {
        return Math.max(Math.abs(ax - bx), Math.abs(az - bz));
    }

    private static float getAverageMspt(MinecraftServer server) {
        Method method = cachedAverageTickTimeMethod;
        if (!didResolveAverageTickTimeMethod) {
            didResolveAverageTickTimeMethod = true;
            method = resolveAverageTickTimeMethod(server.getClass());
            cachedAverageTickTimeMethod = method;
        }
        if (method == null) {
            return 0.0f;
        }
        try {
            Object value = method.invoke(server);
            if (value instanceof Number n) {
                if ("getAverageTickTimeNanos".equals(method.getName())) {
                    return (float) (n.doubleValue() / 1_000_000.0d);
                }
                return n.floatValue();
            }
        } catch (Exception ignored) {
        }
        return 0.0f;
    }

    private static Method resolveAverageTickTimeMethod(Class<?> serverClass) {
        String[] methodNames = new String[]{"getAverageTickTime", "getAverageTickTimeNanos"};
        for (String name : methodNames) {
            try {
                Method method = serverClass.getMethod(name);
                method.setAccessible(true);
                return method;
            } catch (Exception ignored) {
            }
        }
        return null;
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
        if (debugCandidatesChecked == debugLastCandidatesChecked
                && debugGeneratedChunksRead == debugLastChunksRead
                && debugPayloadsSent == debugLastPayloadsSent
                && debugPausedForLoadTicks == debugLastPausedForLoadTicks) {
            return;
        }
        debugLastCandidatesChecked = debugCandidatesChecked;
        debugLastChunksRead = debugGeneratedChunksRead;
        debugLastPayloadsSent = debugPayloadsSent;
        debugLastPausedForLoadTicks = debugPausedForLoadTicks;
        Logger.info("[terrain-sync][walker] candidates=", debugCandidatesChecked,
                " chunksRead=", debugGeneratedChunksRead,
                " transport=", ServerTerrainTransport.status(),
                " pausedForLoadTicks=", debugPausedForLoadTicks);
    }

    private static boolean hasAnyEligibleRecipients(MinecraftServer server) {
        walkerDeadlineNs=System.nanoTime()+5_000_000L;
        List<ServerLevel> levels=new ArrayList<>();server.getAllLevels().forEach(levels::add);
        if(!levels.isEmpty())java.util.Collections.rotate(levels,-Math.floorMod(levelRotation++,levels.size()));
        for (ServerLevel level : levels) {
            for (ServerPlayer player : level.players()) {
                if (!VoxySessionSupport.wantsTerrainPackets(player)) {
                    continue;
                }
                int maxDistance = VoxySessionSupport.getMaxTerrainDistanceChunks(player);
                if (maxDistance > Math.max(0, VoxyConfig.CONFIG.serverTerrainSyncInnerRadiusChunks)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record ChunkCandidate(int x, int z, long key) {}
    private record CachedRegionHeader(long lastModifiedMs, int[] headers) {}

    private static final class WalkCursor {
        private int centerX;
        private int centerZ;
        private int radius;
        private int edgeIndex;

        private WalkCursor(int centerX, int centerZ, int minRadius) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = Math.max(1, minRadius);
            this.edgeIndex = 0;
        }

        private void retargetIfMoved(int newCenterX, int newCenterZ, int minRadius) {
            if (Math.max(Math.abs(this.centerX - newCenterX), Math.abs(this.centerZ - newCenterZ)) < 4) {
                return;
            }
            this.centerX = newCenterX;
            this.centerZ = newCenterZ;
            this.radius = Math.max(1, minRadius);
            this.edgeIndex = 0;
        }

        private void stepBack(int minRadius) {
            if (this.edgeIndex > 0) {
                this.edgeIndex--;
            } else if (this.radius > minRadius) {
                this.radius--;
                this.edgeIndex = Math.max(0, this.radius * 8 - 1);
            }
        }

        private ChunkCandidate next(int minRadius, int maxRadius) {
            if (maxRadius < minRadius) {
                maxRadius = minRadius;
            }
            if (this.radius < minRadius) {
                this.radius = minRadius;
                this.edgeIndex = 0;
            }
            if (this.radius > maxRadius) {
                this.radius = minRadius;
                this.edgeIndex = 0;
            }
            int perimeter = this.radius == 0 ? 1 : this.radius * 8;
            int i = this.edgeIndex;
            if (i >= perimeter) {
                i = 0;
            }
            int x;
            int z;
            int r = this.radius;
            int side = r * 2;
            if (i < side) {
                x = this.centerX - r + i;
                z = this.centerZ - r;
            } else if (i < side * 2) {
                x = this.centerX + r;
                z = this.centerZ - r + (i - side);
            } else if (i < side * 3) {
                x = this.centerX + r - (i - side * 2);
                z = this.centerZ + r;
            } else {
                x = this.centerX - r;
                z = this.centerZ + r - (i - side * 3);
            }

            this.edgeIndex++;
            if (this.edgeIndex >= perimeter) {
                this.edgeIndex = 0;
                this.radius++;
                if (this.radius > maxRadius) {
                    this.radius = minRadius;
                }
            }
            return new ChunkCandidate(x, z, ChunkPos.asLong(x, z));
        }
    }

    private static final class SectionConverter {
        private static final ThreadLocal<VoxelizedSection> SECTION_CACHE = ThreadLocal.withInitial(VoxelizedSection::createEmpty);

        private final PalettedContainerRO<Holder<Biome>> defaultBiomeProvider;
        private final Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec;
        private final Codec<PalettedContainer<BlockState>> blockStateCodec;

        private SectionConverter(ServerLevel level) {
            var biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> defaultBiome = biomeRegistry.getHolder(Biomes.PLAINS).orElseThrow();
            this.defaultBiomeProvider = new PalettedContainerRO<>() {
                @Override
                public Holder<Biome> get(int x, int y, int z) {
                    return defaultBiome;
                }

                @Override
                public void getAll(java.util.function.Consumer<Holder<Biome>> action) {
                }

                @Override
                public void write(net.minecraft.network.FriendlyByteBuf buf) {
                }

                @Override
                public int getSerializedSize() {
                    return 0;
                }

                @Override
                public boolean maybeHas(java.util.function.Predicate<Holder<Biome>> predicate) {
                    return false;
                }

                @Override
                public void count(PalettedContainer.CountConsumer<Holder<Biome>> counter) {
                }

                @Override
                public PalettedContainer<Holder<Biome>> recreate() {
                    return null;
                }

                @Override
                public PalettedContainerRO.PackedData<Holder<Biome>> pack(IdMap<Holder<Biome>> idMap, PalettedContainer.Strategy strategy) {
                    return null;
                }
            };
            this.biomeCodec = PalettedContainer.codecRO(
                    biomeRegistry.asHolderIdMap(),
                    biomeRegistry.holderByNameCodec(),
                    PalettedContainer.Strategy.SECTION_BIOMES,
                    biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
            );
            this.blockStateCodec = PalettedContainer.codecRW(
                    Block.BLOCK_STATE_REGISTRY,
                    BlockState.CODEC,
                    PalettedContainer.Strategy.SECTION_STATES,
                    Blocks.AIR.defaultBlockState()
            );
        }

        private VoxelizedSection convertSection(WorldEngine world, int x, int y, int z, CompoundTag section) {
            if (section.getCompound("block_states").isEmpty()) {
                return null;
            }
            byte[] blockLightData = section.getByteArray("BlockLight");
            byte[] skyLightData = section.getByteArray("SkyLight");
            DataLayer blockLight = blockLightData.length != 0 ? new DataLayer(blockLightData) : null;
            DataLayer skyLight = skyLightData.length != 0 ? new DataLayer(skyLightData) : null;

            var blockStatesRes = this.blockStateCodec.parse(NbtOps.INSTANCE, section.getCompound("block_states"));
            if (!blockStatesRes.hasResultOrPartial()) {
                return null;
            }
            var blockStates = blockStatesRes.getPartialOrThrow();
            var biomes = this.defaultBiomeProvider;
            CompoundTag biomeTag = section.getCompound("biomes");
            if (!biomeTag.isEmpty()) {
                biomes = this.biomeCodec.parse(NbtOps.INSTANCE, biomeTag).result().orElse(this.defaultBiomeProvider);
            }
            VoxelizedSection converted = WorldConversionFactory.convert(
                    SECTION_CACHE.get().setPosition(x, y, z),
                    world.getMapper(),
                    blockStates,
                    biomes,
                    (bx, by, bz) -> {
                        int block = blockLight == null ? 0 : blockLight.get(bx, by, bz);
                        int sky = skyLight == null ? 0 : skyLight.get(bx, by, bz);
                        return (byte) (sky | (block << 4));
                    }
            );
            WorldVoxilizedSectionMipper.mipSection(converted, world.getMapper());
            return converted;
        }
    }
}
