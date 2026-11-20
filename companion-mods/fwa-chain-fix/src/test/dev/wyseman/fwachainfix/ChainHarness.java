package dev.wyseman.fwachainfix;

import dev.wyseman.fwachainfix.ChainColumn.Link;
import java.util.Arrays;

/**
 * Headless model of chain and lantern meshes. Long columns, lanterns at the
 * bottom and at several heights, mixed stacks, and lone lanterns are rendered
 * for thousands of frames and several portal passes.
 *
 * <p>The unfixed path is stock FWA: every chain link whose {@code isLast} flag
 * latched true draws the chain run above it, and every lantern returns
 * {@code isLast() == true} and draws itself plus the chains above it. Portal
 * passes that never flush append. The fixed path keeps one end per column and
 * flushes after it.
 */
public final class ChainHarness {
    private static final int FRAMES = 4000;
    private static final int PORTAL_PASSES = 4;
    private static final int COLUMNS = 32;
    private static final int LONG = 64;

    public static void main(String[] args) {
        checkRules();

        Link[] chains = fill(LONG, Link.CHAIN);
        Link[] lanternBottom = new Link[LONG + 1];
        lanternBottom[0] = Link.HANGING_LANTERN;
        Arrays.fill(lanternBottom, 1, lanternBottom.length, Link.CHAIN);
        Link[] multiHeight = new Link[LONG];
        for (int i = 0; i < LONG; i++) {
            multiHeight[i] = i % 8 == 0 ? Link.HANGING_LANTERN : Link.CHAIN;
        }
        Link[] mixed = {Link.CHAIN, Link.HANGING_LANTERN, Link.CHAIN, Link.CHAIN};
        Link[] stackedLanterns = {Link.HANGING_LANTERN, Link.SOUL_LANTERN, Link.CHAIN, Link.HANGING_LANTERN};
        Link[] splitByStanding = {Link.CHAIN, Link.CHAIN, Link.STANDING, Link.CHAIN, Link.SOUL_LANTERN};
        Link[] hangingAlone = {Link.HANGING_LANTERN};
        Link[] soulAlone = {Link.SOUL_LANTERN};
        Link[] standingAlone = {Link.STANDING};

        Scenario[] scenarios = {
            new Scenario("64 chains", chains, COLUMNS),
            new Scenario("lantern under 64 chains", lanternBottom, COLUMNS),
            new Scenario("lantern every 8 of 64", multiHeight, COLUMNS),
            new Scenario("chain-lantern-chain", mixed, COLUMNS),
            new Scenario("soul and hanging lanterns", stackedLanterns, COLUMNS),
            new Scenario("standing lantern splits the column", splitByStanding, COLUMNS),
            new Scenario("hanging lantern on a block", hangingAlone, COLUMNS),
            new Scenario("soul lantern on a block", soulAlone, COLUMNS),
            new Scenario("standing lantern", standingAlone, COLUMNS),
        };

        System.out.println("columns " + COLUMNS + " portal passes " + PORTAL_PASSES + " frames " + FRAMES);
        int longest = 0;
        for (Scenario scenario : scenarios) {
            long unfixedPass = unfixed(scenario, 1, false);
            long unfixedPasses = unfixed(scenario, PORTAL_PASSES, true);
            int fixedPeak = fixed(scenario, PORTAL_PASSES, FRAMES);
            longest = Math.max(longest, fixedPeak);
            System.out.println(scenario.name
                + " unfixedOnePass blockDatas " + unfixedPass
                + " quads " + ChainColumn.quadsFor(unfixedPass)
                + " | unfixed " + PORTAL_PASSES + " unflushed passes blockDatas " + unfixedPasses
                + " quads " + ChainColumn.quadsFor(unfixedPasses)
                + " | fixed peak blockDatas " + fixedPeak
                + " quads " + ChainColumn.quadsFor(fixedPeak));
            if (unfixedPass < fixedPeak) {
                throw new AssertionError(scenario.name + " unfixed " + unfixedPass + " < fixed " + fixedPeak);
            }
            if (scenario.copies > 1 && unfixedPasses != unfixedPass * PORTAL_PASSES) {
                throw new AssertionError(scenario.name + " passes did not scale");
            }
        }
        if (longest != LONG + 1) {
            throw new AssertionError("longest column " + longest);
        }
        System.out.println("fixed peak across every scenario stayed at one column and did not grow over "
            + FRAMES + " frames");
        System.out.println("ok");
    }

