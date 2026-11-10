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

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.*;
import com.neep.neepmeat.machine.reactor.ReceiverOrganismComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CoreSensorBlockEntity extends SyncableBlockEntity implements ReceiverOrganismComponent, ConfigProvider
{
    // Don't change the lambda to a method reference!
    private final MultiCachingSender sender = new MultiCachingSender(this::getLevel, getBlockPos(), s -> this.config.hasOutput(s));

    public final SimpleReadPort organisation = new SimpleReadPort(new SimpleEntry("Organisation"), sender::send);
    public final SimpleReadPort incidentZoneRadius = new SimpleReadPort(new SimpleEntry("Incident zone radius"), sender::send);
    public final SimpleReadPort exudateFlow = new SimpleReadPort(new SimpleEntry("Exudate mass flow"), sender::send);
    public final SimpleReadPort storedExudate = new SimpleReadPort(new SimpleEntry("Stored exudate"), sender::send);
    public final SimpleReadPort cudEfficiency = new SimpleReadPort(new SimpleEntry("Cud efficiency"), sender::send);

    private final NeepBusConfigImpl config = NeepBusConfig.builder(this::setChanged)
            .outputs(organisation, incidentZoneRadius, exudateFlow, storedExudate, cudEfficiency)
            .build();

    public CoreSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean isComponentRemoved()
    {
        return isRemoved();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.put("organisation", organisation.writeNbt(new CompoundTag()));
        nbt.put("radius", incidentZoneRadius.writeNbt(new CompoundTag()));
        nbt.put("exudate_flow", exudateFlow.writeNbt(new CompoundTag()));
        nbt.put("stored_exudate", storedExudate.writeNbt(new CompoundTag()));
        nbt.put("efficiency", cudEfficiency.writeNbt(new CompoundTag()));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        organisation.readNbt(nbt.getCompound("organisation"));
        incidentZoneRadius.readNbt(nbt.getCompound("radius"));
        exudateFlow.readNbt(nbt.getCompound("exudate_flow"));
        storedExudate.readNbt(nbt.getCompound("stored_exudate"));
        cudEfficiency.readNbt(nbt.getCompound("efficiency"));
    }

    public void networkChanged()
    {
        sender.invalidate();
    }
}
