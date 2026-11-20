package dev.wyseman.fwachainfix;

/**
 * Pure rules for a vertical chain column. The Minecraft mixin applies these;
 * the harness checks them without a client.
 *
 * <p>A column is stored bottom-up. Index 0 is the lowest block. A link renders
 * the chain only when it is the bottom of a run of vertical chain blocks.
 * Every other link still exists so the original model stays hidden, but it
 * must not allocate or draw a column.
 */
public final class ChainColumn {
    /** Swing components below this are treated as settled. Degrees, matching FWA. */
    public static final float REST_EPSILON = 0.05f;

    private ChainColumn() {}

    public static boolean isVerticalChainLink(boolean selfIsVerticalChain) {
        return selfIsVerticalChain;
    }

    /**
     * The rendering end is the lowest vertical-chain block. The block below it
     * is not a vertical chain (air, a support, or an unloaded gap that has
     * since been filled in by a later check).
     */
    public static boolean isRenderingEnd(boolean selfIsVerticalChain, boolean belowIsVerticalChain) {
        return selfIsVerticalChain && !belowIsVerticalChain;
    }

    /** Links this end would draw: itself plus every vertical chain above it. */
    public static int renderedLength(boolean[] bottomUp) {
        int length = 0;
        for (int i = 0; i < bottomUp.length; i++) {
            if (!bottomUp[i]) {
                break;
            }
            length++;
        }
        return length;
    }

    /**
     * What FWA allocates when every link whose {@code isLast} flag is true
     * renders every block above it. A chain whose links were all marked last
     * (the flag is sampled once, and a link whose block-below was not a chain
     * yet stays last) sums {@code n + (n-1) + ... + 1}.
     */
    public static long triangularBlockDatas(int length) {
        if (length <= 0) {
            return 0L;
        }
        return (long) length * (length + 1) / 2L;
    }

    public static long quadsFor(long blockDatas) {
        return blockDatas * 6L;
    }

    public static boolean resting(float tiltX, float tiltZ, float spin) {
        return Math.abs(tiltX) < REST_EPSILON
            && Math.abs(tiltZ) < REST_EPSILON
            && Math.abs(spin) < REST_EPSILON;
    }

    /**
     * Rebuild cached poses when the chain is not resting, when something
     * disturbed it (a block update or an entity in the column), or when there
     * is no cache yet. A resting chain with a cache keeps the poses it has.
     */
    public static boolean rebuildPoses(boolean haveCache, boolean resting, boolean disturbed) {
        return !haveCache || disturbed || !resting;
    }
}
