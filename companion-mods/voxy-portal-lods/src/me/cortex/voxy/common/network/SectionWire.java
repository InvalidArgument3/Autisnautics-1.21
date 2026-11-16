package me.cortex.voxy.common.network;

import com.github.luben.zstd.Zstd;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Frames one terrain section for the wire. Hello protocol 2 stays raw bytes.
 * Protocol 3 is a framed payload: codec byte, uncompressed length, then zstd
 * or deflate. An old client never advertises protocol 3, so it never receives
 * a frame.
 */
public final class SectionWire {
    public static final int PROTOCOL_RAW = 2;
    public static final int PROTOCOL_FRAMED = 3;
    public static final byte ZSTD = 1;
    public static final byte DEFLATE = 2;
    /** Refuse a frame that claims more than 4 MiB uncompressed. */
    public static final int MAX_RAW = 4 * 1024 * 1024;

    private static final byte CODEC = pickCodec();

    private SectionWire() {
    }

    public static byte codec() {
        return CODEC;
    }

    public static String codecName() {
        return CODEC == ZSTD ? "zstd" : "deflate";
    }

    /** Hello protocols this server will stream to. */
    public static boolean accepted(int protocol) {
        return protocol == PROTOCOL_RAW || protocol == PROTOCOL_FRAMED;
    }

    public static boolean compressFor(Object player) {
        return protocolOf(player) >= PROTOCOL_FRAMED;
    }

    public static byte[] frame(byte[] raw) {
        return encode(raw);
    }

    public static Pack pack(byte[] raw) {
        long started = System.nanoTime();
        byte[] framed = encode(raw);
        long compressed = System.nanoTime();
        byte[] back = decode(framed);
        long done = System.nanoTime();
        return new Pack(framed, raw.length, framed.length, compressed - started, done - compressed, Arrays.equals(raw, back));
    }

    public static byte[] decode(byte[] framed) {
        if (framed == null || framed.length < 6) {
            return null;
        }
        int rawLength = ((framed[1] & 0xFF) << 24) | ((framed[2] & 0xFF) << 16) | ((framed[3] & 0xFF) << 8) | (framed[4] & 0xFF);
        if (rawLength <= 0 || rawLength > MAX_RAW) {
            return null;
        }
        byte[] raw = new byte[rawLength];
        try {
            if (framed[0] == ZSTD) {
                long size = Zstd.decompressByteArray(raw, 0, rawLength, framed, 5, framed.length - 5);
                if (Zstd.isError(size) || size != rawLength) {
                    return null;
                }
                return raw;
            }
            if (framed[0] == DEFLATE) {
                Inflater inflater = new Inflater();
                try {
                    inflater.setInput(framed, 5, framed.length - 5);
                    int size = inflater.inflate(raw);
                    if (size != rawLength || !inflater.finished()) {
                        return null;
                    }
                    return raw;
                } finally {
                    inflater.end();
                }
            }
        } catch (DataFormatException failure) {
            return null;
        }
        return null;
    }

    /** Network-thread entry. Framed sections become the raw bytes the apply queue already understands. */
    public static SectionSyncBatchPayload expand(SectionSyncBatchPayload payload) {
        if (payload == null) {
            return null;
        }
        List<byte[]> sections = payload.sectionDatas();
        ArrayList<byte[]> raw = new ArrayList<byte[]>(sections.size());
        for (int i = 0; i < sections.size(); i++) {
            byte[] section = sections.get(i);
            if (section == null || section.length == 0) {
                continue;
            }
            // A raw voxel section starts with protocol varint 2, the same byte as
            // the deflate flag. Only replace the bytes when the frame actually
            // decodes. Dropping a failed guess makes an empty batch, and
            // TerrainApplyQueue.offer then throws "Terrain count outside bounds".
            if (section[0] == ZSTD || section[0] == DEFLATE) {
                byte[] decoded = decode(section);
                raw.add(decoded != null ? decoded : section);
            } else {
                raw.add(section);
            }
        }
        return new SectionSyncBatchPayload(payload.transferId(), payload.levelDimensionId(), payload.dimensionTypeId(), payload.worldSeed(), raw);
    }

    private static byte[] encode(byte[] raw) {
        byte[] compressed = CODEC == ZSTD ? zstd(raw) : null;
        byte codec = ZSTD;
        if (compressed == null) {
            compressed = deflate(raw);
            codec = DEFLATE;
        }
        byte[] out = new byte[5 + compressed.length];
        out[0] = codec;
        out[1] = (byte) (raw.length >>> 24);
        out[2] = (byte) (raw.length >>> 16);
        out[3] = (byte) (raw.length >>> 8);
        out[4] = (byte) raw.length;
        System.arraycopy(compressed, 0, out, 5, compressed.length);
        return out;
    }

    private static byte[] zstd(byte[] raw) {
        try {
            byte[] out = new byte[(int) Zstd.compressBound(raw.length)];
            long size = Zstd.compressByteArray(out, 0, out.length, raw, 0, raw.length, 3);
            if (Zstd.isError(size)) {
                return null;
            }
            return Arrays.copyOf(out, (int) size);
        } catch (Throwable failure) {
            return null;
        }
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater(6);
        try {
            deflater.setInput(raw);
            deflater.finish();
            byte[] out = new byte[Math.max(64, raw.length / 2)];
            int total = 0;
            while (!deflater.finished()) {
                if (total == out.length) {
                    out = Arrays.copyOf(out, out.length * 2);
                }
                total += deflater.deflate(out, total, out.length - total);
            }
            return Arrays.copyOf(out, total);
        } finally {
            deflater.end();
        }
    }

    private static byte pickCodec() {
        byte[] sample = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        return zstd(sample) != null ? ZSTD : DEFLATE;
    }

    private static int protocolOf(Object player) {
        try {
            Field sessions = Class.forName("me.cortex.voxy.common.network.VoxySessionSupport").getDeclaredField("PLAYER_SESSION");
            sessions.setAccessible(true);
            Object map = sessions.get(null);
            Object id = player.getClass().getMethod("getUUID").invoke(player);
            Object session = ((Map<?, ?>) map).get(id);
            if (session == null) {
                return 0;
            }
            return ((Integer) session.getClass().getMethod("protocolVersion").invoke(session)).intValue();
        } catch (Throwable failure) {
            return 0;
        }
    }

    public static final class Pack {
        public final byte[] bytes;
        public final int raw;
        public final int compressed;
        public final long compressNs;
        public final long decompressNs;
        public final boolean roundTrip;

        Pack(byte[] bytes, int raw, int compressed, long compressNs, long decompressNs, boolean roundTrip) {
            this.bytes = bytes;
            this.raw = raw;
            this.compressed = compressed;
            this.compressNs = compressNs;
            this.decompressNs = decompressNs;
            this.roundTrip = roundTrip;
        }
    }
}
