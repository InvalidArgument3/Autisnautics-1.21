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

package com.neep.neepmeat.api.multiblock2;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class MultiBlockStructure<T extends BigBlockStructureEntity> extends Block implements MeatlibBlock, EntityBlock
{
    protected final Multiblock2ControllerBlock<?> parent;
    private final BlockEntityType<T> blockEntityType;

    public MultiBlockStructure(Multiblock2ControllerBlock<?> parent, Properties settings)
    {
        super(settings.noOcclusion().forceSolidOn().pushReaction(PushReaction.IGNORE));
        this.parent = parent;
        this.blockEntityType = registerBlockEntity();
    }

    protected abstract BlockEntityType<T> registerBlockEntity();

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
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be)
        {
            BlockPos controllerPos = be.getControllerPos();
            if (controllerPos == null)
                return Shapes.block();

            BlockState parentState = world.getBlockState(controllerPos);
            if (parentState.is(parent)) // Sometimes air replaces the parent (not sure why)
               return be.translateShape(parent.getAssembledShape(parentState, world, pos, context));
        }

        return Shapes.block();
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(state.getBlock()))
        {
            // Remove the controller block and let it handle the destruction of the rest of the structure.
            if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be && be.getControllerPos() != null)
            {
                BlockPos controllerPos = be.getControllerPos();
                BlockState controllerState = world.getBlockState(controllerPos);
                if (controllerPos != null && controllerState.is(parent) && controllerState.getValue(Multiblock2ControllerBlock.ASSEMBLED))
                {
                    world.setBlockAndUpdate(controllerPos, controllerState.setValue(Multiblock2ControllerBlock.ASSEMBLED, false));
                    parent.disassemble(world, controllerPos, world.getBlockState(controllerPos), pos);
                }
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
        return null;
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state)
    {
        return parent.getCloneItemStack(world, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return blockEntityType.create(pos, state);
    }
}
