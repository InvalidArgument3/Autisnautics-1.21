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

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.block.fluid.SludgeFluidBlock;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

public class BuiltSludgeFluid extends BuiltPersistentFluid
{
    public BuiltSludgeFluid(String namespace, String baseName, int levelDecrease, int tickRate, boolean infinite, boolean makeBlock, boolean makeItem, SoundEvent fillSoun, CreativeModeTab itemGroup)
    {
        super(namespace, baseName, levelDecrease, tickRate, infinite, makeBlock, makeItem, fillSoun, itemGroup);
    }

    @Override
    protected Still makeStill()
    {
        return super.makeStill();
    }

    @Override
    protected Flowing makeFlowing()
    {
        return super.makeFlowing();
    }

    @Override
    protected LiquidBlock makeBlock(Fluid fluid, BlockBehaviour.Properties settings)
    {
        return new SludgeFluidBlock(flowing, settings);
    }
}
