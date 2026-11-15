package abysscreatures.ai;

/**
 * Movement cases that made 0.2.2 mobs spin, run without Minecraft.
 * The old policy is the 0.2.2 formula: any 3D length over 2.5e-7 updates yaw from atan2(z, x)
 * and copies that yaw onto the head.
 */
public final class HeadingHarness {
    private static final double LENGTH_EPS = 2.500000277905201E-7d;
    private static final float TURN = 10.0f;

    public static void main(String[] args) {
        verticalNoise();
        stableHorizontal();
        colocatedFish();
        confinedColumn();
        lookControl();
        separationGain();
        System.out.println("HeadingHarness ok");
    }

    /** Mostly-vertical steer whose horizontal part is rotating noise under 1e-4. */
    private static void verticalNoise() {
        Course course = new Course();
        float oldYaw = 40.0f;
        float newYaw = 40.0f;
        double oldTravel = 0.0;
        double newTravel = 0.0;
        int writes = 0;
        for (int tick = 0; tick < 80; tick++) {
            double x = 1.0e-5 * Math.cos(tick * 1.7);
            double z = 1.0e-5 * Math.sin(tick * 2.3);
            double y = 0.02;
            float prevOld = oldYaw;
            oldYaw = oldPolicy(oldYaw, x, y, z);
            oldTravel += Math.abs(Course.wrap(oldYaw - prevOld));
            float prevNew = newYaw;
            if (course.steer(x, y, z)) {
                newYaw = Course.rotlerp(newYaw, course.yaw(), TURN);
                writes++;
            }
            newTravel += Math.abs(Course.wrap(newYaw - prevNew));
        }
        System.out.printf("vertical noise: old travel %.1f deg, new travel %.1f deg, writes %d%n", oldTravel, newTravel, writes);
        check(oldTravel > 400.0, "old vertical noise should spin, travel=" + oldTravel);
        check(newTravel == 0.0 && writes == 0, "vertical noise still moved yaw " + newTravel);
        check(Math.abs(Course.wrap(newYaw - 40.0f)) < 0.01f, "heading was not kept");
    }

    /** A real sideways target still turns, then holds. */
    private static void stableHorizontal() {
        Course course = new Course();
        float yaw = 10.0f;
        int writes = 0;
        float tail = 0.0f;
        for (int tick = 0; tick < 40; tick++) {
            float prev = yaw;
            if (course.steer(4.0, 0.2, 0.0)) {
                yaw = Course.rotlerp(yaw, course.yaw(), TURN);
                writes++;
            }
            if (tick >= 20) {
                tail += Math.abs(Course.wrap(yaw - prev));
            }
        }
        float want = (float) Math.toDegrees(Math.atan2(0.0, 4.0)) - 90.0f;
        System.out.printf("horizontal: yaw %.2f want %.2f writes %d tail %.2f%n", yaw, want, writes, tail);
        check(Math.abs(Course.wrap(yaw - want)) < 1.0f, "did not face +X, yaw=" + yaw);
        check(tail < 0.01f, "horizontal heading kept turning, tail=" + tail);
        check(writes > 0, "horizontal steer never wrote yaw");
    }

    /**
     * Two fish on top of each other. The gap is 0.002 blocks and its heading flips every tick.
     * 0.2.2 scales that gap by 0.1, so the push is noise. 0.2.3 floors the push length, and the
     * vertical cruise still keeps the yaw gate shut.
     */
    private static void colocatedFish() {
        double[] oldV = {0.0, 0.3, 0.0};
        double[] newV = {0.0, 0.3, 0.0};
        Course course = new Course();
        float oldYaw = 15.0f;
        float newYaw = 15.0f;
        double oldTravel = 0.0;
        double newTravel = 0.0;
        for (int tick = 0; tick < 60; tick++) {
            // A new arbitrary gap direction every tick, 0.002 blocks apart.
            double angle = tick * 2.399963;
            double dist = 0.002;
            double ox = Math.cos(angle) * dist;
            double oz = Math.sin(angle) * dist;
            float prevOld = oldYaw;
            oldV = stepVelocity(oldV, ox * 0.1, 0.0, oz * 0.1);
            oldYaw = oldPolicy(oldYaw, oldV[0] * 0.05, oldV[1] * 0.05, oldV[2] * 0.05);
            oldTravel += Math.abs(Course.wrap(oldYaw - prevOld));

            float prevNew = newYaw;
            newV = stepVelocity(newV, ox * Course.separationGain(dist), 0.0, oz * Course.separationGain(dist));
            if (course.steer(newV[0] * 0.05, newV[1] * 0.05, newV[2] * 0.05)) {
                newYaw = Course.rotlerp(newYaw, course.yaw(), TURN);
            }
            newTravel += Math.abs(Course.wrap(newYaw - prevNew));
        }
        System.out.printf("colocated: old travel %.1f new travel %.1f%n", oldTravel, newTravel);
        check(oldTravel > 200.0, "colocated old policy did not spin, travel=" + oldTravel);
        check(newTravel < 1.0, "colocated fish still spun, travel=" + newTravel);
        check(Math.abs(Course.separationGain(0.002) * 0.002 - 0.035) < 1.0e-9, "floor push length");
        check(Course.separationGain(1.0e-4) == 0.0, "epsilon neighbour must be skipped");
        check(Course.separationGain(2.0) == 0.0, "far neighbour must be skipped");
        check(Math.abs(Course.separationGain(1.0) - 0.1) < 1.0e-12, "normal separation gain");
    }

