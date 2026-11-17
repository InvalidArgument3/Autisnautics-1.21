package me.cortex.voxy.client;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Arrays;
import me.cortex.voxy.client.RenderStatistics;
import me.cortex.voxy.client.core.rendering.SectionUpdateRouter;
import me.cortex.voxy.client.core.rendering.building.RenderGenerationService;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import net.minecraft.client.Minecraft;

/**
 * A write into an unwatched section does not reach the screen. Bubble it to
 * the nearest watched ancestor so that node is rebuilt and can subdivide.
 */
public final class TerrainVisibility {
    private static final long DEBOUNCE_NANOS = 100_000_000L;
    private static volatile SectionUpdateRouter router;
    private static volatile RenderGenerationService mesher;
    private static final Long2LongOpenHashMap NEXT = new Long2LongOpenHashMap();
    private static final Long2LongOpenHashMap RESIDENT = new Long2LongOpenHashMap();
    private static final Long2IntOpenHashMap COVER = new Long2IntOpenHashMap();
    private static final Long2LongOpenHashMap APPLIED = new Long2LongOpenHashMap();
    private static int geometryResident;
    private static int newSinceLine;
    private static final int[] DRAWN = new int[5];

    static {
        // The traversal and command-gen shaders only record draw counts when this
        // is set before they compile, which is when the render system is created.
        RenderStatistics.enabled = true;
    }

    private TerrainVisibility() {
    }

    public static void holdService(RenderGenerationService generation) {
        mesher = generation;
    }

    public static void bindRouter(SectionUpdateRouter updateRouter) {
        router = updateRouter;
    }

    public static void onDirty(WorldSection section) {
        SectionUpdateRouter current = router;
        RenderGenerationService service = mesher;
        if (current == null || service == null || section == null) {
            return;
        }
        int x = section.x;
        int y = section.y;
        int z = section.z;
        for (int level = section.lvl; level <= 4; level++) {
            long id = WorldEngine.getWorldSectionId(level, x, y, z);
            if (current.get(id) != 0) {
                enqueue(service, id);
                return;
            }
            x >>= 1;
            y >>= 1;
            z >>= 1;
        }
    }

    public static void noteApplied(int chunkX, int chunkZ) {
        long key = packChunk(chunkX, chunkZ);
        synchronized (APPLIED) {
            if (!APPLIED.containsKey(key)) {
                APPLIED.put(key, System.nanoTime());
            }
        }
    }

    public static void geometryAdded(long position, boolean hasMesh) {
        if (!hasMesh) {
            return;
        }
        int level = WorldEngine.getLevel(position);
        long cover = coverKey(level, WorldEngine.getX(position), WorldEngine.getZ(position));
        synchronized (RESIDENT) {
            if (RESIDENT.put(position, System.nanoTime()) == 0L) {
                newSinceLine++;
                COVER.addTo(cover, 1);
                geometryResident = RESIDENT.size();
            }
        }
    }

    public static void geometryRemoved(long position) {
        int level = WorldEngine.getLevel(position);
        long cover = coverKey(level, WorldEngine.getX(position), WorldEngine.getZ(position));
        synchronized (RESIDENT) {
            if (RESIDENT.remove(position) != 0L) {
                int left = COVER.addTo(cover, -1);
                if (left <= 0) {
                    COVER.remove(cover);
                }
                geometryResident = RESIDENT.size();
            }
        }
    }

    public static void sampleDraw() {
        for (int level = 0; level < DRAWN.length && level < RenderStatistics.visibleSections.length; level++) {
            int count = RenderStatistics.visibleSections[level];
            if (count > DRAWN[level]) {
                DRAWN[level] = count;
            }
        }
    }

