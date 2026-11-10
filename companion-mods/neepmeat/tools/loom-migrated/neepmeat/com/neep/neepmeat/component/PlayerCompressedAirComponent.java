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

package com.neep.neepmeat.component;

import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCompressedAirComponent implements PowerResourceComponent, PlayerComponent<PlayerCompressedAirComponent>, ServerTickingComponent, AutoSyncedComponent
{
    private final Player player;
    private long air;
    private final long capacity = 20;

    public PlayerCompressedAirComponent(Player player)
    {
        this.player = player;
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag)
    {
        this.air = tag.getLong("air");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag)
    {
        tag.putLong("air", air);
    }

    @Override
    public void serverTick()
    {
        long prevAir = air;
        air = Math.max(0, air - 1);
        if (prevAir != air)
            NMComponents.COMPRESSED_AIR.sync(player);
    }

    public void insertAir(int air)
    {
        this.air = Math.min(capacity, this.air + air);
        NMComponents.COMPRESSED_AIR.sync(player);
    }

    public long getAir()
    {
        return air;
    }

    @Override
    public boolean canRun()
    {
        return air > 0;
    }
}
