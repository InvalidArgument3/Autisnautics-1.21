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

package com.neep.neepmeat.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.block.redstone.BigLeverBlock;
import com.neep.neepmeat.init.NMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BigLeverBlockEntity extends SyncableBlockEntity
{
    // Client only
    public float leverDelta = 0;

    // Server
    public int activeTicks = 40;
    public int tickCounter = 0;
    public boolean powered;

    public BigLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public BigLeverBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.BIG_LEVER, pos, state);
    }

    public boolean isCounting()
    {
        return activeTicks > 0;
    }

    protected void count()
    {
        if (isCounting())
        {
            if (tickCounter > 0)
            {
                --tickCounter;
                this.sync();
            }
            else
            {
                setPower(false);
            }
        }
    }

    public void togglePower()
    {
        setPower(!powered);
    }

    public void setPower(boolean pow)
    {
//        System.out.println(tickCounter);
        if (pow == powered)
            return;

        // Set world blockstate
        ((BigLeverBlock) getBlockState().getBlock()).setPowered(getLevel(), getBlockPos(), pow);

        // Start counter if necessary
        if (pow)
        {
            powered = true;
            if (isCounting())
                tickCounter = activeTicks;
        }
        else
            powered = false;
    }

    public static <E extends BlockEntity> void serverTick(Level world, BlockPos blockPos, BlockState blockState, BigLeverBlockEntity be)
    {
        be.count();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("active_ticks", activeTicks);
        nbt.putInt("counter", tickCounter);
        nbt.putBoolean("powered", powered);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.activeTicks = nbt.getInt("active_ticks");
        this.tickCounter = nbt.getInt("counter");
        this.powered = nbt.getBoolean("powered");
    }
}
