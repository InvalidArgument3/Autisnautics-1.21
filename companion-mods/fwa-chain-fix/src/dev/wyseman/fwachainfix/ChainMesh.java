package dev.wyseman.fwachainfix;

import java.util.ArrayList;
import java.util.List;

/**
 * The frame mesh FWA keeps in {@code AnimationRenderer.buffers}. Quads are
 * appended and the list is cleared only when the whole level pass finishes.
 * One chain end that draws {@code n} links adds {@code n} block-datas and
 * {@code 6n} quads. Several ends, or several portal passes that do not flush,
 * add those on top of each other.
 *
 * <p>The fixed path flushes after each rendering end, so the live count is the
 * longest chain currently being drawn, then zero. Across frames the peak does
 * not grow.
 */
public final class ChainMesh {
    private final List<BlockData> live = new ArrayList<>();
    private long blockDatasAdded;
    private long quadsAdded;
    private int peakBlockDatas;
    private int peakQuads;

    public void beginPass() {
        // Stock prepareFrame. A pass that never reaches renderBuffers keeps this.
        live.clear();
    }

    /** One rendered chain link: one block-data, six quads, one pose. */
    public void addLink() {
        live.add(new BlockData());
        blockDatasAdded++;
        quadsAdded += 6L;
        peakBlockDatas = Math.max(peakBlockDatas, live.size());
        peakQuads = Math.max(peakQuads, live.size() * 6);
    }

    public void addColumn(int links) {
        for (int i = 0; i < links; i++) {
            addLink();
        }
    }

    /** Stock renderBuffers, or the patch's per-chain flush. */
    public void flush() {
        live.clear();
    }

    public int liveBlockDatas() {
        return live.size();
    }

    public int liveQuads() {
        return live.size() * 6;
    }

    public int peakBlockDatas() {
        return peakBlockDatas;
    }

    public int peakQuads() {
        return peakQuads;
    }

    public long blockDatasAdded() {
        return blockDatasAdded;
    }

    public long quadsAdded() {
        return quadsAdded;
    }

    /** One pose per block-data. The class is only here so the count is real objects. */
    public static final class BlockData {}
}
