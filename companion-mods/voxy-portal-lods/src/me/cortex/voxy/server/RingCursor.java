package me.cortex.voxy.server;

/**
 * Square-ring cursor. {@link #peek} repeats the same cell until {@link #commit}
 * or {@link #skip}. A failed send must not move the ring.
 */
public final class RingCursor {
    private int centerX;
    private int centerZ;
    private int radius;
    private int edgeIndex;
    private boolean holding;
    private int holdX;
    private int holdZ;
    private int attempts;

    public RingCursor(int centerX, int centerZ, int minRadius) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = Math.max(1, minRadius);
    }

    public void retargetIfMoved(int newCenterX, int newCenterZ, int minRadius) {
        if (this.centerX == newCenterX && this.centerZ == newCenterZ) {
            return;
        }
        this.centerX = newCenterX;
        this.centerZ = newCenterZ;
        this.radius = Math.max(1, minRadius);
        this.edgeIndex = 0;
        this.holding = false;
        this.attempts = 0;
    }

    public int[] peek(int minRadius, int maxRadius) {
        if (this.holding) {
            return new int[] {this.holdX, this.holdZ};
        }
        int[] point = this.step(minRadius, maxRadius, false);
        this.holdX = point[0];
        this.holdZ = point[1];
        this.holding = true;
        this.attempts = 0;
        return point;
    }

    public void commit(int minRadius, int maxRadius) {
        if (this.holding) {
            this.step(minRadius, maxRadius, true);
        }
        this.holding = false;
        this.attempts = 0;
    }

    public void skip(int minRadius, int maxRadius) {
        this.step(minRadius, maxRadius, true);
        this.holding = false;
        this.attempts = 0;
    }

    /** @return true when the cell is abandoned after {@code cap} failures */
    public boolean fail(int minRadius, int maxRadius, int cap) {
        this.attempts++;
        if (this.attempts < cap) {
            return false;
        }
        this.step(minRadius, maxRadius, true);
        this.holding = false;
        this.attempts = 0;
        return true;
    }

    public int attempts() {
        return this.attempts;
    }

    /**
     * Same square as {@code VoxyServerGeneratedChunkWalker.WalkCursor#next}.
     * {@code mutate} false previews the cell {@code mutate} true would return and then leave behind.
     */
    private int[] step(int minRadius, int maxRadius, boolean mutate) {
        int max = maxRadius < minRadius ? minRadius : maxRadius;
        int radius = this.radius;
        int edge = this.edgeIndex;
        if (radius < minRadius) {
            radius = minRadius;
            edge = 0;
        }
        if (radius > max) {
            radius = minRadius;
            edge = 0;
        }
        int perimeter = radius == 0 ? 1 : radius * 8;
        int i = edge >= perimeter ? 0 : edge;
        int r = radius;
        int side = r * 2;
        int x;
        int z;
        if (i < side) {
            x = this.centerX - r + i;
            z = this.centerZ - r;
        } else if (i < side * 2) {
            x = this.centerX + r;
            z = this.centerZ - r + (i - side);
        } else if (i < side * 3) {
            x = this.centerX + r - (i - side * 2);
            z = this.centerZ + r;
        } else {
            x = this.centerX - r;
            z = this.centerZ + r - (i - side * 3);
        }
        if (mutate) {
            edge++;
            if (edge >= perimeter) {
                edge = 0;
                radius++;
                if (radius > max) {
                    radius = minRadius;
                }
            }
            this.radius = radius;
            this.edgeIndex = edge;
        }
        return new int[] {x, z};
    }
}
