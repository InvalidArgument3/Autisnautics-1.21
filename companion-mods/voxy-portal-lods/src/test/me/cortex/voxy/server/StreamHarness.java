package me.cortex.voxy.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Checks view-distance clamping, movement pauses, per-player budgets, and the square-ring walk. */
public final class StreamHarness {
    public static void main(String[] args) throws Exception {
        check(TerrainStreamPolicy.innerRadiusChunks(0, 12, 10) == 10, "client 12 clamped to server 10");
        check(TerrainStreamPolicy.innerRadiusChunks(0, 6, 10) == 6, "client 6 inside server 10");
        check(TerrainStreamPolicy.innerRadiusChunks(0, 0, 8) == 8, "missing client request uses server view");
        check(TerrainStreamPolicy.innerRadiusChunks(0, 0, 0) == 2, "unknown view distance stays off the player's own chunks");
        check(TerrainStreamPolicy.innerRadiusChunks(32, 6, 10) == 32, "positive config remains a fixed override");

        TerrainStreamPolicy.Motion motion = new TerrainStreamPolicy.Motion();
        long t0 = 1_000_000_000L;
        check(TerrainStreamPolicy.observeMotion(motion, 0, 0, "minecraft:overworld", false, t0, 10) == 0, "first sample walks");
        long tick = 50_000_000L;
        // 0.2 blocks in one tick is a walk (~4 blocks/s). Four blocks in one tick is 80 blocks/s.
        check(TerrainStreamPolicy.observeMotion(motion, 0.2, 0, "minecraft:overworld", false, t0 + tick, 10) == 0, "walking does not pause");
        check(TerrainStreamPolicy.observeMotion(motion, 4, 0, "minecraft:overworld", true, t0 + tick * 2, 10) == 1, "elytra pauses");
        check(motion.clearQueue == false, "elytra keeps the queue");
        check(TerrainStreamPolicy.observeMotion(motion, 80, 0, "minecraft:overworld", false, t0 + tick * 3, 10) == 1, "teleport pauses");
        check(motion.clearQueue, "teleport drops queued work");
        TerrainStreamPolicy.Motion settled = new TerrainStreamPolicy.Motion();
        TerrainStreamPolicy.observeMotion(settled, 0, 0, "minecraft:overworld", false, t0, 8);
        int mode = 1;
        for (int i = 0; i < TerrainStreamPolicy.FAST_PAUSE_TICKS; i++) {
            mode = TerrainStreamPolicy.observeMotion(settled, 0, 0, "minecraft:overworld", true, t0 + tick * (i + 1), 8);
            check(mode == 1, "elytra holds the pause");
        }
        mode = TerrainStreamPolicy.observeMotion(settled, 0, 0, "minecraft:overworld", false, t0 + tick * 30, 8);
        // The elytra sample above refreshed the pause, then one settled sample remains inside the hold.
        check(mode == 1, "pause holds after elytra stops");
        for (int i = 0; i < TerrainStreamPolicy.FAST_PAUSE_TICKS; i++) {
            mode = TerrainStreamPolicy.observeMotion(settled, 0, 0, "minecraft:overworld", false, t0 + tick * (40 + i), 8);
        }
        check(mode == 0, "pause ends once the player is still");

        TerrainStreamPolicy.Motion view = new TerrainStreamPolicy.Motion();
        TerrainStreamPolicy.observeMotion(view, 0, 0, "minecraft:overworld", false, t0, 10);
        check(TerrainStreamPolicy.observeMotion(view, 0, 0, "minecraft:overworld", false, t0 + tick, 6) == 2, "smaller view distance restarts the ring");
        check(!view.clearQueue, "shrinking view distance keeps chunks already sent");
        TerrainStreamPolicy.Motion grown = new TerrainStreamPolicy.Motion();
        TerrainStreamPolicy.observeMotion(grown, 0, 0, "minecraft:overworld", false, t0, 6);
        int grew = TerrainStreamPolicy.observeMotion(grown, 0, 0, "minecraft:overworld", false, t0 + tick, 12);
        check(grew == 2, "larger view distance restarts outside the new vanilla edge");
        check(grown.clearQueue, "larger view distance cancels columns the client now receives as chunks");
        TerrainStreamPolicy.Motion dim = new TerrainStreamPolicy.Motion();
        TerrainStreamPolicy.observeMotion(dim, 0, 0, "minecraft:overworld", false, t0, 10);
        check(TerrainStreamPolicy.observeMotion(dim, 0, 0, "minecraft:the_nether", false, t0 + tick, 10) == 1, "dimension change pauses");
        check(dim.clearQueue, "dimension change drops the old queue");

        Object alice = "alice";
        Object bob = "bob";
        int sent = 0;
        for (int i = 0; i < 4; i++) {
            check(TerrainStreamPolicy.tryPlayer(alice, 8, 1000), "alice batch " + i);
            sent += 8;
        }
        check(sent == 32, "alice used a full column");
        check(!TerrainStreamPolicy.tryPlayer(alice, 1, 1), "alice is capped for this tick");
        check(TerrainStreamPolicy.tryPlayer(bob, 8, 1000), "bob still has a budget");
        TerrainStreamPolicy.resetTick();
        check(TerrainStreamPolicy.tryPlayer(alice, 8, 1000), "alice's cap resets next tick");
        long bytesPerSecondCeiling = 1048576L * 20L;
        System.out.println("global byte ceiling " + bytesPerSecondCeiling + " B/s, per player cap " + TerrainStreamPolicy.PER_PLAYER_BYTES_PER_TICK + " B/tick, sections " + TerrainStreamPolicy.PER_PLAYER_SECTIONS_PER_TICK);

        walkRing();
        System.out.println("StreamHarness passed");
    }

