package me.cortex.voxy.client;

import io.netty.buffer.Unpooled;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import me.cortex.voxy.common.config.section.SectionSerializationStorage;
import me.cortex.voxy.common.config.storage.inmemory.MemoryStorageBackend;
import me.cortex.voxy.common.network.ColumnCoveragePayload;
import me.cortex.voxy.common.network.SectionSyncBatchPayload;
import me.cortex.voxy.common.network.SectionWire;
import me.cortex.voxy.common.network.TerrainApplyQueue;
import me.cortex.voxy.common.network.VoxelSectionWireCodec;
import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import me.cortex.voxy.server.TerrainSentMemory;
import me.cortex.voxy.server.TerrainStreamPolicy;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

/**
 * Sixty seconds through the real queue, protocol-3 frames, and WorldUpdater
 * on a worker, into a real in-memory WorldEngine. Minecraft is only the
 * built-in registries the section codec already requires.
 */
public final class ApplyHarness {
    private static final int COLUMNS_PER_SECOND = 40;
    private static final int SECTIONS_PER_COLUMN = 42;
    private static final int SECONDS = Integer.getInteger("voxy.applySeconds", 60);

    public static void main(String[] args) throws Exception {
        setDist();
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        RegistryAccess registries = biomes();
        WorldEngine engine = new WorldEngine(new SectionSerializationStorage(new MemoryStorageBackend()));
        engine.setSectionCacheLimit(1_000_000);
        engine.setDirtyCallback((WorldSection section, int change, int neighbors) -> {
        });
        engine.setSaveCallback((WorldEngine world, WorldSection section, boolean immediate, boolean unload) -> {
            world.storage.saveSection(section);
            return true;
        });
        ClientSectionApply.useEngineForHarness(engine);

        boolean session = SectionWire.accepted(3) && SectionWire.accepted(2) && !SectionWire.accepted(1);
        link("session", session, "protocol 3 is accepted and protocol 1 is not");

        int window = windowEntries();
        link("window", window >= 32, "in-flight window " + window);

        List<SectionSyncBatchPayload> column = column(registries, engine, 100, 4, 200);
        link("frames", !column.isEmpty() && column.get(0).sectionDatas().get(0)[0] == SectionWire.ZSTD, "first byte is the zstd frame");
        SectionSyncBatchPayload expanded = SectionWire.expand(column.get(0));
        link("expand", expanded.sectionDatas().size() == column.get(0).sectionDatas().size() && expanded.sectionDatas().get(0)[0] != SectionWire.ZSTD, "expand restores raw sections");

        List<List<SectionSyncBatchPayload>> columns = new ArrayList<List<SectionSyncBatchPayload>>();
        for (int i = 0; i < 80; i++) {
            columns.add(column(registries, engine, 80 + i, 4, -30));
        }

        TerrainApplyQueue<SectionSyncBatchPayload> queue = queue();
        long started = System.nanoTime();
        long deadline = started + SECONDS * 1_000_000_000L;
        int offered = 0;
        int rejected = 0;
        int next = 0;
        long nextColumn = started;
        long memory = used();
        int peakQueue = 0;
        while (System.nanoTime() < deadline) {
            long now = System.nanoTime();
            while (now >= nextColumn && System.nanoTime() < deadline) {
                List<SectionSyncBatchPayload> batches = columns.get(next % columns.size());
                next++;
                boolean room = true;
                for (int i = 0; i < batches.size(); i++) {
                    SectionSyncBatchPayload payload = SectionWire.expand(batches.get(i));
                    TerrainApplyQueue.Offer result = queue.offer(offered + 1L, payload, Math.max(1, payload.totalSectionBytes()), payload.sectionDatas().size(), now / 1_000_000L);
                    if (result == TerrainApplyQueue.Offer.ACCEPTED) {
                        offered++;
                    } else {
                        rejected++;
                        room = false;
                        break;
                    }
                }
                if (!room) {
                    break;
                }
                nextColumn += 1_000_000_000L / COLUMNS_PER_SECOND;
            }
            ClientSectionApply.harnessTick(registries, -4, 20, engine);
            peakQueue = Math.max(peakQueue, ClientSectionApply.queuedBatches());
            memory = Math.max(memory, used());
            Thread.sleep(50L);
        }
        long quiet = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < quiet && ClientSectionApply.queuedBatches() > 0) {
            ClientSectionApply.harnessTick(registries, -4, 20, engine);
            Thread.sleep(20L);
        }
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        long sections = sectionsWritten();
        double columnsPerSecond = sections / (double) SECTIONS_PER_COLUMN / seconds;
        double sectionsPerSecond = sections / seconds;
        System.out.println("applySeconds=" + String.format("%.1f", seconds)
                + " sections=" + sections
                + " sectionsPerSecond=" + String.format("%.1f", sectionsPerSecond)
                + " columnsPerSecond=" + String.format("%.1f", columnsPerSecond)
                + " renderThreadUs=" + ClientSectionApply.renderThreadMicros()
                + " peakQueueBatches=" + peakQueue
                + " rejectedOffers=" + rejected
                + " heapBytes=" + memory
                + " applyError=" + ClientSectionApply.applyError());
        link("apply", sections > 0 && ClientSectionApply.renderThreadMicros() < 1000L, "sections applied and the client tick stayed under 1 ms");

        String stats = ClientSectionApply.line();
        boolean notifiesOk = stats.contains("renderNotifies=") && !stats.contains("renderNotifies=0 ") && stats.contains("renderMisses=0") && stats.contains("engineMismatches=0");
        link("render", notifiesOk, stats);

