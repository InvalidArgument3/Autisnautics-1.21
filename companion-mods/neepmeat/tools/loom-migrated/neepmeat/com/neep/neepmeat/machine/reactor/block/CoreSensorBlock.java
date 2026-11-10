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

package com.neep.neepmeat.machine.reactor.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.util.PortMap;
import com.neep.neepmeat.machine.reactor.ReceiverOrganismComponent;
import com.neep.neepmeat.machine.reactor.ReceiverOrganismComponentProvider;
import com.neep.neepmeat.machine.reactor.block.entity.CoreSensorBlockEntity;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CoreSensorBlock extends BaseBlock implements EntityBlock, ReceiverOrganismComponentProvider, NeepBusProvider, DataCable
{
    public CoreSensorBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return IntrusionReactor.CORE_SENSOR_BE.create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof CoreSensorBlockEntity be)
        {
            player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            return InteractionResult.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public PortMap getPorts(Level world, BlockPos pos, BlockState state, @Nullable Direction side)
    {
        if (world.getBlockEntity(pos) instanceof CoreSensorBlockEntity be)
        {
            return be.getConfig().getPorts();
        }
        return NO_PORTS;
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof CoreSensorBlockEntity be)
        {
            be.networkChanged();
        }
    }

    @Override
    public ReceiverOrganismComponent get(Level world, BlockPos pos, BlockState state)
    {
        return (ReceiverOrganismComponent) world.getBlockEntity(pos);
    }
}
