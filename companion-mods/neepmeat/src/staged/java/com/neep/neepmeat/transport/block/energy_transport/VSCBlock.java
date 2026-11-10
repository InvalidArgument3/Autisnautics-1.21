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

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.util.PortMap;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCBlockEntity;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class VSCBlock extends BaseFacingBlock implements EntityBlock, DataCable, NeepBusProvider
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<VSCMode> MODE = EnumProperty.create("mode", VSCMode.class);

    // Vascular Source Converter!
    public VSCBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (VascularConduitBlock.matches(player.getMainHandItem()))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (player.getItemInHand(hand).isEmpty() && player.isShiftKeyDown())
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof VSCBlockEntity be)
            {
                be.changeMode();
                world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof VSCBlockEntity be)
        {
            if (!world.isClientSide())
                player.openMenu(be);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.VSC.create(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof VSCBlockEntity be)
        {
            be.updateState(world.getBestNeighborSignal(pos));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof VSCBlockEntity be)
        {
            be.updateState(world.getBestNeighborSignal(pos));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, MODE);
    }

    @Override
    public PortMap getPorts(Level world, BlockPos pos, BlockState state, @Nullable Direction side)
    {
        if (world.getBlockEntity(pos) instanceof VSCBlockEntity be)
        {
            return be.getConfig().getPorts();
        }
        return NO_PORTS;
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof VSCBlockEntity be)
        {
            be.invalidatePortCache();
        }
    }
}