    private static void checkRules() {
        Link[] chain = fill(LONG, Link.CHAIN);
        if (ChainColumn.renderedLength(chain, 0) != LONG) {
            throw new AssertionError("chain length");
        }
        if (!ChainColumn.isRenderingEnd(Link.CHAIN, Link.NONE)
            || ChainColumn.isRenderingEnd(Link.CHAIN, Link.HANGING_LANTERN)
            || ChainColumn.isRenderingEnd(Link.SOUL_LANTERN, Link.CHAIN)
            || ChainColumn.isRenderingEnd(Link.NONE, Link.NONE)) {
            throw new AssertionError("end test");
        }
        if (ChainColumn.triangularBlockDatas(LONG) != 2080L) {
            throw new AssertionError(ChainColumn.triangularBlockDatas(LONG));
        }
        if (!ChainColumn.resting(0.01f, -0.02f, 0.0f) || ChainColumn.resting(8f, 0f, 0f)) {
            throw new AssertionError("rest");
        }
        if (ChainColumn.rebuildPoses(true, true, false) || !ChainColumn.rebuildPoses(true, true, true)) {
            throw new AssertionError("rebuild");
        }
        Link[] lanternBottom = new Link[LONG + 1];
        lanternBottom[0] = Link.SOUL_LANTERN;
        Arrays.fill(lanternBottom, 1, lanternBottom.length, Link.CHAIN);
        if (ChainColumn.renderedLength(lanternBottom, 0) != LONG + 1) {
            throw new AssertionError("soul column");
        }
        if (ChainColumn.unfixedDrawLength(lanternBottom, 0) != LONG + 1) {
            throw new AssertionError("soul unfixed");
        }
        int ends = 0;
        for (int i = 0; i < lanternBottom.length; i++) {
            Link below = i == 0 ? Link.NONE : lanternBottom[i - 1];
            if (ChainColumn.isRenderingEnd(lanternBottom[i], below)) {
                ends++;
            }
        }
        if (ends != 1) {
            throw new AssertionError("ends " + ends);
        }
    }

    /** Stock drawing. {@code accumulatePasses} keeps each portal pass in the same list. */
    private static long unfixed(Scenario scenario, int passes, boolean accumulatePasses) {
        ChainMesh mesh = new ChainMesh();
        mesh.beginPass();
        for (int pass = 0; pass < passes; pass++) {
            if (!accumulatePasses) {
                mesh.beginPass();
            }
            for (int copy = 0; copy < scenario.copies; copy++) {
                for (int i = 0; i < scenario.column.length; i++) {
                    int length = ChainColumn.unfixedDrawLength(scenario.column, i);
                    if (length > 0) {
                        mesh.addColumn(length);
                    }
                }
            }
        }
        long live = mesh.liveBlockDatas();
        mesh.flush();
        return live;
    }

    /** One end per column, flush after each end. Peak must stay flat for every frame. */
    private static int fixed(Scenario scenario, int passes, int frames) {
        ChainMesh mesh = new ChainMesh();
        int peak = 0;
        for (int frame = 0; frame < frames; frame++) {
            for (int pass = 0; pass < passes; pass++) {
                mesh.beginPass();
                for (int copy = 0; copy < scenario.copies; copy++) {
                    for (int i = 0; i < scenario.column.length; i++) {
                        Link below = i == 0 ? Link.NONE : scenario.column[i - 1];
                        if (!ChainColumn.isRenderingEnd(scenario.column[i], below)) {
                            continue;
                        }
                        int length = ChainColumn.renderedLength(scenario.column, i);
                        mesh.addColumn(length);
                        if (mesh.liveBlockDatas() != length) {
                            throw new AssertionError(scenario.name + " live " + mesh.liveBlockDatas());
                        }
                        mesh.flush();
                        peak = Math.max(peak, length);
                    }
                }
                if (mesh.liveBlockDatas() != 0) {
                    throw new AssertionError(scenario.name + " pass left " + mesh.liveBlockDatas());
                }
            }
            if (mesh.peakBlockDatas() != peak) {
                throw new AssertionError(scenario.name + " peak grew to " + mesh.peakBlockDatas()
                    + " at frame " + frame);
            }
        }
        return peak;
    }

    private static Link[] fill(int length, Link link) {
        Link[] column = new Link[length];
        Arrays.fill(column, link);
        return column;
    }

    private record Scenario(String name, Link[] column, int copies) {}
}
