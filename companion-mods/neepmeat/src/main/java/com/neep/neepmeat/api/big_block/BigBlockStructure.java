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

import com.neep.meatlib.block.MeatlibBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public abstract class BigBlockStructure<T extends BigBlockStructureEntity> extends Block implements MeatlibBlock, EntityBlock {
    protected final BigBlock<?> parent;
    private final BlockEntityType<T> blockEntityType;
    private final BiFunction<BlockPos, BlockState, T> factory;

    public BigBlockStructure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<T> registerBlockEntity, BiFunction<BlockPos, BlockState, T> factory) {
        super(settings.noOcclusion().forceSolidOn().pushReaction(PushReaction.IGNORE));
        this.parent = parent;
        this.factory = factory;
        this.blockEntityType = registerBlockEntity.get(this);
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public String getDescriptionId() {
        return parent.getDescriptionId();
    }

    public BlockEntityType<T> getBlockEntityType() {
        return blockEntityType;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity entity && entity.getControllerPos() != null) {
            BlockPos controllerPos = entity.getControllerPos();
            BlockState parentState = world.getBlockState(controllerPos);
            if (parentState.is(parent)) {
                BlockPos relative = pos.subtract(controllerPos);
                return parent.getShape(parentState, world, pos, context).move(-relative.getX(), -relative.getY(), -relative.getZ());
            }
        }
        return Shapes.block();
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.is(state.getBlock()) && world.getBlockEntity(pos) instanceof BigBlockStructureEntity entity && entity.getControllerPos() != null) {
            world.destroyBlock(entity.getControllerPos(), false);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hit, LevelReader level, BlockPos pos, Player player) {
        return parent.getCloneItemStack(state, hit, level, pos, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    public static class Simple<T extends BigBlockStructureEntity> extends BigBlockStructure<T> {
        public Simple(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<T> registerBlockEntity, BiFunction<BlockPos, BlockState, T> factory) {
            super(parent, settings, registerBlockEntity, factory);
        }

        @Override
        public String toString() {
            return "BigBlockStructure{" + BuiltInRegistries.BLOCK.getKey(this) + "}";
        }
    }

    @FunctionalInterface
    public interface BlockEntityRegisterererer<T extends BigBlockStructureEntity> {
        BlockEntityType<T> get(BigBlockStructure<T> structure);
    }
}
