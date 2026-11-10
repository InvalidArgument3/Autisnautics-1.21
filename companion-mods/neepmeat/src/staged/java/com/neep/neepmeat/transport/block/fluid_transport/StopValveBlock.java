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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.util.Wrenchable;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StopValveBlock extends FluidPipeBlock implements EntityBlock, Wrenchable
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public StopValveBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, PipeCol.ANY, itemSettings, settings.noOcclusion());
        this.registerDefaultState(defaultBlockState()
                .setValue(OPEN, true)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (player.getItemInHand(hand).is(NMTags.WRENCH))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (ItemUtil.playerHoldingPipe(player, hand))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (!world.isClientSide())
        {
            world.setBlockAndUpdate(pos, state.cycle(OPEN));
        }

        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (world.isClientSide())
            return;

        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED))
        {
            if (state.getValue(OPEN) == powered)
            {
                state = state.setValue(OPEN, !powered);
            }
            world.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
//            updateNetwork((ServerWorld) world, pos, state, PipeNetwork.UpdateReason.VALVE_CHANGED);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState state = super.getStateForPlacement(ctx);

        if (state != null)
        {
            state = state.setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
            boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
            state = state.setValue(OPEN, !powered);
            state = state.setValue(POWERED, powered);
        }

        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, POWERED, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.STOP_VALVE.create(pos, state);
//        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        if (!world.isClientSide())
        {
            // TODO: jank
            return ((world1, pos, state1, blockEntity) -> ((FluidPipeBlockEntity<?>) blockEntity).tick());
        }
        return null;
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        return false;
    }

    public static class StopValveFluidPipeVertex extends BlockFluidPipeVertex
    {
        public StopValveFluidPipeVertex(FluidPipeBlockEntity<?> fluidPipeBlockEntity)
        {
            super(fluidPipeBlockEntity);
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public long canInsert(ServerLevel world, int inDir, FluidVariant variant, long maxAmount)
        {
            long superAmount = super.canInsert(world, inDir, variant, maxAmount);
            return parent.getBlockState().getValue(OPEN) ? superAmount : 0;
        }

        @Override
        public long canOutput(ServerLevel world, int outDir, FluidVariant variant, long maxAmount)
        {
            long superAmount = super.canOutput(world, outDir, variant, maxAmount);
            return parent.getBlockState().getValue(OPEN) ? superAmount : 0;
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}
