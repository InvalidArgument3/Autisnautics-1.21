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

package com.neep.neepbus.block;

import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.block.entity.SidedConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.PortMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * To be implemented by blocks.
 * Not using BlockApiLookup so that it is possible to check for a NeepBusProvider without calling World::getBlockEntity.
 */
public interface NeepBusProvider
{
    PortMap NO_PORTS = PortMap.EMPTY;

//    BlockApiLookup<NeepBusMember, Direction> LOOO = BlockApiLookup.get(
//            new Identifier(NeepMeat.NAMESPACE, "neepbus_member"), NeepBusMember.class, Direction.class);

    default PortMap getPorts(Level world, BlockPos pos, BlockState state, @Nullable Direction side)
    {
        NeepBusConfig config = getConfig(world, pos, state, side);
        if (config != null)
            return config.getPorts();

        return PortMap.EMPTY;
    }

    @Nullable
    default NeepBusConfig getConfig(Level world, BlockPos pos, BlockState state, @Nullable Direction side)
    {
        @Nullable BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ConfigProvider provider)
        {
            return provider.getConfig();
        }
        else if (be instanceof SidedConfigProvider provider)
        {
            return provider.getConfig(side);
        }
        return null;
    }

    void networkChanged(Level world, BlockPos pos, BlockPos whereChanged);
}
