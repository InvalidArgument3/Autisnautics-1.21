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

import com.neep.meatlib.block.BaseLeavesBlock;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BloodBubbleLeavesBlock extends BaseLeavesBlock
{
    public BloodBubbleLeavesBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings
            .strength(0.2f)
            .randomTicks()
            .noOcclusion()
            .isValidSpawn((p1, p2, p3, p4) -> false)
            .isSuffocating((p1, p2, p3) -> false).isViewBlocking(((state, world, pos) -> false)));
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
    {
        if (random.nextInt(12) != 1)
            return;

        BlockPos blockPos = pos.below();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.canOcclude() && blockState.isFaceSturdy(world, blockPos, Direction.UP))
            return;

        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() - 0.05;
        double f = (double)pos.getZ() + random.nextDouble();
        world.addParticle(NMParticles.BLOOD_DRIP, d, e, f, 0.0, 0.0, 0.0);
    }

//    @Override
//    public boolean autoGenDrop()
//    {
//        return false;
//    }

    @Override
    public ItemLike dropsLike()
    {
        return null;
    }

    public static class FruitingBloodBubbleLeavesBlock extends BloodBubbleLeavesBlock
    {
        public FruitingBloodBubbleLeavesBlock(RegistrationContext ctx, Properties settings)
        {
            super(ctx, settings);
        }

        @Override
        public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
        {
            if (random.nextInt(5) != 1)
                return;

            BlockPos blockPos = pos.below();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.canOcclude() && blockState.isFaceSturdy(world, blockPos, Direction.UP))
                return;

            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() - 0.05;
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticle(NMParticles.BLOOD_DRIP, d, e, f, 0.0, 0.0, 0.0);
        }

    }
}
