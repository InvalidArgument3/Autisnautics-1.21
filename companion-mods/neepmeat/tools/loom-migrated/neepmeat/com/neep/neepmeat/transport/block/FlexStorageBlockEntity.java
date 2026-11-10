package com.neep.neepmeat.transport.block;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class FlexStorageBlockEntity<T extends FlexStorageBlockEntity<?>> extends SyncableBlockEntity
{
    protected Set<BlockPos> children = Sets.newHashSet();
    @Nullable
    protected BlockPos root;

    public FlexStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    protected abstract StorageDelegate<T> getStorageDelegate();

    public void markRoot(Set<BlockPos> children)
    {
        this.children = children;
        getStorageDelegate().validate();
    }

    public boolean isRoot()
    {
//        return pos.equals(root) || this == getRoot();
        return worldPosition.equals(root);
    }

    @Nullable
    public T getRoot()
    {
        if (root != null && level.getBlockEntity(root) instanceof FlexStorageBlockEntity<?> be)
        {
            return (T) be;
        }
        return null;
    }

    public void setRoot(T root)
    {
        if (root != this)
        {
            this.children.clear();

            if (!getStorageDelegate().isEmpty())
            {
                // If this used to be the root, move the fluid to the new root.
                try (Transaction transaction = Transaction.openOuter())
                {
                    getStorageDelegate().transferInto(root, transaction);

                    transaction.commit();
                }
            }
        }
        else
        {
            sync();
        }

        if (this.root != root.getBlockPos())
        {
            this.root = root.getBlockPos();
           sync();
        }

        // The root's children are probably invalid at this point.
    }

    public int getSize()
    {
        if (!isRoot())
        {
            var theRoot = getRoot();
            return theRoot != null ? theRoot.getSize() : 0;
        }
        else
        {
            return children.size();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        if (root != null)
        {
            nbt.put("root", NbtUtils.writeBlockPos(root));
        }

        if (children != null && !children.isEmpty())
        {
            ListTag list = new ListTag();
            for (var child : children)
            {
                CompoundTag c = NbtUtils.writeBlockPos(child);
                list.add(c);
            }

            nbt.put("children", list);
        }

    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        if (nbt.contains("root"))
        {
            this.root = NbtUtils.readBlockPos(nbt.getCompound("root"));
        }

        if (nbt.contains("children"))
        {
            children.clear();
            ListTag list = nbt.getList("children", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); ++i)
            {
                children.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    /// @param validate Return null if the structure is too large.
    @Nullable
    public static <T extends FlexStorageBlockEntity<T>> T updateConnections(Level world, T origin, BlockEntityType<T> type, int maxSize, boolean validate)
    {
        Set<T> found = findBlocks(world, origin.getBlockPos(), type);

        if (!found.isEmpty())
        {
            if (validate && found.size() > maxSize)
                return null;

            T root = null;
            int lowest = Integer.MAX_VALUE;
            for (var be : found)
            {
                if (root == null)
                    root = be;

                BlockPos bePos = be.getBlockPos();
                if (bePos.getY() < lowest)
                {
                    lowest = bePos.getY();
                    root = be;
                }
            }

            root.markRoot(found.stream().map(BlockEntity::getBlockPos).collect(Collectors.toSet()));

            for (var be : found)
            {
                be.setRoot(root);
            }

            return root;
        }

        return null;
    }

    protected static <T extends FlexStorageBlockEntity<?>> Set<T> findBlocks(Level world, BlockPos start, BlockEntityType<T> type)
    {
        Set<T> found = Sets.newHashSet();
        Queue<BlockPos> queue = Queues.newArrayDeque();
        Set<BlockPos> visited = Sets.newHashSet();

        queue.add(start);
        visited.add(start);

        world.getBlockEntity(start, type).ifPresent(found::add);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (visited.contains(mutable))
                    continue;

                visited.add(mutable.immutable());

                world.getBlockEntity(mutable, type).ifPresent(be ->
                {
                    found.add(be);
                    queue.add(mutable.immutable());
                });
            }
        }

        return found;
    }

    protected interface StorageDelegate<T>
    {
        void validate();

        /// Refers to *this* storage, not the root. Do not delegate to root.
        boolean isEmpty();

        void transferInto(T root, TransactionContext transaction);
    }

    protected static class StorageCache<V>
    {
        private final BlockApiLookup<Storage<V>, Direction> lookup;
        private final FlexStorageBlockEntity<?> parent;
        private BlockApiCache<Storage<V>, Direction> cache = null;
        private BlockPos lastRootPos = null;

        public StorageCache(BlockApiLookup<Storage<V>, Direction> lookup, FlexStorageBlockEntity<?> parent)
        {
            this.lookup = lookup;
            this.parent = parent;
        }

        public Storage<V> find()
        {
            var root = parent.root;
            if ((!Objects.equals(lastRootPos, root) || cache == null) && root != null)
            {
                lastRootPos = root;
                if (parent.getLevel() instanceof ServerLevel serverWorld)
                {
                    cache = BlockApiCache.create(lookup, serverWorld, root);
                }
                else
                {
                    return Storage.empty();
                }
            }

            if (cache != null)
            {
                var found = cache.find(Direction.DOWN);
                if (found != null)
                    return found;
            }

            return Storage.empty();
        }
    }
}
