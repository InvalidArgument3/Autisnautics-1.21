package abysscreatures.ai;

/**
 * Heading for one swimming mob.
 *
 * <p>Yaw is undefined when the steering vector's horizontal part is noise, and it is the wrong
 * thing to chase when a fish is packed into a water column and the only real offset is vertical.
 * Callers write {@code yRot} and {@code yBodyRot} only when {@link #steer} returns true, and they
 * leave {@code yHeadRot} to the look control.
 */
public final class Course {
    /** Below this horizontal length, {@code atan2} is just the noise angle. */
    public static final double HORIZONTAL_EPS = 1.0e-4;
    /**
     * One block. Inside this horizontal radius a steer whose vertical gap is longer than the
     * horizontal gap is a water column, not a turn. A corner of a single cell sits about 0.7
     * blocks from the node, so half a block still retargets a fish pressed against the glass.
     * Boid directions are a fraction of a block, so for them this radius is always true and the
     * vertical comparison is the whole test.
     */
    public static final double CONFINED = 1.0;
    /** Smoothing on the horizontal sample that actually feeds {@code atan2}. */
    public static final double EMA = 0.35;

    /** Separation is ignored at or below this distance: the offset has no direction. */
    public static final double SEPARATION_MIN = 1.0e-4;
    /** Neighbours closer than this are pushed as if they stood this far away. */
    public static final double SEPARATION_FLOOR = 0.35;
    public static final double SEPARATION_RANGE = 1.5;
    public static final double SEPARATION_GAIN = 0.1;

    private double smoothX;
    private double smoothZ;
    private boolean primed;
    private float heading;

    public boolean steer(double x, double y, double z) {
        double horizontal = Math.hypot(x, z);
        if (hold(horizontal, y)) {
            return false;
        }
        if (!this.primed) {
            this.smoothX = x;
            this.smoothZ = z;
            this.primed = true;
        } else {
            this.smoothX += (x - this.smoothX) * EMA;
            this.smoothZ += (z - this.smoothZ) * EMA;
        }
        double smoothed = Math.hypot(this.smoothX, this.smoothZ);
        if (hold(smoothed, y)) {
            return false;
        }
        this.heading = (float) Math.toDegrees(Math.atan2(this.smoothZ, this.smoothX)) - 90.0f;
        return true;
    }

    public float yaw() {
        return this.heading;
    }

    public static boolean hold(double horizontal, double vertical) {
        if (horizontal < HORIZONTAL_EPS) {
            return true;
        }
        return horizontal < CONFINED && horizontal < Math.abs(vertical);
    }

    /**
     * Scale applied to {@code (self - other)} in {@code Boid.update}.
     * Zero means skip. Inside the floor the scale rises so the push length stays
     * {@code SEPARATION_FLOOR * SEPARATION_GAIN} instead of tracking a tiny gap.
     */
    public static double separationGain(double distance) {
        if (!(distance > SEPARATION_MIN) || distance > SEPARATION_RANGE) {
            return 0.0;
        }
        if (distance < SEPARATION_FLOOR) {
            return SEPARATION_GAIN * (SEPARATION_FLOOR / distance);
        }
        return SEPARATION_GAIN;
    }

    public static float wrap(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    /** Same shape as {@code MoveControl.rotlerp}. */
    public static float rotlerp(float current, float target, float maximum) {
        float delta = wrap(target - current);
        if (delta > maximum) {
            delta = maximum;
        }
        if (delta < -maximum) {
            delta = -maximum;
        }
        float next = current + delta;
        if (next < 0.0f) {
            next += 360.0f;
        } else if (next > 360.0f) {
            next -= 360.0f;
        }
        return next;
    }
}
