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

package com.neep.meatlib.block.multi;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.block.BaseDummyBlock;
import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class TallerBlock extends BaseBlock {
    public static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 32, 16);

    private final TallerBlock.Structure structureBlock;
    private final IntegerProperty heightProperty;
    private final int maxHeight;

    public TallerBlock(RegistrationContext ctx, IntegerProperty heightProperty, ItemSettings itemSettings, Properties settings) {
        super(ctx, itemSettings, settings.pushReaction(PushReaction.IGNORE));
        this.heightProperty = heightProperty;
        this.maxHeight = heightProperty.getPossibleValues().stream().max(Integer::compare).orElse(1) + 1;
        this.structureBlock = createStructure(ctx);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    public final Block getStructureBlock() {
        return structureBlock;
    }

    public IntegerProperty getHeightProperty() {
        return heightProperty;
    }

    protected abstract Structure createStructure(RegistrationContext ctx);

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + maxHeight, pos.getZ() + 1);
        if (!super.canSurvive(state, world, pos) || !world.isUnobstructed(null, net.minecraft.world.phys.shapes.Shapes.create(box))) {
            return false;
        }
        boolean valid = true;
        for (int i = 0; i < maxHeight; i++) {
            valid = valid && world.getBlockState(pos.above(i)).isAir();
        }
        return valid;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        for (int i = 1; i < maxHeight; ++i) {
            BlockPos upPos = pos.above(i);
            if (world.getBlockState(upPos).isAir()) {
                world.setBlockAndUpdate(pos.above(i), structureBlock.getState(state).setValue(heightProperty, i));
            }
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.is(this)) {
            for (int i = 1; i < maxHeight; ++i) {
                BlockPos upPos = pos.above(i);
                if (world.getBlockState(upPos).is(structureBlock)) {
                    world.setBlockAndUpdate(upPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1;
    }

    public class Structure extends BaseDummyBlock implements MeatlibBlockExtension {
        public Structure(Properties settings) {
            super(settings.pushReaction(PushReaction.IGNORE));
        }

        protected int getHeight(BlockState state) {
            return state.getValue(getHeightProperty());
        }

        protected BlockState getState(BlockState baseState) {
            return defaultBlockState();
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return TallerBlock.this.getShape(state, world, pos, context).move(0, -1, 0);
        }

        @Override
        protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
            BlockPos basePos = pos.below(getHeight(state));
            if (!newState.is(this) && world.getBlockState(basePos).is(TallerBlock.this)) {
                world.setBlockAndUpdate(basePos, Blocks.AIR.defaultBlockState());
            }
            super.onRemove(state, world, pos, newState, moved);
        }

        @Override
        public String getDescriptionId() {
            return TallerBlock.this.getDescriptionId();
        }

        public ItemLike dropsLike() {
            return TallerBlock.this;
        }

        @Override
        protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
            return 1;
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult hit, LevelReader level, BlockPos pos, Player player) {
            return TallerBlock.this.getCloneItemStack(state, hit, level, pos, player);
        }

        @Override
        protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
            world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(TallerBlock.this.defaultBlockState()));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(heightProperty);
        }
    }
}
