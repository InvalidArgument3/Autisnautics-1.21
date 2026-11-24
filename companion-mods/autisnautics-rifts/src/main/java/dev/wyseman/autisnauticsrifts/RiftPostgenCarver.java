package dev.wyseman.autisnauticsrifts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RiftPostgenCarver {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutisnauticsRifts");
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final long BUDGET_NS = 2_000_000L;

    private static final int[][] OFFSETS = {
        {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
        {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };

    private static final Map<String, String> WALL_IDS = Map.of(
        "minecraft:overworld", "minecraft:stone",
        "oceanworld:deepsea", "minecraft:stone",
        "dimension_of_caves:cave", "minecraft:deepslate",
        "infinite_abyss:first_layer", "minecraft:deepslate",
        "deeperdarker:otherside", "deeperdarker:sculk_stone",
        "undergarden:undergarden", "undergarden:depthrock",
        "infernalcross:dimension", "minecraft:blackstone",
        "minecraft:the_nether", "minecraft:netherrack",
        "autisnautics:molten_depths", "minecraft:obsidian",
        "minecraft:the_end", "minecraft:end_stone"
    );

    private static final Map<String, BlockState> WALL_CACHE = new HashMap<>();
    private static final Map<String, Task> PENDING = new HashMap<>();
    private static final Deque<Task> QUEUE = new ArrayDeque<>();
    private static final Deque<Task> PARKED = new ArrayDeque<>();
    private static final Map<String, Task> RECENT = new HashMap<>();
    private static final Deque<String> RECENT_ORDER = new ArrayDeque<>();

    private static long clock = 0;
    private static boolean trimAvailable = true;
    private static boolean parkOverflow = false;
    private static long lastTickWallMs = System.currentTimeMillis();
    private static long lastLogMs = System.currentTimeMillis();
    private static final Stats STATS = new Stats();

    private RiftPostgenCarver() {}

    private static final class Stats {
        long slices, workNs, maxSliceNs, passes, lastPassMs, maxPassMs, lastPassWallMs;
        long maskEvals, maskHits, checks, airSkipped, removed, fluidsCleared, sealed;
        long trimBoxes, fluidTicksRemoved, maxTrimMs, trimErrors, maxTickGapMs, misses, verifyRetries;

        void reset() {
            slices = workNs = maxSliceNs = passes = lastPassMs = maxPassMs = lastPassWallMs = 0;
            maskEvals = maskHits = checks = airSkipped = removed = fluidsCleared = sealed = 0;
            trimBoxes = fluidTicksRemoved = maxTrimMs = trimErrors = maxTickGapMs = misses = verifyRetries = 0;
        }
    }

    private static final class Task {
        final String key;
        final ServerLevel level;
        final String dim;
        final int cx, cz;
        final boolean legacy;
        final List<RiftBoundary.Face> faces;
        final int minY, maxY;
        final byte[] halo;
        final byte[] kinds;

        long due;
        int passes = 3;
        boolean touched = false;
        boolean rescan = false;
        int misses = 0;
        int verifyFails = 0;
        int unresolved = 0;
        boolean missLogged = false;
        boolean verifyLogged = false;

        int phase = 0, face = 0, col = 0, offset = 0;
        int y = Integer.MIN_VALUE;
        int trimFace = 0, trimRow = 0, trimX = 0;
        long passNs = 0;
        long passStartedMs = System.currentTimeMillis();

        Task(String key, ServerLevel level, String dim, int cx, int cz, boolean legacy,
             List<RiftBoundary.Face> faces, Task prev) {
            this.key = key;
            this.level = level;
            this.dim = dim;
            this.cx = cx;
            this.cz = cz;
            this.legacy = legacy;
            this.faces = faces;
            this.minY = level.getMinBuildHeight();
            this.maxY = level.getMaxBuildHeight();
            this.halo = prev != null ? prev.halo : new byte[faces.size() * 400];
            this.kinds = prev != null ? prev.kinds : new byte[faces.size() * 256];
            this.due = clock + 2;
        }

        void resetPass() {
            phase = 0; face = 0; col = 0; offset = 0;
            y = Integer.MIN_VALUE; unresolved = 0;
            trimFace = 0; trimRow = 0; trimX = 0;
            passNs = 0; passStartedMs = System.currentTimeMillis();
        }
    }

    public static BlockState wallFor(String dim, int y) {
        String id = (("minecraft:overworld".equals(dim) || "oceanworld:deepsea".equals(dim)) && y < 0)
            ? "minecraft:deepslate"
            : WALL_IDS.getOrDefault(dim, "minecraft:deepslate");
        return WALL_CACHE.computeIfAbsent(id, k -> {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(k));
            BlockState state = block.defaultBlockState();
            if (state.isAir()) {
                LOGGER.warn("[RiftPerf] Missing wall {}; using deepslate", k);
                return Blocks.DEEPSLATE.defaultBlockState();
            }
            return state;
        });
    }

    private static String keyOf(String dim, int x, int z) {
        return dim + "|" + x + "|" + z;
    }

    private static BlockState fillState(RiftBoundary.Face s, int y) {
        return s.wantsWater(y) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    private static int missDelay(int misses) {
        int delay = 2;
        for (int i = 1; i < misses && delay < 200; i++) {
            delay *= 2;
        }
        return Math.min(delay, 200);
    }

    private static void dropParked(String key) {
        Iterator<Task> it = PARKED.iterator();
        while (it.hasNext()) {
            if (it.next().key.equals(key)) it.remove();
        }
    }

    private static void request(ServerLevel level, String dim, int cx, int cz, boolean legacy) {
        String key = keyOf(dim, cx, cz);
        dropParked(key);
        Task existing = PENDING.get(key);
        if (existing != null) {
            existing.rescan = true;
            existing.due = Math.min(existing.due, clock + 2);
            return;
        }
        Task prev = RECENT.get(key);
        if (prev != null) legacy = prev.legacy;
        RiftMarkerSavedData.get(level).addMarker(cx, cz, legacy);
        List<RiftBoundary.Face> faces = RiftBoundary.getAllFaces(dim);
        Task task = new Task(key, level, dim, cx, cz, legacy, faces, prev);
        PENDING.put(key, task);
        QUEUE.addLast(task);
        if (prev == null) RECENT_ORDER.addLast(key);
        RECENT.put(key, task);
        while (RECENT_ORDER.size() > 4096) {
            String expired = RECENT_ORDER.removeFirst();
            RECENT.remove(expired);
        }
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        String dim = level.dimension().location().toString();
        if (!RiftBoundary.hasDimension(dim)) return;
        var cp = event.getChunk().getPos();
        boolean fresh = event.isNewChunk();
        RiftMarkerSavedData markers = RiftMarkerSavedData.get(level);
        boolean marked = markers.hasMarker(cp.x, cp.z, false);
        boolean oldMarker = markers.hasMarker(cp.x, cp.z, true);
        if (!fresh && !marked && !oldMarker) return;
        request(level, dim, cp.x, cp.z, !fresh && !marked);
        if (fresh) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Task prev = RECENT.get(keyOf(dim, cp.x + dx, cp.z + dz));
                    if (prev != null) {
                        request(level, dim, cp.x + dx, cp.z + dz, prev.legacy);
                    }
                }
            }
        }
    }

    private static boolean insideAt(Task task, DensityFunction density, int faceIdx, int lx, int lz) {
        int ci = faceIdx * 400 + (lx + 2) * 20 + (lz + 2);
        byte cached = task.halo[ci];
        if (cached == 0) {
            RiftBoundary.Face s = task.faces.get(faceIdx);
            double v = density.compute(new DensityFunction.SinglePointContext(task.cx * 16 + lx, s.anchorY(), task.cz * 16 + lz));
            boolean active = Math.abs(v - s.sentinel()) <= 0.1;
            task.halo[ci] = (byte) (active ? 2 : 1);
            STATS.maskEvals++;
            return active;
        }
        STATS.maskHits++;
        return cached == 2;
    }

    private static void prepareUnit(Task task, DensityFunction density) {
        if (task.face >= task.faces.size()) {
            task.phase = task.touched ? (task.legacy ? 2 : 1) : 4;
            task.face = 0; task.col = 0;
            return;
        }
        if (task.col >= 256) {
            task.face++; task.col = 0; task.offset = 0;
            return;
        }
        int index = task.face * 256 + task.col;
        byte kind = task.kinds[index];
        if (kind != 0) {
            if (kind != 1) task.touched = true;
            task.col++; task.offset = 0;
            return;
        }
        int lx = task.col >> 4;
        int lz = task.col & 15;
        int[] off = OFFSETS[task.offset];
        if (insideAt(task, density, task.face, lx + off[0], lz + off[1])) {
            task.kinds[index] = (byte) (task.offset == 0 ? 2 : 3);
            task.touched = true;
            task.col++; task.offset = 0;
            return;
        }
        task.offset++;
        if (task.legacy || task.offset >= OFFSETS.length) {
            task.kinds[index] = 1;
            task.col++; task.offset = 0;
        }
    }

    private static boolean insideOtherFace(Task task, int col, int y) {
        for (int f = 0; f < task.faces.size(); f++) {
            RiftBoundary.Face s = task.faces.get(f);
            if (task.kinds[f * 256 + col] == 2 && y >= s.lo() && y < s.hi()) {
                return true;
            }
        }
        return false;
    }

    private static void blockUnit(Task task, LevelChunk chunk) {
        if (task.face >= task.faces.size()) {
            task.phase++; task.face = 0; task.col = 0; task.y = Integer.MIN_VALUE;
            return;
        }
        if (task.col >= 256) {
            task.face++; task.col = 0; task.y = Integer.MIN_VALUE;
            return;
        }
        RiftBoundary.Face s = task.faces.get(task.face);
        byte kind = task.kinds[task.face * 256 + task.col];
        // kind: 1=outside, 2=interior, 3=outer-ring
        if ((task.phase == 1 && kind != 3) || (task.phase == 2 && kind != 2)) {
            task.col++; task.y = Integer.MIN_VALUE;
            return;
        }
        int lo = Math.max(task.legacy ? s.clearLo() : s.lo(), task.minY);
        int hi = Math.min(task.legacy ? s.clearHi() : s.hi(), task.maxY);
        if (task.y == Integer.MIN_VALUE) task.y = lo;
        if (task.y >= hi) {
            task.col++; task.y = Integer.MIN_VALUE;
            return;
        }
        int y = task.y++;
        int x = task.cx * 16 + (task.col >> 4);
        int z = task.cz * 16 + (task.col & 15);
        if (task.phase == 1 && insideOtherFace(task, task.col, y)) return;

        if (((y & 15) == 0 || y == lo) && chunk.getSection(chunk.getSectionIndex(y)).hasOnlyAir()) {
            if (task.legacy || task.phase != 2 || !s.wantsWater(y)) {
                int nextY = Math.min(hi, (Math.floorDiv(y, 16) + 1) * 16);
                STATS.airSkipped += nextY - y;
                task.y = nextY;
                return;
            }
        }

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = chunk.getBlockState(pos);
        STATS.checks++;
        if (state.isAir() && (task.legacy || task.phase != 2 || !s.wantsWater(y))) return;
        boolean wet = !state.getFluidState().isEmpty();
        if (task.legacy) {
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) return;
        } else if (task.phase == 1) {
            if (wet) {
                task.level.setBlock(pos, wallFor(task.dim, y), QUIET);
                STATS.sealed++;
            }
            return;
        } else if (!wet && (y < s.clearLo() || y >= s.clearHi())) {
            return;
        }

        if (!task.legacy && s.wantsWater(y) && state.is(Blocks.WATER)) return;
        task.level.setBlock(pos, task.legacy ? Blocks.AIR.defaultBlockState() : fillState(s, y), QUIET);
        STATS.removed++;
        if (wet) STATS.fluidsCleared++;
    }

    private static void trimUnit(Task task, LevelChunk chunk) {
        if (task.legacy || !trimAvailable || task.trimFace >= task.faces.size()) {
            task.phase = 4;
            return;
        }
        if (task.trimRow >= 16) {
            task.trimFace++; task.trimRow = 0; task.trimX = 0;
            return;
        }
        int row = task.trimRow;
        int face = task.trimFace;
        RiftBoundary.Face s = task.faces.get(face);
        int lo = Math.max(s.lo(), task.minY);
        int hi = Math.min(s.hi(), task.maxY);
        if (hi <= lo) {
            task.trimFace++; task.trimRow = 0; task.trimX = 0;
            return;
        }
        while (task.trimX < 16 && task.kinds[face * 256 + (task.trimX << 4) + row] <= 1) task.trimX++;
        if (task.trimX >= 16) {
            task.trimRow++; task.trimX = 0;
            return;
        }
        int start = task.trimX++;
        while (task.trimX < 16 && task.kinds[face * 256 + (task.trimX << 4) + row] > 1) task.trimX++;
        BoundingBox box = new BoundingBox(task.cx * 16 + start, lo, task.cz * 16 + row, task.cx * 16 + task.trimX - 1, hi - 1, task.cz * 16 + row);
        long before = System.currentTimeMillis();
        try {
            int count = chunk.getFluidTicks().count();
            task.level.getFluidTicks().clearArea(box);
            STATS.fluidTicksRemoved += Math.max(0, count - chunk.getFluidTicks().count());
            STATS.trimBoxes++;
        } catch (Throwable err) {
            trimAvailable = false;
            STATS.trimErrors++;
            LOGGER.warn("[RiftPerf] Fluid tick clearArea unavailable: {}", err.toString());
        }
        STATS.maxTrimMs = Math.max(STATS.maxTrimMs, System.currentTimeMillis() - before);
    }

    private static void verifyUnit(Task task, LevelChunk chunk) {
        if (task.face >= task.faces.size()) {
            task.phase = 5;
            return;
        }
        if (task.col >= 256) {
            task.face++; task.col = 0; task.y = Integer.MIN_VALUE;
            return;
        }
        RiftBoundary.Face s = task.faces.get(task.face);
        byte kind = task.kinds[task.face * 256 + task.col];
        if (kind != 2) {
            task.col++; task.y = Integer.MIN_VALUE;
            return;
        }
        int lo = Math.max(s.clearLo(), task.minY);
        int hi = Math.min(s.clearHi(), task.maxY);
        if (task.y == Integer.MIN_VALUE) task.y = lo;
        if (task.y >= hi) {
            task.col++; task.y = Integer.MIN_VALUE;
            return;
        }
        int y = task.y++;
        if (((y & 15) == 0 || y == lo) && chunk.getSection(chunk.getSectionIndex(y)).hasOnlyAir()) {
            if (task.legacy || !s.wantsWater(y)) {
                task.y = Math.min(hi, (Math.floorDiv(y, 16) + 1) * 16);
                return;
            }
        }
        BlockPos pos = new BlockPos(task.cx * 16 + (task.col >> 4), y, task.cz * 16 + (task.col & 15));
        BlockState state = chunk.getBlockState(pos);
        STATS.checks++;
        if (!task.legacy && s.wantsWater(y)) {
            if (!state.is(Blocks.WATER)) task.unresolved++;
            return;
        }
        if (state.isAir()) return;
        if (task.legacy && (state.is(Blocks.WATER) || state.is(Blocks.LAVA))) return;
        task.unresolved++;
    }

    private static int sweep(Task task, long deadlineNs) {
        ServerChunkCache source = task.level.getChunkSource();
        LevelChunk chunk = source.getChunkNow(task.cx, task.cz);
        if (chunk == null) return 0;
        DensityFunction density = source.randomState().router().finalDensity();
        int units = 0;
        while (System.nanoTime() < deadlineNs && units++ < 512) {
            if (task.phase == 0) prepareUnit(task, density);
            else if (task.phase == 1 || task.phase == 2) blockUnit(task, chunk);
            else if (task.phase == 3) trimUnit(task, chunk);
            else if (task.phase == 4) verifyUnit(task, chunk);
            else return task.unresolved > 0 ? 3 : 2;
        }
        return 1;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        clock++;
        long nowMs = System.currentTimeMillis();
        long startedNs = System.nanoTime();
        STATS.maxTickGapMs = Math.max(STATS.maxTickGapMs, nowMs - lastTickWallMs);
        lastTickWallMs = nowMs;

        long deadlineNs = startedNs + BUDGET_NS;
        int looked = 0;
        while (!PARKED.isEmpty() && looked < 2 && System.nanoTime() < deadlineNs) {
            Task parkedTask = PARKED.removeFirst();
            looked++;
            LevelChunk back = parkedTask.level.getChunkSource().getChunkNow(parkedTask.cx, parkedTask.cz);
            if (back != null) {
                if (PENDING.get(parkedTask.key) != parkedTask) {
                    parkedTask.misses = 0;
                    parkedTask.due = clock;
                    PENDING.put(parkedTask.key, parkedTask);
                    QUEUE.addLast(parkedTask);
                }
            } else {
                PARKED.addLast(parkedTask);
            }
        }

        int probes = 0;
        int qSize = QUEUE.size();
        while (!QUEUE.isEmpty() && System.nanoTime() < deadlineNs && probes++ < Math.max(64, qSize)) {
            Task task = QUEUE.removeFirst();
            if (PENDING.get(task.key) != task) continue;
            if (task.due > clock) {
                QUEUE.addLast(task);
                continue;
            }
            long beforeNs = System.nanoTime();
            int result = sweep(task, deadlineNs);
            long spentNs = System.nanoTime() - beforeNs;
            task.passNs += spentNs;
            STATS.workNs += spentNs;
            STATS.slices++;

            if (result == 0) {
                task.misses++;
                STATS.misses++;
                if (task.misses >= 8) {
                    PENDING.remove(task.key);
                    if (PARKED.size() < 2048) PARKED.addLast(task);
                    else if (!parkOverflow) {
                        parkOverflow = true;
                        LOGGER.warn("[RiftPerf] park list full; markers kept until chunk loads again");
                    }
                    if (!task.missLogged) {
                        task.missLogged = true;
                        LOGGER.warn("[RiftPerf] parked {} after getChunkNow misses; marker kept", task.key);
                    }
                    continue;
                }
                task.due = clock + missDelay(task.misses);
                QUEUE.addLast(task);
                continue;
            }

            task.misses = 0;
            if (result == 3) {
                STATS.verifyRetries++;
                task.verifyFails++;
                task.resetPass();
                task.due = clock + (task.verifyFails >= 4 ? 200 : 20);
                if (task.verifyFails >= 4) task.verifyFails = 0;
                if (!task.verifyLogged) {
                    task.verifyLogged = true;
                    LOGGER.warn("[RiftPerf] exit band still blocked {}; marker kept", task.key);
                }
                QUEUE.addLast(task);
                continue;
            }

            if (result == 2) {
                task.verifyLogged = false;
                STATS.passes++;
                long passMs = task.passNs / 1_000_000L;
                STATS.lastPassMs = passMs;
                STATS.maxPassMs = Math.max(STATS.maxPassMs, passMs);
                STATS.lastPassWallMs = System.currentTimeMillis() - task.passStartedMs;
                task.passes = task.rescan ? 3 : task.passes - 1;
                task.rescan = false;
                if (task.passes <= 0) {
                    PENDING.remove(task.key);
                    RiftMarkerSavedData.get(task.level).removeMarker(task.cx, task.cz, task.legacy);
                    continue;
                }
                task.resetPass();
                task.due = clock + (task.passes == 2 ? 40 : task.passes == 1 ? 160 : 2);
            }
            QUEUE.addLast(task);
        }

        STATS.maxSliceNs = Math.max(STATS.maxSliceNs, System.nanoTime() - startedNs);
        if (nowMs - lastLogMs >= 30_000L) {
            if (STATS.slices > 0 || !PENDING.isEmpty() || !PARKED.isEmpty() || STATS.maxTickGapMs > 250) {
                MinecraftServer server = event.getServer();
                double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
                LOGGER.info(
                    "[RiftPerf] queue={} parked={} slices={} workMs={} maxSliceMs={} passes={} checks={} airSkipped={} lateBlocks={} sealed={} misses={} avgMspt={}",
                    PENDING.size(), PARKED.size(), STATS.slices, STATS.workNs / 1_000_000L,
                    STATS.maxSliceNs / 1_000_000L, STATS.passes, STATS.checks, STATS.airSkipped,
                    STATS.removed, STATS.sealed, STATS.misses, String.format("%.2f", mspt)
                );
            }
            lastLogMs = nowMs;
            STATS.reset();
        }
    }
}
