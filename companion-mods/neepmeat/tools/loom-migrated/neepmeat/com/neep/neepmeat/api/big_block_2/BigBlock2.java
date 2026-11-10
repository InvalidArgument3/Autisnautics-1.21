/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.api.big_block_2;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BlockPlacementPreview;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BigBlock2 extends BaseBlock implements BlockPlacementPreview
{
    protected final IntegerProperty index;

    public final BigBlock2Structure structure;

    public BigBlock2(RegistrationContext ctx, ItemSettings itemSettings, Properties settings, int max)
    {
        super(ctx, itemSettings, settings.pushReaction(PushReaction.IGNORE));

        this.index = IntegerProperty.create("position", 0, max);

        this.structure = ctx.append(
                this,
                makeStructure(MeatlibBlockSettings.copyOf(settings)),
                MeatlibBlock::structure
        );
    }

    public static int getMaxIndex(BigBlockPattern pattern)
    {
        return pattern.iterable().size() - 1;
    }

    @Override
    public abstract BigBlockPattern getVolume(BlockState state);

    public abstract Int2ObjectMap<Vec3i> getIndexToOffset(BlockState state);

    protected abstract BigBlock2Structure makeStructure(MeatlibBlockSettings settings);

    protected abstract BlockState getDefaultStructureState(BlockState parentState);

    public Vec3i getOffset(BlockState state)
    {
        return getIndexToOffset(state).get((int) state.getValue(index));
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
    {
        super.spawnDestroyParticles(world, player, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return getVolume(state).toVoxelShape();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BigBlockPattern pattern = getVolume(state);
        for (var a : pattern.iterable())
        {
            if (!world.isEmptyBlock(pos.offset(a)))
            {
                return false;
            }
        }

        for (var box : pattern.toVoxelShape().toAabbs())
        {
            if (!world.noCollision(box.move(pos)))
                return false;
        }

        return super.canSurvive(state, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockState def = getDefaultStructureState(state);
        for (Int2ObjectMap.Entry<Vec3i> pair : getIndexToOffset(state).int2ObjectEntrySet())
        {
            if (pair.getIntKey() != 0)
            {
                BlockPos target = pos.offset(pair.getValue());
                world.setBlockAndUpdate(target, def.setValue(index, pair.getIntKey()));
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);

        // Allow silent replacement with structure to facilitate moving the origin block post-placement
        if (!newState.is(this) && !newState.is(structure))
        {
            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Vec3i vec : getVolume(state).iterable())
            {
                mutable.setWithOffset(pos, vec);
                if (world.getBlockState(mutable).getBlock() == structure)
                {
                    world.setBlock(mutable, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                }
            }
        }
    }

    public IntegerProperty getIndex()
    {
        return index;
    }

    public <T, C> boolean apiEnabled(BlockState state, Vec3i offset, BlockApiLookup<T, C> api)
    {
        return getVolume(state).getApi(offset).contains(api);
    }

    /// Override to expose delegates to the controller's APIs
    public @Nullable <T, C> T getApi(Level world, BlockPos pos, BlockState state, Vec3i offset, BlockApiLookup<T, C> api)
    {
        return null;
    }
}
