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

package com.neep.neepmeat.machine.small_trommel;

import com.neep.meatlib.block.BaseDummyBlock;
import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SmallTrommelBlock extends BaseHorFacingBlock implements EntityBlock
{
    public SmallTrommelBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.pushReaction(PushReaction.IGNORE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.SMALL_TROMMEL.create(pos, state);
    }

    public static void createStructure(Level world, BlockPos pos, Direction facing)
    {
        world.setBlock(pos, NMBlocks.SMALL_TROMMEL_STRUCTURE.defaultBlockState().setValue(FACING, facing), Block.UPDATE_ALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockPos offsetPos = pos.relative(state.getValue(FACING));
        return super.canSurvive(state, world, pos) && world.isEmptyBlock(offsetPos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        Direction facing = state.getValue(FACING);
        BlockPos facingPos = pos.relative(facing);
        if (world.isEmptyBlock(facingPos))
        {
            createStructure(world, facingPos, facing);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);
        Direction facing = state.getValue(FACING);
        BlockPos facingPos = pos.relative(facing);
        if (world.getBlockState(facingPos).is(NMBlocks.SMALL_TROMMEL_STRUCTURE))
        {
            world.setBlock(facingPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static class Structure extends BaseDummyBlock implements ItemPipeConnectable
    {
        public static final FluidPump PUMP = FluidPump.of(-0.5f, () -> AcceptorModes.PUSH);

        private final SmallTrommelBlock parent;

        public Structure(Properties settings, SmallTrommelBlock parent)
        {
            super(settings.noOcclusion().pushReaction(PushReaction.IGNORE));
            this.parent = parent;
            this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
        }

        @Override
        public ItemLike dropsLike()
        {
            return NMBlocks.SMALL_TROMMEL;
        }

        @Override
        public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
        {
            return 1.0f;
        }

        @Override
        public RenderShape getRenderShape(BlockState state)
        {
            return RenderShape.INVISIBLE;
        }

        @Override
        public String getDescriptionId()
        {
            return parent.getDescriptionId();
        }

        @Override
        protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
        {
            Direction facing = state.getValue(FACING);
            BlockPos offsetPos = pos.relative(facing.getOpposite());
            if (world.getBlockState(offsetPos).is(NMBlocks.SMALL_TROMMEL))
            {
                world.setBlock(offsetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            super.onRemove(state, world, pos, newState, moved);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            builder.add(FACING);
        }

        @Override
        public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state)
        {
            return NMBlocks.SMALL_TROMMEL.getCloneItemStack(world, pos, state);
        }

        public static Storage<FluidVariant> getFluidStorage(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, Direction direction)
        {
            Direction facing = state.getValue(FACING);
            BlockPos offsetPos = pos.relative(facing.getOpposite());
            if (direction == Direction.DOWN && world.getBlockEntity(offsetPos) instanceof SmallTrommelBlockEntity be)
            {
                return be.getOutputFluidStorage();
            }
            return null;
        }

        public static FluidPump getFluidPump(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, Direction direction)
        {
            if (direction == Direction.DOWN)
            {
                return PUMP;
            }
            return null;
        }

        public static Storage<ItemVariant> getItemStorage(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, Direction direction)
        {
            Direction facing = state.getValue(FACING);
            BlockPos offsetPos = pos.relative(facing.getOpposite());
            if (direction == facing && world.getBlockEntity(offsetPos) instanceof SmallTrommelBlockEntity be)
            {
                return be.getOutputItemStorage();
            }
            return null;
        }
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        return false;
    }
}