    private static void walkRing() throws Exception {
        Class<?> cursorType = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker$WalkCursor");
        Constructor<?> ctor = cursorType.getDeclaredConstructor(int.class, int.class, int.class);
        ctor.setAccessible(true);
        Object cursor = ctor.newInstance(100, 200, 3);
        Method next = cursorType.getDeclaredMethod("next", int.class, int.class);
        next.setAccessible(true);
        Method retarget = cursorType.getDeclaredMethod("retargetIfMoved", int.class, int.class, int.class);
        retarget.setAccessible(true);
        int previous = 0;
        for (int radius = 3; radius <= 5; radius++) {
            int count = radius == 0 ? 1 : radius * 8;
            for (int i = 0; i < count; i++) {
                Object point = next.invoke(cursor, 3, 6);
                int x = (Integer) point.getClass().getMethod("x").invoke(point);
                int z = (Integer) point.getClass().getMethod("z").invoke(point);
                int distance = Math.max(Math.abs(x - 100), Math.abs(z - 200));
                check(distance == radius, "ring " + radius + " point " + i + " was " + distance + " at " + x + "," + z);
                if (i > 0) {
                    int step = Math.max(Math.abs(x - previousX(previous)), 0);
                    previous = pack(x, z);
                } else {
                    previous = pack(x, z);
                }
            }
        }
        retarget.invoke(cursor, 101, 200, 4);
        retarget.invoke(cursor, 100, 200, 4);
        Object restarted = next.invoke(cursor, 4, 6);
        int rx = (Integer) restarted.getClass().getMethod("x").invoke(restarted);
        int rz = (Integer) restarted.getClass().getMethod("z").invoke(restarted);
        check(Math.max(Math.abs(rx - 100), Math.abs(rz - 200)) == 4, "view-distance restart returns to the new inner ring");
        retarget.invoke(cursor, 400, -50, 3);
        Object moved = next.invoke(cursor, 3, 8);
        int mx = (Integer) moved.getClass().getMethod("x").invoke(moved);
        int mz = (Integer) moved.getClass().getMethod("z").invoke(moved);
        check(Math.max(Math.abs(mx - 400), Math.abs(mz + 50)) == 3, "moving the player recentres the ring");
        System.out.println("spiral rings 3..5 around 100,200 then recentre 400,-50");
    }

    private static int pack(int x, int z) {
        return x * 100000 + z;
    }

    private static int previousX(int packed) {
        return packed / 100000;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        System.out.println("ok " + message);
    }

    private StreamHarness() {
    }
}
