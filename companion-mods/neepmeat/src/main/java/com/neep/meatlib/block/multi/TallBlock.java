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
 *
 * The upper half is a dummy block. Breaking either half removes the other.
 */

package com.neep.meatlib.block.multi;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.block.BaseDummyBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class TallBlock extends BaseBlock {
    public static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 32, 16);

    private final Block structureBlock;

    public TallBlock(RegistrationContext ctx, Properties settings) {
        this(ctx, ItemSettings.block(), settings);
    }

    public TallBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings) {
        super(ctx, itemSettings, settings.pushReaction(PushReaction.IGNORE));
        this.structureBlock = createStructure(ctx);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    public final Block getStructureBlock() {
        return structureBlock;
    }

    protected abstract Structure createStructure(RegistrationContext ctx);

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.above()).isAir() && super.canSurvive(state, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockState(pos.above()).isAir()) {
            world.setBlockAndUpdate(pos.above(), structureBlock.defaultBlockState());
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.is(this) && world.getBlockState(pos.above()).is(structureBlock)) {
            world.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    public class Structure extends BaseDummyBlock {
        public Structure(Properties settings) {
            super(settings.pushReaction(PushReaction.IGNORE));
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return TallBlock.this.getShape(state, world, pos, context).move(0, -1, 0);
        }

        @Override
        protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
            if (!newState.is(this) && world.getBlockState(pos.below()).is(TallBlock.this)) {
                world.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }
}
