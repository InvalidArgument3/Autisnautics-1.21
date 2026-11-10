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

package com.neep.neepmeat.machine.large_motor;

import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LargeMotorStructureEntity extends BigBlockStructureEntity
{
    private final AcceptorWrapper acceptorWrapper;
    @Nullable private BlockApiCache<Void, Void> controllerCache;

    public LargeMotorStructureEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.acceptorWrapper = new AcceptorWrapper();
    }

    public BloodAcceptor getBloodAcceptor(Direction direction)
    {
        return acceptorWrapper;
    }

    @Nullable
    private LargeMotorBlockEntity getController()
    {
        if (controllerCache == null)
        {
            if (controllerPos != null && level instanceof ServerLevel serverWorld)
            {
                controllerCache = BlockApiCache.create(MeatLib.VOID_LOOKUP, serverWorld, controllerPos);
            }
            else
            {
                return null;
            }
        }

        if (controllerCache.getBlockEntity() instanceof LargeMotorBlockEntity be)
        {
            return be;
        }

        return null;
    }

    private class AcceptorWrapper implements BloodAcceptor
    {
        public Mode getMode()
        {
            return Mode.SINK;
        }

        @Override
        public float updateInflux(float influx)
        {
            var controller = getController();
            if (getController() != null)
            {
                return controller.getAcceptor(null).updateInflux(influx);
            }
            return 0;
        }
    }
}
