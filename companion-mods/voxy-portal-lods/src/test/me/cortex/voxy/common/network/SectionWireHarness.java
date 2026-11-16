package me.cortex.voxy.common.network;

import java.util.Arrays;

/** Compress, decompress, and require the original bytes back. */
public final class SectionWireHarness {
    public static void main(String[] args) {
        check(SectionWire.accepted(2) && SectionWire.accepted(3) && !SectionWire.accepted(1), "protocol gate");
        byte[] sample = new byte[48 * 1024];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = (byte) ((i * 17) ^ (i >>> 3));
        }
        SectionWire.Pack pack = SectionWire.pack(sample);
        check(pack.roundTrip, "round trip");
        check(pack.compressed < pack.raw, "compressible column got smaller, " + pack.compressed + " of " + pack.raw);
        check(Arrays.equals(sample, SectionWire.decode(pack.bytes)), "decode matches");
        check(SectionWire.decode(new byte[] {9, 0, 0, 0, 1, 0}) == null, "unknown codec is dropped");
        System.out.println("SectionWireHarness passed codec=" + SectionWire.codecName() + " ratio=" + (pack.compressed / (double) pack.raw));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
