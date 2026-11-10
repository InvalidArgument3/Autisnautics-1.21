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

package com.neep.neepmeat.api.block;

import com.neep.neepmeat.block.WashableFire;
import com.neep.neepmeat.init.NMFluids;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface PressureWashable
{
    Map<Block, PressureWashable> FALLBACK = new IdentityHashMap<>();

    static void registerFallback(Block block, PressureWashable washable)
    {
        FALLBACK.put(block, washable);
    }

    @Nullable
    static PressureWashable get(BlockState state)
    {
        if (state.isAir())
            return null;

        Block block = state.getBlock();
        if (block instanceof PressureWashable washable)
            return washable;

        // Some jank for vanilla blocks
        // Can't use tags as wash handling and tint must be implemented
        var fallback = FALLBACK.get(block);

        if (fallback != null)
            return fallback;

        if (state.getBlock() instanceof WeatheringCopper oxidizable)
            return new Oxidisable(oxidizable);
        else if (state.getBlock() instanceof CropBlock cropBlock)
            return new Crop(cropBlock);

        return null;
    }

    static void initDefaults()
    {
        registerFallback(Blocks.FIRE, new WashableFire());
        registerFallback(Blocks.SCULK_VEIN, PressureWashable.SCULK_VEIN);
        registerFallback(Blocks.VINE, PressureWashable.VINE);
        registerFallback(Blocks.GLOW_LICHEN, PressureWashable.GLOW_LICHEN);
        registerFallback(Blocks.FROGSPAWN, PressureWashable.FROGSPAWN);
        registerFallback(Blocks.LILY_PAD, new PressureWashable.Simple(0x008800));
//        registerFallback(Blocks.GRASS_BLOCK, new ToState(Blocks.DIRT::getDefaultState,0x037d9b);)
//        registerFallback(Blocks.GRASS, new PressureWashable.Simple(0x008800));
//        registerFallback(Blocks.ROSE_BUSH, new PressureWashable.Simple(0x228800));
//        registerFallback(Blocks.POPPY, new PressureWashable.Simple(0x228800));
    }

    /// @return The power consumed by the washing
    int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant);

    /// Return -1 to ignore
    default int tint()
    {
        return -1;
    }

    record Simple(int tint) implements PressureWashable
    {
        @Override
        public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
        {
            if (power > 0)
            {
                world.destroyBlock(pos, true);
//                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return 1;
            }

            return 0;
        }

        @Override
        public int tint()
        {
            return tint;
        }
    }

    record ToState(Supplier<BlockState> stateSupplier, int tint) implements PressureWashable
    {
        @Override
        public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
        {
            if (power > 0)
            {
                world.setBlockAndUpdate(pos, stateSupplier.get());
                return 1;
            }
            return 0;
        }

        @Override
        public int tint()
        {
            return tint;
        }
    }

    record Oxidisable(WeatheringCopper oxidizable) implements PressureWashable
    {
        @Override
        public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
        {
            if (power > 0)
            {
                if (fluidVariant.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.PATINA_TREATMENT)))
                {
                    var result = oxidizable.getNext(state);
                    if (result.isPresent())
                    {
                        world.setBlockAndUpdate(pos, result.get());
                        return 1;
                    }
                }
                else if (FluidVariantAttributes.getTemperature(fluidVariant) >= FluidConstants.LAVA_TEMPERATURE)
                {
                    var result = WeatheringCopper.getPrevious(state);
                    if (result.isPresent())
                    {
                        world.setBlockAndUpdate(pos, result.get());
                        return 1;
                    }
                }
            }
            return 0;
        }
    }

    record Crop(CropBlock block) implements PressureWashable
    {
        @Override
        public int wash(Level world, BlockPos pos, BlockState state, Vec3 velocity, int power, FluidVariant fluidVariant)
        {
            if (power > 1 && fluidVariant.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.COMPRESSED_AIR)))
            {
                world.destroyBlock(pos, true);
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

    PressureWashable VINE = new Simple(0x00aa00);
    PressureWashable GLOW_LICHEN = new Simple(0x96bfae);
    PressureWashable FROGSPAWN = new Simple(0x97877f);
    PressureWashable SCULK_VEIN = new Simple(0x037d9b);
    PressureWashable GRASS_BLOCK = new ToState(Blocks.DIRT::defaultBlockState,0x037d9b);
}
