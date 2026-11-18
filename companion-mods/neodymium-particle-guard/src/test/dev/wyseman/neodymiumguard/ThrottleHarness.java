package dev.wyseman.neodymiumguard;

/**
 * Drives {@link NeodymiumThrottle} without a running game. The throttle is pure,
 * so the claims the mixin depends on can be checked directly: a node spawns
 * exactly once per cycle, the phase is stable, and the phases stay spread
 * across neighbouring block positions instead of clustering.
 */
public final class ThrottleHarness {
    public static void main(String[] args) {
        unthrottledAlwaysAllows();
        oneSpawnPerCyclePerNode();
        deterministicAndStable();
        phasesSpreadAcrossNeighbours();
        fractionPerTick();
        negativeTimeIsHandled();
        divisorIsClamped();
        System.out.println("throttle ok");
    }

    /** Divisor 1 must reproduce the unmodified mod: every call spawns. */
    private static void unthrottledAlwaysAllows() {
        NeodymiumThrottle throttle = new NeodymiumThrottle(NeodymiumThrottle.UNTHROTTLED);
        expect(!throttle.throttled(), "divisor 1 is not throttled");
        for (long tick = 0; tick < 64; tick++) {
            expect(throttle.allows(12345L, tick), "unthrottled allows at tick " + tick);
        }
    }

    /** Over any full cycle a node spawns exactly once, and never twice. */
    private static void oneSpawnPerCyclePerNode() {
        int divisor = 4;
        NeodymiumThrottle throttle = new NeodymiumThrottle(divisor);
        long[] nodes = {0L, 1L, 7L, -1L, 4096L, 0x0123456789ABCDEFL, -987654321L};
        for (long node : nodes) {
            for (long start = 0; start < 40; start++) {
                int spawns = 0;
                for (long tick = start; tick < start + divisor; tick++) {
                    if (throttle.allows(node, tick)) {
                        spawns++;
                    }
                }
                expect(spawns == 1, "node " + node + " spawns once per cycle from tick " + start);
            }
        }
    }

    /** Same inputs give the same answer, and the phase does not drift with time. */
    private static void deterministicAndStable() {
        NeodymiumThrottle throttle = new NeodymiumThrottle(4);
        long node = 0x00DEADBEEFL;
        int phase = throttle.phase(node);
        for (long tick = 0; tick < 200; tick++) {
            boolean first = throttle.allows(node, tick);
            boolean second = throttle.allows(node, tick);
            expect(first == second, "repeat call agrees at tick " + tick);
            expect(throttle.phase(node) == phase, "phase stable at tick " + tick);
        }
        expect(phase >= 0 && phase < 4, "phase in range: " + phase);
    }

    /**
     * Neighbouring blocks differ by a few bits in the packed position. Without
     * mixing, whole rows would land on the same phase and pulse together.
     */
    private static void phasesSpreadAcrossNeighbours() {
        int divisor = 4;
        NeodymiumThrottle throttle = new NeodymiumThrottle(divisor);
        int[] seen = new int[divisor];
        int samples = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    long key = pack(x, y, z);
                    seen[throttle.phase(key)]++;
                    samples++;
                }
            }
        }
        for (int i = 0; i < divisor; i++) {
            expect(seen[i] > 0, "phase " + i + " is used by some node");
        }
        // A perfect split would be samples/divisor. Allow generous slack but
        // reject the clustering that raw packed positions would produce.
        int ideal = samples / divisor;
        for (int i = 0; i < divisor; i++) {
            expect(Math.abs(seen[i] - ideal) < ideal / 2,
                "phase " + i + " count " + seen[i] + " near ideal " + ideal);
        }
        System.out.println("  phases over " + samples + " nodes: "
            + seen[0] + "/" + seen[1] + "/" + seen[2] + "/" + seen[3]);
    }

    /** On one tick, roughly one in `divisor` nodes of a chunk spawn. */
    private static void fractionPerTick() {
        int divisor = 4;
        NeodymiumThrottle throttle = new NeodymiumThrottle(divisor);
        int nodes = 0;
        int allowed = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    nodes++;
                    if (throttle.allows(pack(x, y, z), 12345L)) {
                        allowed++;
                    }
                }
            }
        }
        double ratio = (double) allowed / nodes;
        expect(ratio > 0.15 && ratio < 0.35, "one tick spawns about a quarter: " + ratio);
        System.out.println("  tick 12345 spawned " + allowed + "/" + nodes + " nodes");
    }

    /** Game time can be negative on a fresh world; floorMod must not throw. */
    private static void negativeTimeIsHandled() {
        NeodymiumThrottle throttle = new NeodymiumThrottle(4);
        for (long tick = -64; tick < 0; tick++) {
            throttle.allows(999L, tick);
        }
        int spawns = 0;
        for (long tick = -4; tick < 0; tick++) {
            if (throttle.allows(999L, tick)) {
                spawns++;
            }
        }
        expect(spawns == 1, "negative ticks still give one spawn per cycle");
    }

    /** A bad divisor must fall back to unthrottled rather than misbehave. */
    private static void divisorIsClamped() {
        expect(new NeodymiumThrottle(0).divisor() == 1, "divisor 0 clamps to 1");
        expect(new NeodymiumThrottle(-5).divisor() == 1, "negative divisor clamps to 1");
        expect(!new NeodymiumThrottle(0).throttled(), "clamped divisor is unthrottled");
        expect(new NeodymiumThrottle(8).divisor() == 8, "divisor 8 kept");
        expect(new NeodymiumThrottle(8).throttled(), "divisor 8 is throttled");
    }

    /** Mirrors BlockPos.asLong so the harness uses realistic keys. */
    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    private static void expect(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
