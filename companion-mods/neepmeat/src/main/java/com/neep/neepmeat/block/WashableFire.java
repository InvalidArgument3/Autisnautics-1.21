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

package com.neep.neepmeat.block;

import com.neep.neepmeat.api.block.PressureWashable;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WashableFire implements PressureWashable
{
    @Override
    public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
    {
        if (power >= 1 && FluidVariantAttributes.getTemperature(fluidVariant) < FluidConstants.LAVA_TEMPERATURE)
        {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (world instanceof ServerLevel serverWorld)
            {
                serverWorld.playSeededSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS,
                        (float) world.getRandom().triangle(0.4, 0.3),
                    (float) world.getRandom().triangle(1.4, 0.3),
                        0);
            }

            return 1;
        }
        return 0;
    }

    @Override
    public int tint()
    {
        return -1;
    }
}
