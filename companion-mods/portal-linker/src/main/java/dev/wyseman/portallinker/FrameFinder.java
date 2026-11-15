package dev.wyseman.portallinker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;

/**
 * Finds one rectangular opening whose frame includes the clicked block.
 * The flood fill is Immersive Portals' {@link BlockPortalShape#findArea}, so a
 * hole, a missing edge, or a non-rectangle is not an opening.
 */
public final class FrameFinder {
    public enum Status {
        FOUND,
        NONE,
        TOO_SMALL,
        TOO_BIG,
        AMBIGUOUS
    }

    public record Hit(BlockPortalShape shape, int u, int v) {}

    public record Result(Status status, List<Hit> hits) {
        public Hit only() {
            return hits.get(0);
        }
    }

    private FrameFinder() {}

    /** Edge material: the frame tag or frames.frameBlock. */
    public static boolean isEdge(BlockState state) {
        return state.is(PortalLinker.FRAME_BLOCKS) || state.is(Config.frameBlock());
    }

    /** Corner material: the corner tag or frames.cornerBlock. */
    public static boolean isCorner(BlockState state) {
        return state.is(PortalLinker.CORNER_BLOCKS) || state.is(Config.cornerBlock());
    }

    public static boolean isFrame(BlockState state) {
        return isEdge(state) || isCorner(state);
    }

    /**
     * A saved frame cell still counts.
     * New links require a corner block on the four corners and an edge block everywhere else.
     * A link saved before that rule (legacy) also accepts crying obsidian in either place, until that block is broken.
     */
    public static boolean holds(BlockState state, boolean corner, boolean legacy) {
        if (legacy && state.is(Blocks.CRYING_OBSIDIAN)) {
            return true;
        }
        if (!Config.distinctCorners()) {
            return isFrame(state);
        }
        return corner ? isCorner(state) : isEdge(state);
    }

    /** The four extreme corners of a one-block-thick rectangular frame. */
    public static boolean isCornerPosition(Set<BlockPos> frame, BlockPos pos) {
        if (frame.isEmpty() || !frame.contains(pos)) {
            return false;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos other : frame) {
            minX = Math.min(minX, other.getX());
            minY = Math.min(minY, other.getY());
            minZ = Math.min(minZ, other.getZ());
            maxX = Math.max(maxX, other.getX());
            maxY = Math.max(maxY, other.getY());
            maxZ = Math.max(maxZ, other.getZ());
        }
        return (pos.getX() == minX || pos.getX() == maxX)
                && (pos.getY() == minY || pos.getY() == maxY)
                && (pos.getZ() == minZ || pos.getZ() == maxZ);
    }

    /** Uniform frame. Size tests and a config with cornerBlock equal to frameBlock use this. */
    public static Result find(BlockPos clicked, Predicate<BlockPos> isFrame, Predicate<BlockPos> isOpen, int maxInterior) {
        return findSplit(clicked, isFrame, isFrame, isOpen, maxInterior, false);
    }

    /** Edges and corners are different blocks. The flood fill accepts either; the rectangle is then checked. */
    public static Result find(BlockPos clicked, Predicate<BlockPos> isEdge, Predicate<BlockPos> isCorner, Predicate<BlockPos> isOpen, int maxInterior) {
        return findSplit(clicked, isEdge, isCorner, isOpen, maxInterior, true);
    }

    private static Result findSplit(BlockPos clicked, Predicate<BlockPos> isEdge, Predicate<BlockPos> isCorner, Predicate<BlockPos> isOpen, int maxInterior, boolean checkCorners) {
        Predicate<BlockPos> isFrame = pos -> isEdge.test(pos) || isCorner.test(pos);
        if (!isFrame.test(clicked)) {
            return new Result(Status.NONE, List.of());
        }
        List<Hit> within = collect(clicked, isFrame, isEdge, isCorner, isOpen, maxInterior, checkCorners);
        List<Hit> ok = new ArrayList<>();
        boolean tooSmall = false;
        for (Hit hit : within) {
            if (OpeningSize.acceptable(hit.u(), hit.v(), maxInterior)) {
                ok.add(hit);
            } else if (hit.u() <= maxInterior && hit.v() <= maxInterior) {
                tooSmall = true;
            }
        }
        if (ok.size() == 1) {
            return new Result(Status.FOUND, ok);
        }
        if (ok.size() > 1) {
            return new Result(Status.AMBIGUOUS, ok);
        }
        List<Hit> bigger = collect(clicked, isFrame, isEdge, isCorner, isOpen, maxInterior + 1, checkCorners);
        for (Hit hit : bigger) {
            if (hit.u() > maxInterior || hit.v() > maxInterior) {
                return new Result(Status.TOO_BIG, List.of());
            }
            if (!OpeningSize.acceptable(hit.u(), hit.v(), maxInterior)) {
                tooSmall = true;
            }
        }
        if (tooSmall) {
            return new Result(Status.TOO_SMALL, List.of());
        }
        return new Result(Status.NONE, List.of());
    }

    private static List<Hit> collect(BlockPos clicked, Predicate<BlockPos> isFrame, Predicate<BlockPos> isEdge, Predicate<BlockPos> isCorner, Predicate<BlockPos> isOpen, int lengthLimit, boolean checkCorners) {
        List<Hit> hits = new ArrayList<>();
        for (Direction into : Direction.values()) {
            BlockPos start = clicked.relative(into);
            if (!isOpen.test(start)) {
                continue;
            }
            for (Direction.Axis axis : Direction.Axis.values()) {
                if (axis == into.getAxis()) {
                    continue;
                }
                BlockPortalShape shape = BlockPortalShape.findArea(start, axis, isOpen, isFrame, lengthLimit);
                if (shape == null || !shape.isRectangle()) {
                    continue;
                }
                if (!shape.frameAreaWithCorner.contains(clicked)) {
                    continue;
                }
                if (!shape.frameAreaWithCorner.stream().allMatch(isFrame)) {
                    continue;
                }
                if (checkCorners && !cornersHold(shape, isEdge, isCorner)) {
                    continue;
                }
                if (already(hits, shape)) {
                    continue;
                }
                int[] edges = OpeningSize.inPlane(shape.axis, shape.innerAreaBox.getSize().getX(), shape.innerAreaBox.getSize().getY(), shape.innerAreaBox.getSize().getZ());
                hits.add(new Hit(shape, edges[0], edges[1]));
            }
        }
        return hits;
    }

    private static boolean cornersHold(BlockPortalShape shape, Predicate<BlockPos> isEdge, Predicate<BlockPos> isCorner) {
        for (BlockPos pos : shape.frameAreaWithCorner) {
            boolean corner = !shape.frameAreaWithoutCorner.contains(pos);
            if (corner) {
                if (!isCorner.test(pos)) {
                    return false;
                }
            } else if (!isEdge.test(pos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean already(List<Hit> hits, BlockPortalShape shape) {
        for (Hit hit : hits) {
            if (hit.shape().area.equals(shape.area) && hit.shape().axis == shape.axis) {
                return true;
            }
        }
        return false;
    }
}
