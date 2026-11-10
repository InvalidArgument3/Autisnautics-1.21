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

package com.neep.neepmeat.machine;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Heatable
{
    void setBurning();

    default void updateState(Level world, BlockPos pos, BlockState oldState) {};

    void setHeat(float heat);

    static int getFurnaceTickIncrement(float heat)
    {
        return (int) Mth.clamp(heat / (150f / 1000), 1, 128);
    }

    static float heatForTickIncrement(int tickIncrement)
    {
        return tickIncrement * (150f / 1000);
    }

    BlockApiLookup<Heatable, Void> LOOKUP = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "heatable"),
            Heatable.class, Void.class
    );
}