    private static double[] stepVelocity(double[] velocity, double ax, double ay, double az) {
        double vx = velocity[0] + ax;
        double vy = velocity[1] + ay;
        double vz = velocity[2] + az;
        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (len > 1.0e-8 && len > 0.3) {
            double scale = 0.3 / len;
            vx *= scale;
            vy *= scale;
            vz *= scale;
        }
        return new double[] {vx, vy, vz};
    }

    /**
     * One water column. The waypoint sits 3 blocks above the fish and the fish is kept inside
     * half a block of that column, which is what a tank does. 0.2.2 turns toward whatever side
     * of the column the fish just crept to.
     */
    private static void confinedColumn() {
        Fish oldFish = new Fish(20.0f);
        Fish newFish = new Fish(20.0f);
        Course course = new Course();
        for (int tick = 0; tick < 100; tick++) {
            creep(oldFish, true, null);
            creep(newFish, false, course);
        }
        System.out.printf(
                "column: old travel %.1f (yaw %.1f) new travel %.1f (yaw %.1f)%n",
                oldFish.travel, oldFish.yaw, newFish.travel, newFish.yaw);
        check(oldFish.travel > 180.0, "column old policy did not spin, travel=" + oldFish.travel);
        check(newFish.travel < 1.0, "column still spun, travel=" + newFish.travel);
        check(Math.abs(Course.wrap(newFish.yaw - 20.0f)) < 0.01f, "column heading changed");
    }

    private static void creep(Fish fish, boolean old, Course course) {
        double dx = 0.0 - fish.x;
        double dy = 8.0 - 5.0;
        double dz = 0.0 - fish.z;
        float prev = fish.yaw;
        if (old) {
            fish.yaw = oldPolicy(fish.yaw, dx, dy, dz);
        } else if (course.steer(dx, dy, dz)) {
            fish.yaw = Course.rotlerp(fish.yaw, course.yaw(), TURN);
            fish.writes++;
        }
        fish.travel += Math.abs(Course.wrap(fish.yaw - prev));
        double rad = Math.toRadians(fish.yaw);
        fish.x += -Math.sin(rad) * 0.05;
        fish.z += Math.cos(rad) * 0.05;
        fish.x += Math.sin(fish.travel) * 1.0e-5;
        // Free space in a one-block cell after a fish's own width: about 0.7 to the corner.
        if (fish.x > 0.7) fish.x = 0.7;
        if (fish.x < -0.7) fish.x = -0.7;
        if (fish.z > 0.7) fish.z = 0.7;
        if (fish.z < -0.7) fish.z = -0.7;
    }

    /**
     * LookAtTargetGoal only asks the look control to face the target. 0.2.2's move control then
     * overwrites yHeadRot with the body yaw on the same tick, so the head never gets further
     * than one look step (30 degrees) from the body. The patch does not write yHeadRot.
     */
    private static void lookControl() {
        float body = 0.0f;
        float oldHead = 0.0f;
        float newHead = 0.0f;
        float lookAt = 90.0f;
        for (int tick = 0; tick < 6; tick++) {
            oldHead = body;
            oldHead = Course.rotlerp(oldHead, lookAt, 30.0f);
            newHead = Course.rotlerp(newHead, lookAt, 30.0f);
        }
        System.out.printf("look: old head %.1f new head %.1f%n", oldHead, newHead);
        check(Math.abs(Course.wrap(oldHead - 30.0f)) < 0.01f, "old head should stick at one 30 degree step");
        check(Math.abs(Course.wrap(newHead - 90.0f)) < 0.01f, "new head should reach the target");
    }

    private static void separationGain() {
        check(Course.hold(1.0e-5, 0.02), "noise must hold");
        check(Course.hold(0.2, 3.0), "column offset must hold");
        check(!Course.hold(4.0, 0.2), "sideways steer must write");
        check(!Course.hold(3.0, 2.0), "a waypoint outside one block must write");
    }

    private static float oldPolicy(float yaw, double x, double y, double z) {
        if (x * x + y * y + z * z < LENGTH_EPS) {
            return yaw;
        }
        float target = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        return Course.rotlerp(yaw, target, TURN);
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }

    private static final class Fish {
        float yaw;
        double x;
        double z;
        double travel;
        int writes;

        Fish(float yaw) {
            this.yaw = yaw;
        }
    }
}
