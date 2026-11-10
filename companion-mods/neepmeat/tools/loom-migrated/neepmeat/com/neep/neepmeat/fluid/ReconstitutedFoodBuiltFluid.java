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

import com.neep.meatlib.fluid.BaseFluid;
import com.neep.meatlib.fluid.BuiltFluid;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.nbt.CompoundTag;
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

public class ReconstitutedFoodBuiltFluid extends BuiltFluid<ReconstitutedFoodBuiltFluid.Still, ReconstitutedFoodBuiltFluid.Flowing>
{
    public ReconstitutedFoodBuiltFluid(String namespace, String baseName, int levelDecrease, int tickRate, boolean infinite, boolean makeBlock, boolean makeItem, SoundEvent fillSound, CreativeModeTab itemGroup)
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

    public abstract class MixableMain extends BaseFluid implements MixableFluid
    {
        @Override
        public FluidVariant mixNbt(FluidVariant thisVariant, long thisAmount, FluidVariant otherVariant, long otherAmount)
        {
            CompoundTag nbt = new CompoundTag();

            // Treat hunger and saturation as intrinsic properties.
            float hunger1 = MeatFluidUtil.getHunger(thisVariant);
            float hunger2 = MeatFluidUtil.getHunger(otherVariant);
            MeatFluidUtil.setHunger(nbt, ((hunger1 * thisAmount + hunger2 * otherAmount) / (float) (thisAmount + otherAmount)));

            float sat1 = MeatFluidUtil.getSaturation(thisVariant);
            float sat2 = MeatFluidUtil.getSaturation(otherVariant);
            MeatFluidUtil.setSaturation(nbt, ((sat1 * thisAmount + sat2 * otherAmount) / (float) (thisAmount + otherAmount)));

            return FluidVariant.of(this, nbt);
        }

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
        protected BlockState createLegacyBlock(FluidState fluidState)
        {
            return block.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(fluidState));
        }

        @Override
        protected boolean canConvertToSource(Level world)
        {
            return false;
        }
    }

    public class Flowing extends MixableMain
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

    public class Still extends MixableMain
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
