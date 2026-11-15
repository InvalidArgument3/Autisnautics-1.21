package dev.wyseman.portallinker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;

/** One end of a link, or a frame remembered on the linker. */
public final class FrameEnd {
    public final ResourceKey<Level> dimension;
    public final Direction.Axis axis;
    public final BlockPos anchor;
    public final int u;
    public final int v;
    public final Set<BlockPos> interior;
    public final Set<BlockPos> frame;

    public FrameEnd(ResourceKey<Level> dimension, Direction.Axis axis, BlockPos anchor, int u, int v, Set<BlockPos> interior, Set<BlockPos> frame) {
        this.dimension = dimension;
        this.axis = axis;
        this.anchor = anchor.immutable();
        this.u = u;
        this.v = v;
        this.interior = Set.copyOf(interior);
        this.frame = Set.copyOf(frame);
    }

    public static FrameEnd fromShape(ResourceKey<Level> dimension, FrameFinder.Hit hit) {
        BlockPortalShape shape = hit.shape();
        return new FrameEnd(dimension, shape.axis, shape.anchor, hit.u(), hit.v(), shape.area, shape.frameAreaWithCorner);
    }

    public BlockPortalShape shape() {
        return new BlockPortalShape(new HashSet<>(interior), axis);
    }

    public boolean sameOpening(FrameEnd other) {
        return OpeningSize.sameOpening(u, v, other.u, other.v);
    }

    public boolean samePlace(FrameEnd other) {
        return dimension.equals(other.dimension) && interior.equals(other.interior);
    }

    public boolean overlaps(FrameEnd other) {
        if (!dimension.equals(other.dimension)) {
            return false;
        }
        for (BlockPos pos : interior) {
            if (other.interior.contains(pos) || other.frame.contains(pos)) {
                return true;
            }
        }
        for (BlockPos pos : frame) {
            if (other.interior.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", dimension.location().toString());
        tag.putString("axis", axis.getName());
        tag.putLong("anchor", anchor.asLong());
        tag.putInt("u", u);
        tag.putInt("v", v);
        tag.putLongArray("interior", pack(interior));
        tag.putLongArray("frame", pack(frame));
        return tag;
    }

    public static FrameEnd load(CompoundTag tag) {
        ResourceLocation dimId = ResourceLocation.parse(tag.getString("dim"));
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimId);
        Direction.Axis axis = Direction.Axis.byName(tag.getString("axis"));
        if (axis == null) {
            axis = Direction.Axis.Y;
        }
        return new FrameEnd(dimension, axis, BlockPos.of(tag.getLong("anchor")), tag.getInt("u"), tag.getInt("v"), unpack(tag.getLongArray("interior")), unpack(tag.getLongArray("frame")));
    }

    static long[] pack(Set<BlockPos> positions) {
        long[] packed = new long[positions.size()];
        int i = 0;
        for (BlockPos pos : positions) {
            packed[i++] = pos.asLong();
        }
        return packed;
    }

    static Set<BlockPos> unpack(long[] packed) {
        Set<BlockPos> positions = new HashSet<>();
        for (long value : packed) {
            positions.add(BlockPos.of(value));
        }
        return positions;
    }

    public static final class Selection {
        public final UUID token;
        public final FrameEnd end;

        public Selection(UUID token, FrameEnd end) {
            this.token = token;
            this.end = end;
        }

        public CompoundTag save() {
            CompoundTag tag = end.save();
            tag.putUUID("token", token);
            return tag;
        }

        public static Selection load(CompoundTag tag) {
            UUID token = tag.getUUID("token");
            return new Selection(token, FrameEnd.load(tag));
        }
    }
}
