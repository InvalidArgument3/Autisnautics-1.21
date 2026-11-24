package dev.wyseman.autisnauticsrifts;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class RiftMarkerSavedData extends SavedData {
    private static final String NAME = "autisnautics_rifts_markers";
    private final Set<Long> v4Markers = new HashSet<>();
    private final Set<Long> v3Markers = new HashSet<>();

    public static RiftMarkerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(RiftMarkerSavedData::new, RiftMarkerSavedData::load, null),
            NAME
        );
    }

    private static RiftMarkerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        RiftMarkerSavedData data = new RiftMarkerSavedData();
        for (long l : tag.getLongArray("v4")) data.v4Markers.add(l);
        for (long l : tag.getLongArray("v3")) data.v3Markers.add(l);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLongArray("v4", v4Markers.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("v3", v3Markers.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    public boolean hasMarker(int cx, int cz, boolean legacy) {
        long key = ChunkPos.asLong(cx, cz);
        return legacy ? v3Markers.contains(key) : v4Markers.contains(key);
    }

    public void addMarker(int cx, int cz, boolean legacy) {
        long key = ChunkPos.asLong(cx, cz);
        boolean changed = legacy ? v3Markers.add(key) : v4Markers.add(key);
        if (changed) setDirty();
    }

    public void removeMarker(int cx, int cz, boolean legacy) {
        long key = ChunkPos.asLong(cx, cz);
        boolean changed = legacy ? v3Markers.remove(key) : v4Markers.remove(key);
        if (changed) setDirty();
    }
}
