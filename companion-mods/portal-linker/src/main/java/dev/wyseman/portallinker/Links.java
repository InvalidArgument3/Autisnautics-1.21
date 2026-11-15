package dev.wyseman.portallinker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import qouteall.imm_ptl.core.portal.PortalPlaceholderBlock;
import qouteall.imm_ptl.core.portal.nether_portal.BreakablePortalEntity;

/**
 * Active links, linker selections, and breaks whose far chunk was not loaded.
 * Stored on the overworld so a cross-dimension pair survives a restart.
 */
public final class Links extends SavedData {
    public static final String ID = "portal_linker";
    public static final SavedData.Factory<Links> FACTORY = new SavedData.Factory<>(Links::new, Links::load);

    private final Map<UUID, FrameEnd.Selection> selections = new HashMap<>();
    private final Map<UUID, Link> links = new HashMap<>();
    private final Map<DimPos, UUID> occupied = new HashMap<>();

    public Links() {}

    public static Links get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public FrameEnd.Selection selection(UUID token) {
        return selections.get(token);
    }

    public void putSelection(FrameEnd.Selection selection) {
        selections.put(selection.token, selection);
        setDirty();
    }

    public void removeSelection(UUID token) {
        if (selections.remove(token) != null) {
            setDirty();
        }
    }

    public Link linkAt(ServerLevel level, BlockPos pos) {
        UUID id = occupied.get(key(level, pos));
        if (id == null) {
            return null;
        }
        Link link = links.get(id);
        return link == null || link.breaking ? null : link;
    }

    public boolean occupied(FrameEnd end) {
        for (BlockPos pos : end.frame) {
            if (occupied.containsKey(key(end, pos))) {
                return true;
            }
        }
        for (BlockPos pos : end.interior) {
            if (occupied.containsKey(key(end, pos))) {
                return true;
            }
        }
        return false;
    }

    /** Placeholders exist, entities might not. A restart cleans this up. */
    public void begin(Link link) {
        link.breaking = true;
        links.put(link.id, link);
        setDirty();
    }

    public void commit(UUID id) {
        Link link = links.get(id);
        if (link == null) {
            return;
        }
        link.breaking = false;
        index(link);
        setDirty();
    }

    /** Drop a link that never finished spawning, and any blocks it already placed. */
    public void abort(MinecraftServer server, UUID id) {
        Link link = links.get(id);
        if (link == null) {
            return;
        }
        link.breaking = true;
        unindex(link);
        if (finishLoaded(server, link)) {
            links.remove(id);
        }
        setDirty();
    }

    public void onBlockChanged(ServerLevel level, BlockPos pos) {
        UUID id = occupied.get(key(level, pos));
        if (id == null) {
            return;
        }
        Link link = links.get(id);
        if (link == null || link.breaking) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        boolean framePos = contains(link.from.frame, pos) && link.from.dimension.equals(level.dimension())
                || contains(link.to.frame, pos) && link.to.dimension.equals(level.dimension());
        if (framePos) {
            if (!FrameFinder.isFrame(state)) {
                dismantle(level.getServer(), id);
            }
            return;
        }
        if (state.getBlock() != PortalPlaceholderBlock.instance) {
            dismantle(level.getServer(), id);
        }
    }

    public void dismantle(MinecraftServer server, UUID id) {
        Link link = links.get(id);
        if (link == null || link.breaking) {
            return;
        }
        link.breaking = true;
        unindex(link);
        boolean pending = !finishLoaded(server, link);
        if (!pending) {
            links.remove(id);
        }
        setDirty();
        PortalLinker.LOG.info("Closed portal link {}", id);
    }

    /** Called a few times a second, and after a chunk loads. Does not force-load anything. */
    public void sweep(MinecraftServer server) {
        if (links.isEmpty()) {
            return;
        }
        List<UUID> broken = new ArrayList<>();
        List<UUID> finished = new ArrayList<>();
        for (Link link : links.values()) {
            if (!link.breaking && !link.orientSynced && LinkRotation.sync(server, link)) {
                link.orientSynced = true;
                setDirty();
            }
            if (link.breaking) {
                if (finishLoaded(server, link)) {
                    finished.add(link.id);
                }
            } else if (loadedFrameBroken(server, link)) {
                broken.add(link.id);
            }
        }
        for (UUID id : finished) {
            links.remove(id);
            setDirty();
        }
        for (UUID id : broken) {
            dismantle(server, id);
        }
    }

    private boolean loadedFrameBroken(MinecraftServer server, Link link) {
        return sideBroken(server, link.from) || sideBroken(server, link.to);
    }

