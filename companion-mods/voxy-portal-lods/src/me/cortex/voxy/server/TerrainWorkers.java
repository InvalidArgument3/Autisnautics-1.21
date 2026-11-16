package me.cortex.voxy.server;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.cortex.voxy.common.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * Reads generated columns through the chunk IO worker and voxelizes them on a
 * small pool. The server tick only offers finished columns, nearest first.
 */
public final class TerrainWorkers {
    private static final int WINDOW = 24;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "voxy-terrain");
        thread.setDaemon(true);
        return thread;
    });
    private static final ConcurrentHashMap<String, Lane> LANES = new ConcurrentHashMap<String, Lane>();
    private static int failures;

    private TerrainWorkers() {
    }

    public static int schedule(Object level, Object engine, Object player, String playerId, String world, int x, int z) {
        Lane lane = LANES.computeIfAbsent(playerId, key -> new Lane());
        int window = TerrainDispatch.asyncWindow(player);
        synchronized (lane) {
            if (lane.pending.size() >= window) {
                return RingScan.WAIT;
            }
            Job job = new Job(level, engine, player, playerId, world, x, z);
            lane.pending.addLast(job);
            lane.flying++;
            submit(job);
            return RingScan.SCHEDULED;
        }
    }

    public static boolean busy(String playerId) {
        Lane lane = LANES.get(playerId);
        if (lane == null) {
            return false;
        }
        synchronized (lane) {
            return !lane.pending.isEmpty();
        }
    }

    public static void drop(String playerId) {
        Lane lane = LANES.remove(playerId);
        if (lane == null) {
            return;
        }
        synchronized (lane) {
            for (Job job : lane.pending) {
                job.dropped = true;
            }
            lane.pending.clear();
            lane.flying = 0;
        }
    }

    public static void drain() {
        for (Lane lane : LANES.values()) {
            drain(lane);
        }
    }

    private static void submit(Job job) {
        try {
            ServerLevel level = (ServerLevel) job.level;
            CompletableFuture<Optional<CompoundTag>> read = level.getChunkSource().chunkMap.read(new ChunkPos(job.x, job.z));
            long started = System.nanoTime();
            read.whenComplete((tag, error) -> POOL.execute(() -> finish(job, tag, error, started)));
        } catch (Throwable failure) {
            job.skip = "unreadable";
            job.done = true;
            if (failures++ < 3) {
                Logger.warn("[terrain-sync] could not queue a column read", failure);
            }
        }
    }

    private static void finish(Job job, Optional<CompoundTag> tag, Throwable error, long readStart) {
        if (ColumnProfile.timing) {
            ColumnProfile.add("regionRead", System.nanoTime() - readStart);
        }
        try {
            if (job.dropped || error != null || tag == null || tag.isEmpty() || !ColumnStages.importable(tag.get())) {
                job.skip = "unreadable";
            } else {
                List<byte[]> wires = ColumnStages.wires(job.level, job.engine, job.x, job.z, tag.get());
                if (wires.isEmpty()) {
                    job.skip = "unreadable";
                } else {
                    job.wires = wires;
                }
            }
        } catch (Throwable failure) {
            job.skip = "unreadable";
            if (failures++ < 3) {
                Logger.warn("[terrain-sync] background column failed", failure);
            }
        }
        job.done = true;
    }

    private static void drain(Lane lane) {
        long budget = System.nanoTime() + 2_000_000L;
        synchronized (lane) {
            while (!lane.pending.isEmpty() && System.nanoTime() < budget) {
                Job job = lane.pending.peekFirst();
                if (!job.done) {
                    return;
                }
                lane.pending.pollFirst();
                lane.flying--;
                if (job.dropped || job.skip != null) {
                    TerrainDispatch.noteAsyncSkip(job.playerId, job.world);
                    if (ColumnProfile.timing) {
                        ColumnProfile.skip("unreadable");
                    }
                    continue;
                }
                long offerStart = System.nanoTime();
                boolean sent = false;
                try {
                    if (job.player instanceof ServerPlayer body && job.level instanceof ServerLevel level) {
                        sent = me.cortex.voxy.server.ServerTerrainTransport.offerColumn(body, level, new ChunkPos(job.x, job.z), job.wires);
                    }
                } catch (Throwable failure) {
                    sent = false;
                }
                if (ColumnProfile.timing) {
                    ColumnProfile.add("offer", System.nanoTime() - offerStart);
                }
                if (!sent) {
                    if (ColumnProfile.asyncMeasure) {
                        if (ColumnProfile.timing) {
                            ColumnProfile.skip("backpressure");
                        }
                        continue;
                    }
                    job.done = true;
                    lane.pending.addFirst(job);
                    lane.flying++;
                    if (ColumnProfile.timing) {
                        ColumnProfile.skip("backpressure");
                    }
                    return;
                }
                TerrainDispatch.noteAsyncSent(job.playerId, job.world, job.x, job.z);
                if (ColumnProfile.timing) {
                    ColumnProfile.sent();
                }
            }
        }
    }

    private static final class Lane {
        private final ArrayDeque<Job> pending = new ArrayDeque<Job>();
        private int flying;
    }

    private static final class Job {
        private final Object level;
        private final Object engine;
        private final Object player;
        private final String playerId;
        private final String world;
        private final int x;
        private final int z;
        private volatile boolean done;
        private volatile boolean dropped;
        private volatile String skip;
        private volatile List<byte[]> wires;

        private Job(Object level, Object engine, Object player, String playerId, String world, int x, int z) {
            this.level = level;
            this.engine = engine;
            this.player = player;
            this.playerId = playerId;
            this.world = world;
            this.x = x;
            this.z = z;
        }
    }
}
