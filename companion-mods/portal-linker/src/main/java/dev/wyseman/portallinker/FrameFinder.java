package dev.wyseman.portallinker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public static boolean isFrame(BlockState state) {
        return state.is(PortalLinker.FRAME_BLOCKS) || state.is(Config.frameBlock());
    }

    public static Result find(BlockPos clicked, Predicate<BlockPos> isFrame, Predicate<BlockPos> isOpen, int maxInterior) {
        if (!isFrame.test(clicked)) {
            return new Result(Status.NONE, List.of());
        }
        List<Hit> within = collect(clicked, isFrame, isOpen, maxInterior);
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
        List<Hit> bigger = collect(clicked, isFrame, isOpen, maxInterior + 1);
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

    private static List<Hit> collect(BlockPos clicked, Predicate<BlockPos> isFrame, Predicate<BlockPos> isOpen, int lengthLimit) {
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
                if (already(hits, shape)) {
                    continue;
                }
                int[] edges = OpeningSize.inPlane(shape.axis, shape.innerAreaBox.getSize().getX(), shape.innerAreaBox.getSize().getY(), shape.innerAreaBox.getSize().getZ());
                hits.add(new Hit(shape, edges[0], edges[1]));
            }
        }
        return hits;
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
