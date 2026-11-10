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
import com.neep.neepmeat.init.NMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class LimbEnlightenmentEvent implements EnlightenmentEvent
{
    private final Level world;
    private final ServerPlayer serverPlayer;
    private final RandomSource random;

    public LimbEnlightenmentEvent(Level world, ServerPlayer serverPlayer)
    {
        this.world = world;
        this.random = RandomSource.create();
        this.serverPlayer = serverPlayer;
    }

    @Override
    public void tick()
    {
        BlockPos playerPos = serverPlayer.blockPosition();
        int attempt = 0;
        while (attempt < 10)
        {
            attempt++;

            double d = playerPos.getX() + (this.random.nextDouble() - 0.5) * 64.0;
            double e = playerPos.getY() + (double)(this.random.nextInt(64) - 32);
            double f = playerPos.getZ() + (this.random.nextDouble() - 0.5) * 64.0;

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(d, e, f);
            while (mutable.getY() > this.world.getMinBuildHeight() && this.world.getBlockState(mutable).isSolid())
            {
                mutable.move(Direction.DOWN);
            }

            if (world.isOutsideBuildHeight(mutable.getY()) || !mutable.closerThan(playerPos, 20) || !world.noCollision(new AABB(mutable)))
            {
                continue;
            }

            int spawnTries = random.nextInt(5);
            for (int i = 0; i < spawnTries; ++i)
            {
                var limb = NMEntities.LIMB.create(world);
                world.addFreshEntity(limb);
                limb.setPosRaw(mutable.getX() + random.nextDouble(), mutable.getY() + 0.1, mutable.getZ() + random.nextDouble());
            }

            break;
        }
    }

    @Override
    public boolean isRemoved()
    {
        return true;
    }
}
