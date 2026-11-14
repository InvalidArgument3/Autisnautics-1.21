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

package com.neep.neepmeat.enlightenment.event;

import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.datagen.tag.NMTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceWasteEnlightenmentEvent implements EnlightenmentEvent
{
    private final ServerPlayer player;
    private final BlockState state;

    public PlaceWasteEnlightenmentEvent(Level world, ServerPlayer player, BlockState state)
    {
        this.player = player;
        this.state = state;
    }

    public static boolean canSpawn(Level world, ServerPlayer player)
    {
        return true;
    }

    @Override
    public void spawn()
    {
        Level world = player.level();
        for (var pos : BlockPos.randomInCube(RandomSource.create(), 2, player.blockPosition(), 4))
        {
            if (world.getBlockState(pos).is(NMTags.CONTAMINATED_RUBBLE_REPLACABLE))
            {
                world.setBlockAndUpdate(pos, state);
            }
        }
    }

    @Override
    public void tick() { }

    @Override
    public boolean isRemoved()
    {
        return true;
    }
}
