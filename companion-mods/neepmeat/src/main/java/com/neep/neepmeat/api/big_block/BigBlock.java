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

package com.neep.neepmeat.api.big_block;

import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BigBlock<T extends BigBlockStructure<?>> extends Block implements BlockPlacementPreview {
    private final T structureBlock;

    public BigBlock(RegistrationContext ctx, Properties settings) {
        super(settings.pushReaction(PushReaction.IGNORE));
        this.structureBlock = registerStructureBlock(ctx);
    }

    protected abstract T registerStructureBlock(RegistrationContext ctx);

    @Override
    public abstract BigBlockPattern getVolume(BlockState blockState);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getVolume(state).toVoxelShape();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        for (Vec3i offset : getVolume(state).iterable()) {
            if (!world.isEmptyBlock(pos.offset(offset))) {
                return false;
            }
        }
        for (var box : getVolume(state).toVoxelShape().toAabbs()) {
            if (!world.isUnobstructed(null, Shapes.create(box.move(pos)))) {
                return false;
            }
        }
        return super.canSurvive(state, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        getVolume(state).placeBlocks(world, pos, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, world, pos, newState, moved);
        if (!newState.is(this)) {
            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Vec3i offset : getVolume(state).iterable()) {
                mutable.setWithOffset(pos, offset);
                if (mutable.equals(pos)) {
                    continue;
                }
                world.setBlock(mutable, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public T getStructure() {
        return structureBlock;
    }

    public boolean survivesPreview(BlockState state, LevelReader world, BlockPos pos) {
        return canSurvive(state, world, pos);
    }
}
