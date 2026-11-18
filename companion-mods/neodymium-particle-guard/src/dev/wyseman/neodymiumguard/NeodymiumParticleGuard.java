package dev.wyseman.neodymiumguard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Runtime half of {@link NeodymiumThrottle}. Reads the divisor once and turns a
 * level plus block position into a spawn decision.
 *
 * <p>The divisor can be changed without rebuilding the jar by setting the
 * {@code neodymiumguard.divisor} system property. {@code 1} restores the
 * unmodified Alex's Caves behaviour, {@code 8} makes the nodes pulse half as
 * often as the default. Anything unparseable or below 1 falls back to the
 * default, so a typo cannot switch the guard off by accident.
 */
public final class NeodymiumParticleGuard {
    public static final String DIVISOR_PROPERTY = "neodymiumguard.divisor";

    public static final NeodymiumThrottle THROTTLE = new NeodymiumThrottle(resolveDivisor());

    private NeodymiumParticleGuard() {}

    /** True when this node may spawn its orbit particle on this tick. */
    public static boolean shouldSpawn(Level level, BlockPos pos) {
        if (!THROTTLE.throttled()) {
            return true;
        }
        if (level == null || pos == null) {
            return true;
        }
        return THROTTLE.allows(pos.asLong(), level.getGameTime());
    }

    private static int resolveDivisor() {
        String raw = System.getProperty(DIVISOR_PROPERTY);
        if (raw == null) {
            return NeodymiumThrottle.DEFAULT_DIVISOR;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return NeodymiumThrottle.DEFAULT_DIVISOR;
        }
    }
}