    public static String line() {
        sampleDraw();
        int resident;
        int fresh;
        long[] positions;
        synchronized (RESIDENT) {
            resident = geometryResident;
            fresh = newSinceLine;
            newSinceLine = 0;
            positions = RESIDENT.keySet().toLongArray();
        }
        int[] drawn = DRAWN.clone();
        Arrays.fill(DRAWN, 0);
        int playerX = 0;
        int playerZ = 0;
        boolean havePlayer = false;
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.player != null) {
                playerX = minecraft.player.chunkPosition().x;
                playerZ = minecraft.player.chunkPosition().z;
                havePlayer = true;
            }
        } catch (Throwable ignored) {
            havePlayer = false;
        }
        long[] farthest = farthest(positions, playerX, playerZ, havePlayer, 5);
        int undrawn = 0;
        StringBuilder examples = new StringBuilder();
        long now = System.nanoTime();
        synchronized (APPLIED) {
            for (it.unimi.dsi.fastutil.longs.Long2LongMap.Entry entry : APPLIED.long2LongEntrySet()) {
                if (now - entry.getLongValue() < 10_000_000_000L) {
                    continue;
                }
                int chunkX = (int) entry.getLongKey();
                int chunkZ = (int) (entry.getLongKey() >>> 32);
                if (covered(chunkX, chunkZ)) {
                    continue;
                }
                undrawn++;
                if (examples.length() < 80) {
                    if (examples.length() > 0) {
                        examples.append(',');
                    }
                    examples.append(chunkX).append(',').append(chunkZ);
                }
            }
        }
        long prune = now;
        NEXT.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < prune);
        return "geometryResident=" + resident
                + " drawnL0=" + drawn[0]
                + " drawnL1=" + drawn[1]
                + " drawnL2=" + drawn[2]
                + " drawnL3=" + drawn[3]
                + " drawnL4=" + drawn[4]
                + " newResident=" + fresh
                + " farthestDrawn=" + format(farthest, 0, playerX, playerZ, havePlayer)
                + " far5=" + formatAll(farthest, playerX, playerZ, havePlayer)
                + " undrawnOld=" + undrawn
                + " undrawnAt=" + (examples.length() == 0 ? "-" : examples);
    }

    private static void enqueue(RenderGenerationService service, long id) {
        long now = System.nanoTime();
        long due = NEXT.get(id);
        if (due > now) {
            return;
        }
        NEXT.put(id, now + DEBOUNCE_NANOS);
        service.enqueueTask(id);
    }

    private static boolean covered(int chunkX, int chunkZ) {
        synchronized (RESIDENT) {
            for (int level = 0; level <= 4; level++) {
                if (COVER.get(coverKey(level, chunkX >> (level + 1), chunkZ >> (level + 1))) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long[] farthest(long[] positions, int playerX, int playerZ, boolean havePlayer, int count) {
        long[] best = new long[count];
        int[] distance = new int[count];
        Arrays.fill(distance, -1);
        for (long position : positions) {
            int chunks = distanceChunks(position, playerX, playerZ, havePlayer);
            for (int i = 0; i < count; i++) {
                if (chunks > distance[i]) {
                    for (int j = count - 1; j > i; j--) {
                        distance[j] = distance[j - 1];
                        best[j] = best[j - 1];
                    }
                    distance[i] = chunks;
                    best[i] = position;
                    break;
                }
            }
        }
        return best;
    }

    private static int distanceChunks(long position, int playerX, int playerZ, boolean havePlayer) {
        int level = WorldEngine.getLevel(position);
        int originX = WorldEngine.getX(position) << (level + 1);
        int originZ = WorldEngine.getZ(position) << (level + 1);
        int centerX = originX + (1 << level);
        int centerZ = originZ + (1 << level);
        if (!havePlayer) {
            return Math.max(Math.abs(centerX), Math.abs(centerZ));
        }
        return Math.max(Math.abs(centerX - playerX), Math.abs(centerZ - playerZ));
    }

    private static String format(long[] positions, int index, int playerX, int playerZ, boolean havePlayer) {
        if (index >= positions.length || positions[index] == 0L && WorldEngine.getLevel(positions[index]) == 0 && WorldEngine.getX(positions[index]) == 0) {
            boolean any = false;
            for (long position : positions) {
                if (position != 0L) {
                    any = true;
                }
            }
            if (!any) {
                return "-";
            }
        }
        long position = positions[index];
        if (position == 0L && index > 0) {
            return "-";
        }
        return WorldEngine.getLevel(position) + "@" + WorldEngine.getX(position) + "," + WorldEngine.getY(position) + "," + WorldEngine.getZ(position)
                + " d=" + distanceChunks(position, playerX, playerZ, havePlayer);
    }

    private static String formatAll(long[] positions, int playerX, int playerZ, boolean havePlayer) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == 0L && i > 0) {
                break;
            }
            if (text.length() > 0) {
                text.append(';');
            }
            text.append(format(positions, i, playerX, playerZ, havePlayer));
        }
        return text.length() == 0 ? "-" : text.toString();
    }

    private static long packChunk(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }

    private static long coverKey(int level, int x, int z) {
        return ((long) level << 56) | ((long) x & 0xFFFFFFL) << 28 | ((long) z & 0xFFFFFFL);
    }
}
