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

package com.neep.neepmeat.transport.block.energy_transport;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.block.energy_transport.entity.VascularCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VascularCondenserBlock extends BaseBlock implements EntityBlock, NeepBusProvider, DataCable
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<RedstoneMode> REDSTONE_MODE = EnumProperty.create("redstone_mode", RedstoneMode.class);

    public VascularCondenserBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, true).setValue(REDSTONE_MODE, RedstoneMode.ACTIVE_WITHOUT));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (VascularConduitBlock.matches(player.getMainHandItem()))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (player.isShiftKeyDown())
        {
            var newState = state.cycle(REDSTONE_MODE);
            newState = newState.setValue(ACTIVE, newState.getValue(REDSTONE_MODE).active(world.hasNeighborSignal(pos)));

            world.setBlock(pos, newState, UPDATE_ALL);
            world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 1, 1);

            return InteractionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof VascularCondenserBlockEntity be)
        {
            be.openScreen(player);

//            if (!world.isClient())
//                player.sendMessage(Text.of(be.getStored() / (double) be.getCapacity() * 100 + "%"), true);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);

        var nextState = state;

        nextState = nextState.setValue(ACTIVE, state.getValue(REDSTONE_MODE).active(world.hasNeighborSignal(pos)));

        world.setBlockAndUpdate(pos, nextState);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof VascularCondenserBlockEntity be)
        {
            be.justPlaced = true;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, REDSTONE_MODE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.VASCULAR_CONDENSER.create(pos, state);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
    }
}
