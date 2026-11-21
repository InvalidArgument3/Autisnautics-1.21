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

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkEnlightenmentComponentImpl implements ChunkEnlightenmentComponent, ServerTickingComponent
{
    private final ChunkAccess chunk;

    private int temporaryEnlightenment = 0;
    private int enlightenment = 0;

    public ChunkEnlightenmentComponentImpl(ChunkAccess chunk)
    {
        this.chunk = chunk;
    }

    @Override
    public int getEnlightenment()
    {
        return enlightenment;
    }

    @Override
    public int getTemporaryEnlightenment()
    {
        return temporaryEnlightenment;
    }

    @Override
    public void addEnlightenment(int v)
    {
        enlightenment += v;
    }

    @Override
    public void readFromNbt(CompoundTag tag)
    {
        this.enlightenment = tag.getInt("enlightenment");
        this.temporaryEnlightenment = tag.getInt("temporary_enlightenment");
    }

    @Override
    public void writeToNbt(CompoundTag tag)
    {
        tag.putInt("enlightenment", enlightenment);
        tag.putInt("temporary_enlightenment", temporaryEnlightenment);
    }

    @Override
    public void serverTick()
    {
        if (chunk instanceof LevelChunk worldChunk)
        {
            Level world = worldChunk.getLevel();

            if (world.getGameTime() % 80 == 0)
            {
                tickThings(world);
            }
        }
    }

    private void tickThings(Level world)
    {
        
    }
}
