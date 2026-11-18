package dev.wyseman.neodymiumguard;

/**
 * Decides which neodymium nodes may spawn a particle on a given tick.
 *
 * <p>Alex's Caves spawns a magnetic orbit particle from
 * {@code NeodymiumNodeBlock.animateTick} on every call. The guard in that
 * method reads {@code random.nextInt(1)}, which is always {@code 0}, so the
 * spawn is unconditional. Each particle is a
 * {@code MagneticOrbitParticle} that lives 50-80 ticks and draws 20 trail
 * samples under {@code ParticleRenderType.CUSTOM}, so a magnetic caves chunk
 * keeps thousands of unbatched quads alive.
 *
 * <p>This throttle keeps the visual but spreads it out. Every node gets a
 * fixed phase derived from its position, and spawns only on ticks where
 * {@code (gameTime + phase) % divisor == 0}. Over any {@code divisor} ticks
 * each node spawns exactly once, and on any single tick roughly
 * {@code 1/divisor} of the nodes in view spawn. The particle count in flight
 * drops by the divisor, and the phase is stable per block, so a given node
 * always pulses on the same beat instead of flickering randomly.
 *
 * <p>No state is kept: the same position and tick always give the same answer,
 * which is what makes this testable without a running game.
 */
public final class NeodymiumThrottle {
    /** Spawn one particle per four ticks. */
    public static final int DEFAULT_DIVISOR = 4;

    /** Divisor 1 reproduces the unmodified mod behaviour. */
    public static final int UNTHROTTLED = 1;

    private final int divisor;

    public NeodymiumThrottle(int divisor) {
        this.divisor = Math.max(UNTHROTTLED, divisor);
    }

    public int divisor() {
        return this.divisor;
    }

    public boolean throttled() {
        return this.divisor > UNTHROTTLED;
    }

    /**
     * True when the node at {@code nodeKey} is scheduled to spawn on
     * {@code gameTime}. {@code nodeKey} is a packed block position.
     */
    public boolean allows(long nodeKey, long gameTime) {
        if (this.divisor <= UNTHROTTLED) {
            return true;
        }
        return Math.floorMod(gameTime + phase(nodeKey), this.divisor) == 0;
    }

    /** The node's fixed slot in the cycle, in {@code [0, divisor)}. */
    public int phase(long nodeKey) {
        return (int) Math.floorMod(mix(nodeKey), this.divisor);
    }

    /**
     * Murmur3's 64-bit finalizer. Packed block positions differ only in a few
     * bits between neighbours, so the raw value would cluster phases along an
     * axis; mixing spreads them across the whole cycle.
     */
    static long mix(long value) {
        long x = value;
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;
        return x;
    }
}