    private boolean sideBroken(MinecraftServer server, FrameEnd end) {
        ServerLevel level = server.getLevel(end.dimension);
        if (level == null) {
            return false;
        }
        for (BlockPos pos : end.frame) {
            if (!chunkLoaded(level, pos)) {
                continue;
            }
            if (!FrameFinder.isFrame(level.getBlockState(pos))) {
                return true;
            }
        }
        for (BlockPos pos : end.interior) {
            if (!chunkLoaded(level, pos)) {
                continue;
            }
            if (level.getBlockState(pos).getBlock() != PortalPlaceholderBlock.instance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes portals and placeholder blocks that are currently loaded.
     * Returns true when nothing is left to do.
     */
    private boolean finishLoaded(MinecraftServer server, Link link) {
        boolean done = true;
        for (PortalRef ref : link.portals) {
            ServerLevel level = server.getLevel(ref.dimension());
            if (level == null) {
                done = false;
                continue;
            }
            BlockPos anchor = BlockPos.of(ref.anchor());
            if (!chunkLoaded(level, anchor)) {
                done = false;
                continue;
            }
            Entity entity = level.getEntity(ref.id());
            if (entity instanceof BreakablePortalEntity portal && !portal.isRemoved()) {
                portal.breakPortalNow();
            }
        }
        done = clearLoaded(server, link.from) && done;
        done = clearLoaded(server, link.to) && done;
        return done;
    }

    private boolean clearLoaded(MinecraftServer server, FrameEnd end) {
        ServerLevel level = server.getLevel(end.dimension);
        if (level == null) {
            return false;
        }
        boolean done = true;
        for (BlockPos pos : end.interior) {
            if (!chunkLoaded(level, pos)) {
                done = false;
                continue;
            }
            if (level.getBlockState(pos).getBlock() == PortalPlaceholderBlock.instance) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        return done;
    }

    private void index(Link link) {
        indexEnd(link.id, link.from);
        indexEnd(link.id, link.to);
    }

    private void indexEnd(UUID id, FrameEnd end) {
        for (BlockPos pos : end.frame) {
            occupied.put(key(end, pos), id);
        }
        for (BlockPos pos : end.interior) {
            occupied.put(key(end, pos), id);
        }
    }

    private void unindex(Link link) {
        occupied.values().removeIf(id -> id.equals(link.id));
    }

    private static boolean contains(Set<BlockPos> positions, BlockPos pos) {
        return positions.contains(pos);
    }

    private static boolean chunkLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static DimPos key(FrameEnd end, BlockPos pos) {
        return new DimPos(end.dimension.location().toString(), pos.asLong());
    }

    private static DimPos key(ServerLevel level, BlockPos pos) {
        return new DimPos(level.dimension().location().toString(), pos.asLong());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("version", 2);
        ListTag selectionList = new ListTag();
        for (FrameEnd.Selection selection : selections.values()) {
            selectionList.add(selection.save());
        }
        tag.put("selections", selectionList);
        ListTag linkList = new ListTag();
        for (Link link : links.values()) {
            linkList.add(link.save());
        }
        tag.put("links", linkList);
        return tag;
    }

    public static Links load(CompoundTag tag, HolderLookup.Provider registries) {
        Links data = new Links();
        for (Tag entry : tag.getList("selections", Tag.TAG_COMPOUND)) {
            FrameEnd.Selection selection = FrameEnd.Selection.load((CompoundTag) entry);
            data.selections.put(selection.token, selection);
        }
        for (Tag entry : tag.getList("links", Tag.TAG_COMPOUND)) {
            Link link = Link.load((CompoundTag) entry);
            data.links.put(link.id, link);
            if (!link.breaking) {
                data.index(link);
            }
        }
        return data;
    }

    public record PortalRef(UUID id, ResourceKey<Level> dimension, long anchor) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putString("dim", dimension.location().toString());
            tag.putLong("anchor", anchor);
            return tag;
        }

        static PortalRef load(CompoundTag tag) {
            ResourceKey<Level> dimension = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.parse(tag.getString("dim")));
            return new PortalRef(tag.getUUID("id"), dimension, tag.getLong("anchor"));
        }
    }

    public static final class Link {
        public final UUID id;
        public final FrameEnd from;
        public final FrameEnd to;
        public final List<PortalRef> portals;
        public boolean breaking;
        /** Exit mode. FRONT is the upright default, including links saved before 1.1.0. */
        public LinkOrientation orientation;
        /** Not saved. False after load so the next sweep rewrites rotation onto portals that were saved by 1.0.0. */
        public boolean orientSynced;

        public Link(UUID id, FrameEnd from, FrameEnd to, List<PortalRef> portals, boolean breaking, LinkOrientation orientation) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.portals = List.copyOf(portals);
            this.breaking = breaking;
            this.orientation = orientation == null ? LinkOrientation.FRONT : orientation;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.put("from", from.save());
            tag.put("to", to.save());
            tag.putBoolean("breaking", breaking);
            tag.putString("orientation", orientation.name());
            ListTag list = new ListTag();
            for (PortalRef ref : portals) {
                list.add(ref.save());
            }
            tag.put("portals", list);
            return tag;
        }

        static Link load(CompoundTag tag) {
            List<PortalRef> refs = new ArrayList<>();
            for (Tag entry : tag.getList("portals", Tag.TAG_COMPOUND)) {
                refs.add(PortalRef.load((CompoundTag) entry));
            }
            Link link = new Link(tag.getUUID("id"), FrameEnd.load(tag.getCompound("from")), FrameEnd.load(tag.getCompound("to")), refs, tag.getBoolean("breaking"), LinkOrientation.byName(tag.getString("orientation")));
            link.orientSynced = false;
            return link;
        }
    }

    private record DimPos(String dim, long pos) {}

    /** Present so the sweep can be called with an explicit server from tests. */
    public int activeCount() {
        int n = 0;
        for (Link link : links.values()) {
            if (!link.breaking) {
                n++;
            }
        }
        return n;
    }

    public Set<UUID> activeIds() {
        Set<UUID> ids = new HashSet<>();
        for (Link link : links.values()) {
            if (!link.breaking) {
                ids.add(link.id);
            }
        }
        return ids;
    }
}
