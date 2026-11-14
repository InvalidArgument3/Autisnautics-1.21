package com.neep.neepmeat.transport.block;

import com.google.common.collect.Sets;
import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FlexStorageBlock<T extends FlexStorageBlockEntity<T>> extends BaseBlock implements EntityBlock
{
    public final int maxSize = 64;
    protected final Supplier<BlockEntityType<T>> typeSupplier;

    public FlexStorageBlock(RegistrationContext ctx, Supplier<BlockEntityType<T>> type, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.typeSupplier = type;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        if (!isAllowedAtPos(world, pos))
            return false;

        return super.canSurvive(state, world, pos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        var state = super.getStateForPlacement(ctx);

        if (state == null)
            return null;

        var world = ctx.getLevel();
        var pos = ctx.getClickedPos();

        // Enforce sizing rules.
        // Due to the canPlaceAt implementation, this hopefully isn't entirely necessary.
        // This might eat the item, but at least it prevents violations.
        if (!isAllowedAtPos(world, pos))
            return null;

        return state;
    }

    protected boolean isAllowedAtPos(LevelReader world, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        Set<FlexStorageBlockEntity<?>> roots = new HashSet<>();
        int adjacentSize = 0;
        for (var face : Direction.values())
        {
            mutable.setWithOffset(pos, face);
            if (world.getBlockState(mutable).is(this)
                    && world.getBlockEntity(mutable) instanceof FlexStorageBlockEntity<?> be)
            {
                var root = be.getRoot();
                if (root != null && !roots.contains(root))
                {
                    adjacentSize += root.getSize();
                    roots.add(root);
                }

                if (adjacentSize >= maxSize)
                {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide())
        {
            enforce(world, pos, typeSupplier.get());
        }
    }

    private void enforce(Level world, BlockPos pos, BlockEntityType<T> type)
    {
        world.getBlockEntity(pos, type).ifPresent(be ->
        {
            var result = FlexStorageBlockEntity.updateConnections(world, be, type, maxSize, true);
            if (result == null)
            {
                world.destroyBlock(pos, true);
            }
        });
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && !world.isClientSide())
        {
            // Get the block entity before it is removed
            world.getBlockEntity(pos, typeSupplier.get()).ifPresent(be ->
            {
                // Block entity will be removed here
                super.onRemove(state, world, pos, newState, moved);

                Set<T> adjacent = Sets.newHashSet();

                BlockPos.MutableBlockPos mutable = pos.mutable();
                for (Direction direction : Direction.values())
                {
                    mutable.setWithOffset(pos, direction);

                    world.getBlockEntity(mutable, typeSupplier.get()).ifPresent(adjacent::add);
                }

                for (T adj : adjacent)
                {
                    var root = FlexStorageBlockEntity.updateConnections(world, adj, typeSupplier.get(), maxSize, false);
                    be.setRoot(root); // Move as much fluid as possible into the new root(s).
                }

                if (adjacent.isEmpty())
                {
                    onRootDestroyed(be);
                }
            });
        }
    }

    protected void onRootDestroyed(T be)
    {

    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return typeSupplier.get().create(pos, state);
    }
}
