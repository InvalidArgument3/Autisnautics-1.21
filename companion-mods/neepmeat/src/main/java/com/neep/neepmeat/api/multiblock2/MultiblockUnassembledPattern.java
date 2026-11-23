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

package com.neep.neepmeat.api.multiblock2;

import com.neep.neepmeat.api.big_block.BigBlockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MultiblockUnassembledPattern extends BigBlockPattern
{
    public boolean test(Level world, BlockPos controllerPos)
    {
        BlockPos.MutableBlockPos mutable = controllerPos.mutable();
        for (var entry : entries())
        {
            mutable.setWithOffset(controllerPos, entry.key());

            // Ignore the controller
            if (mutable.equals(controllerPos))
                continue;

            if (!world.getBlockState(mutable).equals(entry.value()))
                return false;
        }
        return true;
    }

    public MultiblockUnassembledPattern rotateY(float degrees)
    {
        MultiblockUnassembledPattern newVolume = new MultiblockUnassembledPattern();

        double s = Math.round(Math.sin(Math.toRadians(degrees)));
        double c = Math.round(Math.cos(Math.toRadians(degrees)));

        stateProviderMap.forEach((offset, state) ->
        {
            int newX = (int) Math.round(offset.getX() * c - offset.getZ() * s);
            int newZ = (int) Math.round(offset.getX() * s + offset.getZ() * c);
            newVolume.set( newX, offset.getY(), newZ, state);
        });

        apiMap.forEach((offset, api) ->
        {
            int newX = (int) Math.round(offset.getX() * c - offset.getZ() * s);
            int newZ = (int) Math.round(offset.getX() * s + offset.getZ() * c);
            newVolume.enableApi(newX, offset.getY(), newZ, api);
        });

        return newVolume;
    }
}
