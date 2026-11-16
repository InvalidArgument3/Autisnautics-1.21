package me.cortex.voxy.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.world.WorldEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;

/**
 * Times the same stages {@code sendChunkColumn} runs, using its private read and convert.
 * The file read is split from the NBT parse so the two costs are not one number.
 */
public final class ColumnStages {
    private static Method regionPath;
    private static Method regionHeader;
    private static Method importable;
    private static Method encode;
    private static Method offer;
    private static Method convert;
    private static Constructor<?> converterCtor;
    private static final ThreadLocal<Object> CONVERTER = new ThreadLocal<Object>();
    private static final ThreadLocal<Object> CONVERTER_LEVEL = new ThreadLocal<Object>();
    private static int failures;

    private ColumnStages() {
    }

    /** @return a {@link RingScan} result */
    public static int time(Object levelObject, Object engineObject, Object playerObject, int x, int z) {
        long totalStart = System.nanoTime();
        try {
            ensure();
            ServerLevel level = (ServerLevel) levelObject;
            WorldEngine engine = (WorldEngine) engineObject;
            ServerPlayer player = (ServerPlayer) playerObject;
            long[] read = new long[1];
            long[] parse = new long[1];
            CompoundTag tag = readSplit(level, x, z, read, parse);
            ColumnProfile.add("regionRead", read[0]);
            ColumnProfile.add("nbtParse", parse[0]);
            long importStart = System.nanoTime();
            boolean ok = tag != null && (Boolean) importable.invoke(null, tag);
            ColumnProfile.add("import", System.nanoTime() - importStart);
            if (!ok) {
                ColumnProfile.add("total", System.nanoTime() - totalStart);
                return RingScan.SKIP;
            }
            Object converter = converter(level);
            long convertStart = System.nanoTime();
            ListTag sections = tag.getList("sections", Tag.TAG_COMPOUND);
            ArrayList<VoxelizedSection> voxels = new ArrayList<VoxelizedSection>();
            for (int i = 0; i < sections.size(); i++) {
                Tag element = sections.get(i);
                if (!(element instanceof CompoundTag section)) {
                    continue;
                }
                Object voxel = convert.invoke(converter, engine, Integer.valueOf(x), Integer.valueOf(section.getInt("Y")), Integer.valueOf(z), section);
                if (voxel instanceof VoxelizedSection) {
                    voxels.add((VoxelizedSection) voxel);
                }
            }
            ColumnProfile.add("convert", System.nanoTime() - convertStart);
            if (voxels.isEmpty()) {
                ColumnProfile.add("total", System.nanoTime() - totalStart);
                return RingScan.SKIP;
            }
            long encodeStart = System.nanoTime();
            ArrayList<byte[]> wires = new ArrayList<byte[]>(voxels.size());
            int bytes = 0;
            for (int i = 0; i < voxels.size(); i++) {
                byte[] wire = (byte[]) encode.invoke(null, level, engine, voxels.get(i));
                if (wire != null && wire.length > 0) {
                    wires.add(wire);
                    bytes += wire.length;
                }
            }
            ColumnProfile.add("encode", System.nanoTime() - encodeStart);
            ColumnProfile.sizes(voxels.size(), wires.size(), bytes);
            if (wires.isEmpty()) {
                ColumnProfile.add("total", System.nanoTime() - totalStart);
                return RingScan.SKIP;
            }
            long offerStart = System.nanoTime();
            boolean sent = (Boolean) offer.invoke(null, player, level, new ChunkPos(x, z), wires);
            ColumnProfile.add("offer", System.nanoTime() - offerStart);
            ColumnProfile.add("total", System.nanoTime() - totalStart);
            if (!sent) {
                return RingScan.FAIL;
            }
            ColumnProfile.sent();
            return RingScan.SENT;
        } catch (Throwable failure) {
            if (failures++ < 3) {
                me.cortex.voxy.common.Logger.warn("[terrain-sync][profile] column stage failed", failure);
            }
            ColumnProfile.add("total", System.nanoTime() - totalStart);
            return RingScan.FAIL;
        }
    }

    /**
     * Convert and encode one column. Safe to call off the server thread: the section
     * buffer is thread-local, and the shared id mapper is held only for this call.
     */
    public static List<byte[]> wires(Object levelObject, Object engineObject, int x, int z, CompoundTag tag) throws ReflectiveOperationException {
        ensure();
        ServerLevel level = (ServerLevel) levelObject;
        WorldEngine engine = (WorldEngine) engineObject;
        long convertStart = System.nanoTime();
        ListTag sections = tag.getList("sections", Tag.TAG_COMPOUND);
        ArrayList<VoxelizedSection> voxels = new ArrayList<VoxelizedSection>();
        ArrayList<byte[]> encoded = new ArrayList<byte[]>();
        synchronized (engine) {
            Object converter = converter(level);
            for (int i = 0; i < sections.size(); i++) {
                Tag element = sections.get(i);
                if (!(element instanceof CompoundTag section)) {
                    continue;
                }
                Object voxel = convert.invoke(converter, engine, Integer.valueOf(x), Integer.valueOf(section.getInt("Y")), Integer.valueOf(z), section);
                if (voxel instanceof VoxelizedSection) {
                    voxels.add((VoxelizedSection) voxel);
                }
            }
            if (ColumnProfile.timing) {
                ColumnProfile.add("convert", System.nanoTime() - convertStart);
            }
            if (voxels.isEmpty()) {
                return encoded;
            }
            long encodeStart = System.nanoTime();
            for (int i = 0; i < voxels.size(); i++) {
                byte[] wire = (byte[]) encode.invoke(null, level, engine, voxels.get(i));
                if (wire != null && wire.length > 0) {
                    encoded.add(wire);
                }
            }
            if (ColumnProfile.timing) {
                ColumnProfile.add("encode", System.nanoTime() - encodeStart);
            }
        }
        if (ColumnProfile.timing) {
            int bytes = 0;
            for (int i = 0; i < encoded.size(); i++) {
                bytes += encoded.get(i).length;
            }
            ColumnProfile.sizes(voxels.size(), encoded.size(), bytes);
        }
        return encoded;
    }

