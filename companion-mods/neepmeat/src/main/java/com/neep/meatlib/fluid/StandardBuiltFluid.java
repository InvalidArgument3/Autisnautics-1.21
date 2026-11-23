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

package com.neep.meatlib.fluid;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class StandardBuiltFluid extends BuiltFluid<StandardBuiltFluid.Still, StandardBuiltFluid.Flowing>
{
    public StandardBuiltFluid(String namespace, String baseName,
                              int levelDecrease, int tickRate,
                              boolean infinite, boolean makeBlock, boolean makeItem, SoundEvent fillSound, CreativeModeTab itemGroup)
    {
        super(namespace, baseName, levelDecrease, tickRate, infinite, makeBlock, makeItem, fillSound, itemGroup);
    }

    @Override
    protected Still makeStill()
    {
        return new Still();
    }

    @Override
    protected Flowing makeFlowing()
    {
        return new Flowing();
    }

    protected abstract class Main extends BaseFluid
    {
        @Override
        protected int getDropOff(LevelReader worldView)
        {
            return levelDecrease;
        }

        @Override
        public int getTickDelay(LevelReader worldView)
        {
            return tickRate;
        }

        @Override
        public Fluid getSource()
        {
            return still;
        }

        @Override
        public Fluid getFlowing()
        {
            return flowing;
        }

        @Override
        public Item getBucket()
        {
            return bucketItem;
        }

        @Override
        protected boolean canConvertToSource(Level world)
        {
            return infinite;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState fluidState)
        {
            return block.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(fluidState));
        }

        @Override
        public Optional<SoundEvent> getPickupSound()
        {
            return Optional.ofNullable(fillSound);
        }
    }

    public class Flowing extends Main
    {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState fluidState)
        {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState)
        {
            return false;
        }
    }

    public class Still extends Main
    {
        @Override
        public int getAmount(FluidState fluidState)
        {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState)
        {
            return true;
        }
    }
}
