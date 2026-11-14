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

package com.neep.neepmeat.block.fluid;

import com.neep.neepmeat.api.ContaminationUtil;
import com.neep.neepmeat.api.block.PressureWashable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class SludgeFluidBlock extends PersistentFluidBlock implements PressureWashable
{
    public SludgeFluidBlock(FlowingFluid fluid, Properties settings)
    {
        super(fluid, settings.randomTicks());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        int level = state.getFluidState().getType().getAmount(state.getFluidState());

        if (random.nextFloat() < 0.5 && level > 0)
        {
            level--;

            BlockState newState = Blocks.AIR.defaultBlockState();

            if (level > 0)
                newState = fluid.getFlowing(level, false).createLegacyBlock();

            world.setBlockAndUpdate(pos, newState);

            ContaminationUtil.contaminate(world, pos.below(), null, 3, 1);
        }
    }

    @Override
    public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
    {
        var fluidState = state.getFluidState();
        int level = fluidState.getAmount();
        if (power >= 1 && level > 0)
        {
            level--;

            BlockState newState = Blocks.AIR.defaultBlockState();
            if (level > 0)
            {
                newState = ((FlowingFluid) fluidState.getType()).getFlowing(level, false).createLegacyBlock();
            }

            world.setBlockAndUpdate(pos, newState);
            if (world instanceof ServerLevel serverWorld)
            {
                Vec3 origin = Vec3.atCenterOf(pos);
                var particleVelocity = velocity.scale(0.3);
                com.neep.neepmeat.network.ParticleSpawnS2C.sendNearby(serverWorld, pos,
                        new BlockParticleOption(com.neep.neepmeat.init.NMParticles.FLUID_BITS, state),
                        origin, particleVelocity, new Vec3(0.5, 0.5, 0.5), 7);
            }
        }
        return 1;
    }

    @Override
    public int tint()
    {
        return 0xec6040;
    }
}