    public static boolean importable(CompoundTag tag) {
        try {
            ensure();
            return tag != null && (Boolean) importable.invoke(null, tag);
        } catch (ReflectiveOperationException failure) {
            return false;
        }
    }

    private static Object converter(ServerLevel level) throws ReflectiveOperationException {
        if (CONVERTER.get() == null || CONVERTER_LEVEL.get() != level) {
            CONVERTER.set(converterCtor.newInstance(level));
            CONVERTER_LEVEL.set(level);
        }
        return CONVERTER.get();
    }

    /**
     * Same sector layout as {@code VoxyServerGeneratedChunkWalker.readChunkNbt}.
     * {@code readOut} is the file-channel time and {@code parseOut} is decompression plus NBT.
     */
    static CompoundTag readSplit(ServerLevel level, int x, int z, long[] readOut, long[] parseOut) throws ReflectiveOperationException {
        Path path = (Path) regionPath.invoke(null, level, Integer.valueOf(x >> 5), Integer.valueOf(z >> 5));
        if (path == null || !Files.exists(path)) {
            return null;
        }
        int[] header = (int[]) regionHeader.invoke(null, path);
        if (header == null) {
            return null;
        }
        int index = (x & 31) + ((z & 31) << 5);
        int packed = header[index];
        int sector = packed >>> 8;
        int count = packed & 255;
        if (sector <= 0 || count <= 0) {
            return null;
        }
        long started = System.nanoTime();
        byte[] payload;
        int versionId;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer lead = ByteBuffer.allocate(5).order(ByteOrder.BIG_ENDIAN);
            if (channel.read(lead, sector * 4096L) != 5) {
                readOut[0] = System.nanoTime() - started;
                return null;
            }
            lead.flip();
            int length = lead.getInt();
            versionId = lead.get() & 255;
            if (length <= 1 || length > count * 4096 - 4 || (versionId & 128) != 0) {
                readOut[0] = System.nanoTime() - started;
                return null;
            }
            versionId &= 127;
            payload = new byte[length - 1];
            ByteBuffer body = ByteBuffer.wrap(payload);
            if (channel.read(body, sector * 4096L + 5L) != payload.length) {
                readOut[0] = System.nanoTime() - started;
                return null;
            }
        } catch (Exception failure) {
            readOut[0] = System.nanoTime() - started;
            return null;
        }
        readOut[0] = System.nanoTime() - started;
        RegionFileVersion version = RegionFileVersion.fromId(versionId);
        if (version == null) {
            return null;
        }
        long parseStart = System.nanoTime();
        try (DataInputStream input = new DataInputStream(version.wrap(new ByteArrayInputStream(payload)))) {
            CompoundTag tag = NbtIo.read(input);
            parseOut[0] = System.nanoTime() - parseStart;
            return tag;
        } catch (Exception failure) {
            parseOut[0] = System.nanoTime() - parseStart;
            return null;
        }
    }

    private static void ensure() throws ReflectiveOperationException {
        if (regionPath != null) {
            return;
        }
        Class<?> walker = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker");
        regionPath = walker.getDeclaredMethod("getRegionPath", ServerLevel.class, int.class, int.class);
        regionPath.setAccessible(true);
        regionHeader = walker.getDeclaredMethod("loadRegionHeader", Path.class);
        regionHeader.setAccessible(true);
        importable = walker.getDeclaredMethod("isImportableChunk", CompoundTag.class);
        importable.setAccessible(true);
        Class<?> converterClass = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker$SectionConverter");
        converterCtor = converterClass.getDeclaredConstructor(ServerLevel.class);
        converterCtor.setAccessible(true);
        convert = converterClass.getDeclaredMethod("convertSection", WorldEngine.class, int.class, int.class, int.class, CompoundTag.class);
        convert.setAccessible(true);
        encode = Class.forName("me.cortex.voxy.common.network.SectionSyncPayload").getMethod("encodeSectionWireBytes", ServerLevel.class, WorldEngine.class, VoxelizedSection.class);
        offer = Class.forName("me.cortex.voxy.server.ServerTerrainTransport").getMethod("offerColumn", ServerPlayer.class, ServerLevel.class, ChunkPos.class, List.class);
    }
}
