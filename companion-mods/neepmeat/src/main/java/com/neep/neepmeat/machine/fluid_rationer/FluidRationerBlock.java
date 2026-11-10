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

package com.neep.neepmeat.machine.fluid_rationer;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class FluidRationerBlock extends BaseFacingBlock implements EntityBlock, NeepBusProvider, DataCable
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<RedstoneMode> MODE = EnumProperty.create("redstone_mode", RedstoneMode.class);

    public FluidRationerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, true).setValue(MODE, RedstoneMode.ACTIVE_WITHOUT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = super.getStateForPlacement(context);

        if (state == null)
            return null;

        state = state.setValue(ACTIVE, state.getValue(MODE).active(context.getLevel().hasNeighborSignal(context.getClickedPos())));

        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        var nextState = state;

        nextState = nextState.setValue(ACTIVE, state.getValue(MODE).active(world.hasNeighborSignal(pos)));

        world.setBlockAndUpdate(pos, nextState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (player.isShiftKeyDown())
        {
            var newState = state.cycle(MODE);
            newState = newState.setValue(ACTIVE, newState.getValue(MODE).active(world.hasNeighborSignal(pos)));

            world.setBlock(pos, newState, UPDATE_ALL);
            world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 1, 1);

            return InteractionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof FluidRationerBlockEntity be)
        {
            player.openMenu(be);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.FLUID_RATIONER.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.FLUID_RATIONER, FluidRationerBlockEntity::serverTick, null, world);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }
}
