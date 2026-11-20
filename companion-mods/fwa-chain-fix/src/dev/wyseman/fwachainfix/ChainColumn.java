package dev.wyseman.fwachainfix;

import java.util.function.IntPredicate;

/**
 * Pure rules for one vertical column of chains and hanging lanterns.
 * The Minecraft mixins apply these. The harness checks them without a client.
 *
 * <p>A column is stored bottom-up. Index 0 is the lowest block. Chains and
 * hanging lanterns (including soul lanterns) are the same column. A standing
 * lantern, air, or any other block ends the column. Exactly one block renders
 * it: the lowest link. Every other link still exists so the original model
 * stays hidden, but it must not allocate or draw.
 */
public final class ChainColumn {
    /** Swing components below this are treated as settled. Degrees, matching FWA. */
    public static final float REST_EPSILON = 0.05f;

    /** Stops a bad block-state lookup from walking forever. Taller than a world column. */
    public static final int MAX_LINKS = 8192;

    private ChainColumn() {}

    public enum Link {
        /** Air or a support. Not part of a swinging column. */
        NONE,
        CHAIN,
        HANGING_LANTERN,
        SOUL_LANTERN,
        /**
         * Sitting on a block. Stock does not start an animation. A leftover
         * one still has {@code isLast() == true} and would draw the chains above.
         */
        STANDING
    }

    public static boolean isColumnLink(Link link) {
        return link == Link.CHAIN || link == Link.HANGING_LANTERN || link == Link.SOUL_LANTERN;
    }

    /**
     * The rendering end is the lowest chain or hanging lantern. The block
     * below it is not part of the column.
     */
    public static boolean isRenderingEnd(Link self, Link below) {
        return isColumnLink(self) && !isColumnLink(below);
    }

    /**
     * Links this end draws, including itself. {@code step 0} is the end.
     * Walking stops at the first block that is not a chain or hanging lantern.
     */
    public static int countAscending(IntPredicate columnLinkAtStep) {
        int length = 0;
        for (int step = 0; step < MAX_LINKS; step++) {
            if (!columnLinkAtStep.test(step)) {
                break;
            }
            length++;
        }
        return length;
    }

    /** Length of the column that starts at {@code endIndex}, bottom-up. */
    public static int renderedLength(Link[] bottomUp, int endIndex) {
        return countAscending(step -> {
            int index = endIndex + step;
            return index < bottomUp.length && isColumnLink(bottomUp[index]);
        });
    }

    /**
     * What stock FWA draws for one link. A latched chain end walks only
     * chains, so it stops at a lantern. A lantern's {@code isLast} is always
     * true and its count is itself plus the chains directly above it. A
     * standing lantern is not started by stock; a leftover animation would
     * still claim to be last and would draw the chains above it.
     */
    public static int unfixedDrawLength(Link[] bottomUp, int index) {
        Link link = bottomUp[index];
        if (link == Link.NONE) {
            return 0;
        }
        // Standing is not a column link, but a leftover animation still draws.
        int length = 1;
        for (int above = index + 1; above < bottomUp.length; above++) {
            if (bottomUp[above] != Link.CHAIN) {
                break;
            }
            length++;
        }
        return length;
    }

    /**
     * What FWA allocates when every chain link is a rendering end and each
     * one draws every chain above it. Sums {@code n + (n-1) + ... + 1}.
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
     * Rebuild cached poses when the column is not resting, when something
     * disturbed it, or when there is no cache yet.
     */
    public static boolean rebuildPoses(boolean haveCache, boolean resting, boolean disturbed) {
        return !haveCache || disturbed || !resting;
    }
}
