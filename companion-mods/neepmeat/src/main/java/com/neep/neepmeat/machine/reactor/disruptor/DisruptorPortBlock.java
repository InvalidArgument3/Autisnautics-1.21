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

package com.neep.neepmeat.machine.reactor.disruptor;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.machine.live_machine.block.PortBlock;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DisruptorPortBlock extends PortBlock<DisruptorPortBlockEntity> implements DataCable, NeepBusProvider
{
    public DisruptorPortBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings)
    {
        super(ctx, itemSettings, () -> IntrusionReactor.DISRUPTOR_PORT_BE, settings);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }
}