        ArrayList<ColumnCoveragePayload> coverage = ClientColumnCoverage.collect(engine, 80, -30, 512);
        String coverageStats = ClientColumnCoverage.stats();
        int packets = 0;
        for (int i = 0; i < coverage.size(); i++) {
            ColumnCoveragePayload payload = coverage.get(i);
            TerrainSentMemory.cover("player", "minecraft:overworld#1", payload.regionX(), payload.regionZ(), payload.columns());
            packets++;
        }
        boolean held = TerrainSentMemory.holdKind("player", "minecraft:overworld#1", 80, -30) == TerrainSentMemory.CLIENT;
        link("coverage", coverageStats.contains("engine=ok") && coverageStats.contains("channel=ok") && coverageStats.contains("scans=") && !coverageStats.contains("scans=0") && packets > 0 && TerrainSentMemory.coveragePackets() > 0 && held, coverageStats + " coveragePkts=" + TerrainSentMemory.coveragePackets() + " skippedClient=" + held);

        TerrainStreamPolicy.Motion motion = new TerrainStreamPolicy.Motion();
        long t0 = 1_000_000_000L;
        TerrainStreamPolicy.observeMotion(motion, 0, 0, "minecraft:overworld", false, t0, 10);
        int mode = TerrainStreamPolicy.observeMotion(motion, 3, 0, "minecraft:overworld", false, t0 + 20_000_000_000L, 10);
        link("motion", mode == 0 && !motion.droppedQueue(), "20s stall mode=" + mode + " cause=" + motion.cause());

        System.out.println(failed == 0 ? "ApplyHarness passed" : "ApplyHarness failed " + failed);
        if (failed != 0) {
            throw new AssertionError("ApplyHarness failed " + failed + " links");
        }
    }

    private static int failed;

    private static RegistryAccess biomes() throws Exception {
        ResourceKey<Biome> plains = ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:plains"));
        Biome biome = readPlains();
        MappedRegistry<Biome> registry = new MappedRegistry<Biome>(Registries.BIOME, com.mojang.serialization.Lifecycle.stable());
        registry.register(plains, biome, RegistrationInfo.BUILT_IN);
        registry.freeze();
        return new RegistryAccess() {
            @Override
            public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> key) {
                if (Registries.BIOME.equals(key)) {
                    return Optional.of((Registry<E>) registry);
                }
                return Optional.empty();
            }

            @Override
            public Stream<RegistryAccess.RegistryEntry<?>> registries() {
                return Stream.of(new RegistryAccess.RegistryEntry<Biome>(Registries.BIOME, registry));
            }
        };
    }

    private static void setDist() throws Exception {
        Field dist = FMLLoader.class.getDeclaredField("dist");
        dist.setAccessible(true);
        dist.set(null, Dist.CLIENT);
    }

    /** The codec only asks the holder for its registry key, so the biome body can be blank. */
    private static Biome readPlains() throws Exception {
        Field field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Object unsafe = field.get(null);
        return (Biome) unsafe.getClass().getMethod("allocateInstance", Class.class).invoke(unsafe, Biome.class);
    }

    private static void link(String name, boolean pass, String detail) {
        System.out.println((pass ? "PASS " : "FAIL ") + name + " " + detail);
        if (!pass) {
            failed++;
        }
    }

    private static long used() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long sectionsWritten() {
        String line = ClientSectionApply.line();
        int at = line.indexOf("sectionsWritten=") + "sectionsWritten=".length();
        int end = line.indexOf(' ', at);
        return Long.parseLong(line.substring(at, end));
    }

    private static int windowEntries() throws Exception {
        Class<?> transport = Class.forName("me.cortex.voxy.server.ServerTerrainTransport");
        java.lang.reflect.Field queueField = transport.getDeclaredField("QUEUE");
        queueField.setAccessible(true);
        Object queue = queueField.get(null);
        return queue.getClass().getField("windowEntries").getInt(queue);
    }

    @SuppressWarnings("unchecked")
    private static TerrainApplyQueue<SectionSyncBatchPayload> queue() throws Exception {
        Class<?> handler = Class.forName("me.cortex.voxy.common.network.TerrainSyncClientHandler");
        java.lang.reflect.Field field = handler.getDeclaredField("QUEUE");
        field.setAccessible(true);
        return (TerrainApplyQueue<SectionSyncBatchPayload>) field.get(null);
    }

    private static List<SectionSyncBatchPayload> column(RegistryAccess registries, WorldEngine engine, int x, int y, int z) {
        ArrayList<byte[]> framed = new ArrayList<byte[]>();
        for (int i = 0; i < SECTIONS_PER_COLUMN; i++) {
            framed.add(SectionWire.frame(encode(registries, engine, x, y, z)));
        }
        ArrayList<SectionSyncBatchPayload> batches = new ArrayList<SectionSyncBatchPayload>();
        ResourceLocation overworld = ResourceLocation.parse("minecraft:overworld");
        for (int at = 0; at < framed.size(); at += 7) {
            int end = Math.min(framed.size(), at + 7);
            batches.add(new SectionSyncBatchPayload(1L, overworld, overworld, 1L, new ArrayList<byte[]>(framed.subList(at, end))));
        }
        return batches;
    }

    private static byte[] encode(RegistryAccess registries, WorldEngine engine, int x, int y, int z) {
        VoxelizedSection section = VoxelizedSection.createEmpty().setPosition(x, y, z);
        ResourceKey<Biome> plains = ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:plains"));
        long id = engine.getMapper().getBaseId((byte) 0, Blocks.STONE.defaultBlockState(), registries.lookupOrThrow(Registries.BIOME).getOrThrow(plains));
        section.section[0] = id;
        section.lvl0NonAirCount = 1;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            VoxelSectionWireCodec.writeSection(buf, engine, section);
            byte[] raw = new byte[buf.readableBytes()];
            buf.readBytes(raw);
            return raw;
        } finally {
            buf.release();
        }
    }
}
