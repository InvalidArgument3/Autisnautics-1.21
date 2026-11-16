package me.cortex.voxy.server;

import java.util.function.BooleanSupplier;

/** One nearest-first pass over a square ring. Pure: the probe does the I/O. */
public final class RingScan {
    public static final int SENT = 1;
    public static final int WAIT = 2;
    public static final int FAIL = 3;
    public static final int BACKPRESSURE = 4;
    public static final int MAX_STEPS = 256;

    public interface Probe {
        boolean known(int x, int z);

        boolean noted(int x, int z);

        boolean onDisk(int x, int z);

        int attempt(int x, int z);
    }

    public interface Abandoned {
        void cell(int x, int z);
    }

    private RingScan() {
    }

    /**
     * @return packed chunk of a successful send, or {@code null} when this pass sent nothing
     */
    public static int[] scan(RingCursor cursor, int playerX, int playerZ, int innerExclusive, int maxInclusive, Probe probe, int failCap, BooleanSupplier expired, Abandoned onAbandoned) {
        int min = innerExclusive + 1;
        for (int n = 0; n < MAX_STEPS; n++) {
            if (expired != null && expired.getAsBoolean()) {
                return null;
            }
            int[] cell = cursor.peek(min, maxInclusive);
            int x = cell[0];
            int z = cell[1];
            int distance = Math.max(Math.abs(x - playerX), Math.abs(z - playerZ));
            if (distance <= innerExclusive || distance > maxInclusive) {
                cursor.skip(min, maxInclusive);
                continue;
            }
            if (probe.known(x, z)) {
                cursor.skip(min, maxInclusive);
                continue;
            }
            boolean noted = probe.noted(x, z);
            boolean onDisk = probe.onDisk(x, z);
            if (!noted && !onDisk) {
                cursor.skip(min, maxInclusive);
                continue;
            }
            int result = probe.attempt(x, z);
            if (result == SENT) {
                cursor.commit(min, maxInclusive);
                return cell;
            }
            if (result == WAIT || result == BACKPRESSURE) {
                return null;
            }
            if (cursor.fail(min, maxInclusive, failCap)) {
                if (onAbandoned != null) {
                    onAbandoned.cell(x, z);
                }
                continue;
            }
            return null;
        }
        return null;
    }
}
