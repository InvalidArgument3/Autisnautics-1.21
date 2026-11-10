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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Only to be used for MixinAbstractFurnaceBlockEntity.
public interface HeatableFurnace
{
    static Heatable of(AbstractFurnaceBlockEntity furnace)
    {
        return new Wrapper(furnace);
    }

    default void neepMeat$setBurning()
    {

    }

    default void neepMeat$updateState(Level world, BlockPos pos, BlockState oldState)
    {

    }

    default int neepMeat$getCookTimeTotal() { return 0; }

    default int neepMeat$getCookTime() { return 0; }

    default void neepMeat$setCookTime(int time) {}

    default void neepMeat$setHeat(float heat) {}

    default float neepMeat$getHeat() { return 0; }

    default boolean neepMeat$isCooking() { return false; }

    class Wrapper implements Heatable
    {
        private final HeatableFurnace furnace;

        protected Wrapper(HeatableFurnace furnace)
        {
            this.furnace = furnace;
        }

        @Override
        public void setBurning()
        {
            furnace.neepMeat$setBurning();
        }

        @Override
        public void updateState(Level world, BlockPos pos, BlockState oldState)
        {
            furnace.neepMeat$updateState(world, pos, oldState);
        }

        @Override
        public void setHeat(float heat)
        {
            furnace.neepMeat$setHeat(heat);
        }
    }
}
