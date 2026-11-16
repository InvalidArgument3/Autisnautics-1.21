package me.cortex.voxy.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives {@link RingScan} the way the dedicated-server walker will: nearest ring first,
 * a rejected send stays on that cell, and a cell that is noted but not readable yet is not skipped.
 */
public final class RingHarness {
    public static void main(String[] args) throws Exception {
        ringsMatchWalkCursor();
        rejectedOfferIsRetried();
        unreadableNoteWaits();
        capSkipsOnce();
        backpressureHolds();
        pendingFrameGoesToOwnerFront();
        availabilityRoundTrip();
        System.out.println("RingHarness passed");
    }

    private static void ringsMatchWalkCursor() throws Exception {
        Class<?> type = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker$WalkCursor");
        java.lang.reflect.Constructor<?> ctor = type.getDeclaredConstructor(int.class, int.class, int.class);
        ctor.setAccessible(true);
        Object legacy = ctor.newInstance(Integer.valueOf(100), Integer.valueOf(200), Integer.valueOf(3));
        java.lang.reflect.Method next = type.getDeclaredMethod("next", int.class, int.class);
        next.setAccessible(true);
        RingCursor cursor = new RingCursor(100, 200, 3);
        for (int radius = 3; radius <= 5; radius++) {
            int count = radius * 8;
            for (int i = 0; i < count; i++) {
                Object point = next.invoke(legacy, Integer.valueOf(3), Integer.valueOf(6));
                int lx = (Integer) point.getClass().getMethod("x").invoke(point);
                int lz = (Integer) point.getClass().getMethod("z").invoke(point);
                int[] cell = cursor.peek(3, 6);
                check(cell[0] == lx && cell[1] == lz, "ring cell " + radius + ":" + i + " was " + cell[0] + "," + cell[1] + " legacy " + lx + "," + lz);
                check(Math.max(Math.abs(cell[0] - 100), Math.abs(cell[1] - 200)) == radius, "distance");
                cursor.commit(3, 6);
            }
        }
        cursor.retargetIfMoved(400, -50, 3);
        int[] moved = cursor.peek(3, 8);
        check(Math.max(Math.abs(moved[0] - 400), Math.abs(moved[1] + 50)) == 3, "retarget returns to the inner ring");
        System.out.println("ok square ring matches WalkCursor");
    }

    private static void rejectedOfferIsRetried() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        List<int[]> sent = new ArrayList<int[]>();
        boolean[] rejectOnce = new boolean[] {true};
        int[] result = RingScan.scan(cursor, 0, 0, 0, 4, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                if (rejectOnce[0] && x == -1 && z == -1) {
                    rejectOnce[0] = false;
                    return RingScan.FAIL;
                }
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(result == null, "a rejected first cell sends nothing");
        result = RingScan.scan(cursor, 0, 0, 0, 4, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(result != null && result[0] == -1 && result[1] == -1, "retry sends the same nearest cell");
        check(sent.size() == 1 && sent.get(0)[0] == -1 && sent.get(0)[1] == -1, "no farther cell jumped the queue");
        System.out.println("ok rejected offer retries the same cell");
    }

    private static void unreadableNoteWaits() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] waited = RingScan.scan(cursor, 0, 0, 0, 3, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return x == -1 && z == -1;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return x != -1;
            }

            @Override
            public int attempt(int x, int z) {
                if (x == -1 && z == -1) {
                    return RingScan.WAIT;
                }
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(waited == null, "unread note does not send a farther chunk");
        int[] again = cursor.peek(1, 3);
        check(again[0] == -1 && again[1] == -1, "cursor stays on the unread cell");
        System.out.println("ok unreadable note waits");
    }

    private static void capSkipsOnce() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        List<int[]> abandoned = new ArrayList<int[]>();
        List<int[]> sent = new ArrayList<int[]>();
        RingScan.Probe probe = new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                if (x == -1 && z == -1) {
                    return RingScan.FAIL;
                }
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        };
        RingScan.Abandoned log = (x, z) -> abandoned.add(new int[] {x, z});
        int[] first = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(first == null && abandoned.isEmpty(), "still retrying inside the cap");
        int[] second = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(second == null && abandoned.isEmpty(), "second failure still holds");
        int[] third = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(third != null && !(third[0] == -1 && third[1] == -1), "cap skips the bad cell and sends the next");
        check(abandoned.size() == 1 && abandoned.get(0)[0] == -1 && abandoned.get(0)[1] == -1, "skip is logged once");
        System.out.println("ok retry cap skips once");
    }

    private static void backpressureHolds() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] held = RingScan.scan(cursor, 0, 0, 0, 2, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                return RingScan.BACKPRESSURE;
            }
        }, 2, null, (x, z) -> {
            throw new AssertionError("backpressure must not count as a give-up");
        });
        check(held == null, "full queue sends nothing");
        int[] cell = cursor.peek(1, 2);
        check(cell[0] == -1 && cell[1] == -1, "full queue keeps the nearest cell");
        System.out.println("ok backpressure holds the cursor");
    }

    private static void pendingFrameGoesToOwnerFront() {
        java.util.LinkedHashMap<Long, Object> pending = new java.util.LinkedHashMap<Long, Object>();
        FakeFrame older = new FakeFrame(1L, "alice");
        FakeFrame newer = new FakeFrame(2L, "alice");
        FakeFrame bob = new FakeFrame(3L, "bob");
        pending.put(Long.valueOf(1L), older);
        pending.put(Long.valueOf(2L), newer);
        pending.put(Long.valueOf(3L), bob);
        FakeFrame retry = new FakeFrame(9L, "alice");
        PendingOrder.requeueFront(pending, retry);
        Long[] order = pending.keySet().toArray(new Long[0]);
        check(order.length == 4, "four frames");
        check(order[0].longValue() == 9L && order[1].longValue() == 1L && order[2].longValue() == 2L && order[3].longValue() == 3L, "alice retry is in front of alice and bob stays put");
        System.out.println("ok retried frame is at the front of that player");
    }

    private static void availabilityRoundTrip() {
        TerrainAvailability.clearAll();
        check(TerrainAvailability.levelId("minecraft:overworld|minecraft:overworld|5").equals("minecraft:overworld"), "level id");
        TerrainAvailability.note("minecraft:overworld", 4, -2);
        check(TerrainAvailability.contains("minecraft:overworld", 4, -2), "noted");
        check(!TerrainAvailability.contains("minecraft:the_nether", 4, -2), "other dimension");
        TerrainAvailability.clear("minecraft:overworld", 4, -2);
        check(!TerrainAvailability.contains("minecraft:overworld", 4, -2), "cleared");
        System.out.println("ok availability set");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static final class FakeFrame {
        public final long id;
        public final FakeKey key;

        FakeFrame(long id, String owner) {
            this.id = id;
            this.key = new FakeKey(owner);
        }
    }

    public static final class FakeKey {
        private final String owner;

        FakeKey(String owner) {
            this.owner = owner;
        }

        public String owner() {
            return this.owner;
        }
    }

    private RingHarness() {
    }
}
