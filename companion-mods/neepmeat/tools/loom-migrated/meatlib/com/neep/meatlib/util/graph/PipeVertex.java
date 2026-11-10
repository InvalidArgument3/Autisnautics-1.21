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

package com.neep.meatlib.util.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface PipeVertex
{
    boolean canSimplify();

    // TODO: CRTP for Vertex?
    void setAdjVertex(int id, PipeVertex first);

    interface Lookup<T extends PipeVertex>
    {
        T find(ServerLevel world, BlockPos pos);
    }
}
