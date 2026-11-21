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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface ChunkEnlightenmentComponent extends Component
{
    int getEnlightenment();
    int getTemporaryEnlightenment();
    void addEnlightenment(int v);

    static void distribute(Level world, BlockPos pos, double amount, int r)
    {
        int chunks = (2 * r + 1) * (2 * r + 1);

        int cx = pos.getX();
        int cz = pos.getZ();

        float denominator = 0;

        List<FloatObjectPair<ChunkAccess>> result = new ObjectArrayList<>();

        ChunkPos origin = new ChunkPos(pos);
        for (int i = -r; i <= r; ++i)
        {
            for (int j = -r; j <= r; ++j)
            {
                ChunkAccess chunk = world.getChunk(origin.x + i, origin.z + j);

                float distance = distance(cx, cz, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ());

                denominator += distance;

                result.add(FloatObjectPair.of(distance, chunk));
            }
        }

        float amountDistributed = 0;
        for (var pair : result)
        {
            ChunkEnlightenmentComponent component = pair.value().getComponent(NMComponents.CHUNK_ENLIGHTENMENT);

            float distance = pair.keyFloat();
            float chunkAmount = (float) ((amount * distance) / denominator);
            amountDistributed += chunkAmount;
            component.addEnlightenment((int) chunkAmount);
        }

//        NeepMeat.LOGGER.info("amount: {}, distributed: {}", amount, amountDistributed);
    }

    static float distance(int x1, int z1, int x2, int z2)
    {
        float p1 = (x1 - x2);
        float p2 = (z1 - z2);
        return Mth.sqrt(p1 * p1 + p2 * p2);
    }
}
