package dev.wyseman.portallinker;

import net.minecraft.core.Direction;

/**
 * Interior size rules. The two numbers are the opening's edges, in either order.
 * A 3×5 matches a 5×3. Nothing is scaled: the player stays the same size.
 */
public final class OpeningSize {
    public static final int MIN_SHORT = 1;
    public static final int MIN_LONG = 2;

    private OpeningSize() {}

    public static boolean acceptable(int u, int v, int max) {
        if (u < 1 || v < 1 || u > max || v > max) {
            return false;
        }
        return Math.max(u, v) >= MIN_LONG;
    }

    public static boolean sameOpening(int u1, int v1, int u2, int v2) {
        return Math.min(u1, v1) == Math.min(u2, v2) && Math.max(u1, v1) == Math.max(u2, v2);
    }

    /** The two in-plane edges. The axis component of a flat box is the 1-block thickness. */
    public static int[] inPlane(Direction.Axis axis, int sizeX, int sizeY, int sizeZ) {
        return switch (axis) {
            case X -> new int[]{sizeZ, sizeY};
            case Y -> new int[]{sizeX, sizeZ};
            case Z -> new int[]{sizeX, sizeY};
        };
    }
}
