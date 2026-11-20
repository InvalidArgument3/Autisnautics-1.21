package dev.wyseman.fwachainfix;

/**
 * Headless model of the chain mesh. A long chain and a set of chains are
 * rendered for thousands of frames. The unfixed path is what FWA does when
 * every link is a rendering end and the quad list lives until the level pass
 * finishes. The fixed path keeps one end and flushes after it.
 */
public final class ChainHarness {
    private static final int FRAMES = 4000;
    private static final int PORTAL_PASSES = 4;
    private static final int LONG_CHAIN = 64;
    private static final int CHAIN_COUNT = 32;

    public static void main(String[] args) {
        boolean[] chain = new boolean[LONG_CHAIN];
        java.util.Arrays.fill(chain, true);
        int length = ChainColumn.renderedLength(chain);
        if (length != LONG_CHAIN) {
            throw new AssertionError("length " + length);
        }
        if (!ChainColumn.isRenderingEnd(true, false) || ChainColumn.isRenderingEnd(true, true)) {
            throw new AssertionError("end test");
        }
        if (ChainColumn.triangularBlockDatas(LONG_CHAIN) != 2080L) {
            throw new AssertionError(ChainColumn.triangularBlockDatas(LONG_CHAIN));
        }
        if (!ChainColumn.resting(0.01f, -0.02f, 0.0f) || ChainColumn.resting(8f, 0f, 0f)) {
            throw new AssertionError("rest");
        }
        if (ChainColumn.rebuildPoses(true, true, false) || !ChainColumn.rebuildPoses(true, true, true)) {
            throw new AssertionError("rebuild");
        }
        if (!ChainColumn.rebuildPoses(false, true, false)) {
            throw new AssertionError("first build");
        }

        long unfixedOneFrame = unfixedFrame(CHAIN_COUNT, LONG_CHAIN, PORTAL_PASSES);
        long fixedPeak = fixedFrames(CHAIN_COUNT, LONG_CHAIN, PORTAL_PASSES, FRAMES);

        System.out.println("chain length " + LONG_CHAIN + " chains " + CHAIN_COUNT
            + " portal passes " + PORTAL_PASSES + " frames " + FRAMES);
        System.out.println("unfixed one frame blockDatas " + unfixedOneFrame
            + " quads " + ChainColumn.quadsFor(unfixedOneFrame)
            + " (triangular " + ChainColumn.triangularBlockDatas(LONG_CHAIN) + " per chain, times chains, times passes)");
        System.out.println("unfixed if that frame never flushes, after " + FRAMES + " frames blockDatas "
            + (unfixedOneFrame * FRAMES));
        System.out.println("fixed peak live blockDatas " + fixedPeak
            + " quads " + ChainColumn.quadsFor(fixedPeak)
            + " (one chain, flushed, flat across frames)");

        if (fixedPeak != LONG_CHAIN) {
            throw new AssertionError("fixed peak " + fixedPeak);
        }
        if (unfixedOneFrame <= fixedPeak) {
            throw new AssertionError("unfixed did not exceed fixed");
        }
        // 32 chains of 64, every link draws the remainder, 4 portal passes.
        long expect = ChainColumn.triangularBlockDatas(LONG_CHAIN) * CHAIN_COUNT * PORTAL_PASSES;
        if (unfixedOneFrame != expect) {
            throw new AssertionError(unfixedOneFrame + " != " + expect);
        }
        System.out.println("ok");
    }

    /** Every link is an end. Portal passes append because nothing flushed. */
    private static long unfixedFrame(int chains, int length, int passes) {
        ChainMesh mesh = new ChainMesh();
        mesh.beginPass();
        for (int pass = 0; pass < passes; pass++) {
            for (int chain = 0; chain < chains; chain++) {
                for (int link = 0; link < length; link++) {
                    int above = length - link;
                    mesh.addColumn(above);
                }
            }
        }
        long live = mesh.liveBlockDatas();
        mesh.flush();
        return live;
    }

    /** One end per chain, flush after each end, repeated for many frames. */
    private static long fixedFrames(int chains, int length, int passes, int frames) {
        ChainMesh mesh = new ChainMesh();
        int last = -1;
        for (int frame = 0; frame < frames; frame++) {
            for (int pass = 0; pass < passes; pass++) {
                mesh.beginPass();
                for (int chain = 0; chain < chains; chain++) {
                    mesh.addColumn(length);
                    if (mesh.liveBlockDatas() != length) {
                        throw new AssertionError("live " + mesh.liveBlockDatas());
                    }
                    mesh.flush();
                    if (mesh.liveBlockDatas() != 0) {
                        throw new AssertionError("flush left " + mesh.liveBlockDatas());
                    }
                }
            }
            if (mesh.peakBlockDatas() != length) {
                throw new AssertionError("peak grew to " + mesh.peakBlockDatas() + " at frame " + frame);
            }
            last = mesh.peakBlockDatas();
        }
        return last;
    }
}
