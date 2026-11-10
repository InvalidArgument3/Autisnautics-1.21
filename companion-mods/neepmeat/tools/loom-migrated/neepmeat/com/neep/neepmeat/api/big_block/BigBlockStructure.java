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
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public abstract class BigBlockStructure<T extends BigBlockStructureEntity> extends Block implements MeatlibBlock, EntityBlock
{
    protected final BigBlock<?> parent;
    private final BlockEntityType<T> blockEntityType;

    public BigBlockStructure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<T> registerBlockEntity)
    {
        super(settings.noOcclusion().forceSolidOn().pushReaction(PushReaction.IGNORE));
        this.parent = parent;
        this.blockEntityType = registerBlockEntity.get(this);
    }

    public BigBlockStructure(BigBlock<?> parent, Properties settings)
    {
        this(parent, settings, BigBlockStructure::registerBlockEntity);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    public String getDescriptionId()
    {
        return parent.getDescriptionId();
    }

    protected BlockEntityType<T> registerBlockEntity()
    {
        throw new NotImplementedException("You fool! Implement this or supply a BlockEntityRegistererererer in the super constructor.");
    }

    public BlockEntityType<T> getBlockEntityType()
    {
        return blockEntityType;
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be)
        {
            BlockPos controllerPos = be.getControllerPos();
            if (controllerPos == null)
                return Shapes.block();

            BlockState parentState = world.getBlockState(controllerPos);
            if (parentState.is(parent)) // Sometimes air replaces the parent (not sure why)
               return be.translateShape(parent.getShape(parentState, world, pos, context));
        }

        return Shapes.empty();
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(state.getBlock()))
        {
            // Remove the controller block and let it handle the destruction of the rest of the structure.
            if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be)
            {
                world.destroyBlock(be.getControllerPos(), false);
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
    {
//        world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(parent.getDefaultState()));
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public ItemLike dropsLike()
    {
        return parent;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
    {
        return parent.getCloneItemStack(world, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return blockEntityType.create(pos, state);
    }

    public static class Simple<T extends BigBlockStructureEntity> extends BigBlockStructure<T>
    {
//        private final BlockEntityRegisterererer<T> registerBlockEntity;

        public Simple(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<T> registerBlockEntity)
        {
            super(parent, settings, registerBlockEntity);
//            this.registerBlockEntity = registerBlockEntity;
        }

        @Override
        protected BlockEntityType<T> registerBlockEntity()
        {
//            return registerBlockEntity.get(this);
            return null;
        }

        @Override
        public String toString()
        {
            return "BigBlockStructure{" + BuiltInRegistries.BLOCK.getKey(this) + "}";
        }
    }

    @FunctionalInterface
    public interface BlockEntityRegisterererer<T extends BigBlockStructureEntity>
    {
        BlockEntityType<T> get(BigBlockStructure<T> structure);
    }
}
