package me.cortex.voxy.common.config.compressors;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDecompressCtx;
import com.github.luben.zstd.ZstdException;
import me.cortex.voxy.common.config.ConfigBuildCtx;
import me.cortex.voxy.common.util.GlobalCleaner;
import me.cortex.voxy.common.util.MemoryBuffer;
import me.cortex.voxy.common.util.ResizingThreadLocalMemoryBuffer;
import me.cortex.voxy.common.util.UnsafeUtil;

public class ZSTDCompressor implements StorageCompressor {
    private static final ResizingThreadLocalMemoryBuffer SCRATCH = new ResizingThreadLocalMemoryBuffer(525320L);

    private static final class CompressCtxHolder {
        final ZstdCompressCtx ctx;
        int lastLevel = Integer.MIN_VALUE;

        CompressCtxHolder() {
            ZstdCompressCtx created = new ZstdCompressCtx();
            this.ctx = created;
            GlobalCleaner.CLEANER.register(this, created::close);
        }

        ZstdCompressCtx forLevel(int level) {
            if (this.lastLevel != level) {
                this.ctx.setLevel(level);
                this.lastLevel = level;
            }
            return this.ctx;
        }
    }

    private static final class DecompressCtxHolder {
        final ZstdDecompressCtx ctx;

        DecompressCtxHolder() {
            ZstdDecompressCtx created = new ZstdDecompressCtx();
            this.ctx = created;
            GlobalCleaner.CLEANER.register(this, created::close);
        }
    }

    private static final ThreadLocal<CompressCtxHolder> COMPRESS_CTX = ThreadLocal.withInitial(CompressCtxHolder::new);
    private static final ThreadLocal<DecompressCtxHolder> DECOMPRESS_CTX = ThreadLocal.withInitial(DecompressCtxHolder::new);

    private final int level;

    public ZSTDCompressor(int level) {
        this.level = level;
    }

    @Override
    public MemoryBuffer compress(MemoryBuffer saveData) {
        long bound = Zstd.compressBound(saveData.size);
        if (Zstd.isError(bound)) {
            throw new IllegalStateException("Zstd.compressBound failed: " + Zstd.getErrorName(bound));
        }
        if (saveData.size > Integer.MAX_VALUE || bound > Integer.MAX_VALUE) {
            throw new IllegalStateException("Section data is too large for ZSTD Java byte[] path");
        }

        byte[] src = new byte[(int) saveData.size];
        UnsafeUtil.memcpy(saveData.address, src);

        byte[] dst = new byte[(int) bound];
        long compressedSize;
        try {
            ZstdCompressCtx ctx = COMPRESS_CTX.get().forLevel(this.level);
            compressedSize = ctx.compressByteArray(dst, 0, dst.length, src, 0, src.length);
        } catch (ZstdException e) {
            throw new IllegalStateException("Failed to compress section data with ZSTD", e);
        }

        if (Zstd.isError(compressedSize)) {
            throw new IllegalStateException("Failed to compress section data with ZSTD: " + Zstd.getErrorName(compressedSize));
        }

        MemoryBuffer out = SCRATCH.get(bound).createUntrackedUnfreeableReference();
        UnsafeUtil.memcpy(dst, (int) compressedSize, out.address);
        return out.subSize(compressedSize);
    }

    @Override
    public MemoryBuffer decompress(MemoryBuffer saveData) {
        MemoryBuffer out = SCRATCH.get().createUntrackedUnfreeableReference();
        if (saveData.size > Integer.MAX_VALUE || out.size > Integer.MAX_VALUE) {
            throw new IllegalStateException("Section data is too large for ZSTD Java byte[] path");
        }

        byte[] src = new byte[(int) saveData.size];
        UnsafeUtil.memcpy(saveData.address, src);

        byte[] dst;
        long decompressedSize;
        try {
            long frameSize = Zstd.decompressedSize(src);
            if (frameSize > out.size) {
                throw new IllegalStateException("ZSTD frame exceeds section scratch capacity");
            }
            int dstLen = frameSize > 0L ? (int) frameSize : (int) out.size;
            dst = new byte[dstLen];
            ZstdDecompressCtx ctx = DECOMPRESS_CTX.get().ctx;
            decompressedSize = ctx.decompressByteArray(dst, 0, dstLen, src, 0, src.length);
        } catch (ZstdException e) {
            throw new IllegalStateException("Failed to decompress section data with ZSTD", e);
        }

        if (Zstd.isError(decompressedSize)) {
            throw new IllegalStateException("Failed to decompress section data with ZSTD: " + Zstd.getErrorName(decompressedSize));
        }

        UnsafeUtil.memcpy(dst, (int) decompressedSize, out.address);
        return out.subSize(decompressedSize);
    }

    @Override
    public void close() {
    }

    public static class Config extends CompressorConfig {
        public int compressionLevel;

        @Override
        public StorageCompressor build(ConfigBuildCtx ctx) {
            return new ZSTDCompressor(this.compressionLevel);
        }

        public static String getConfigTypeName() {
            return "ZSTD";
        }
    }
}
