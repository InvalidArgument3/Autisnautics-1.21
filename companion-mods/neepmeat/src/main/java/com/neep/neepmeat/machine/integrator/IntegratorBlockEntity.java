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
 *
 * A full bucket of blood starts growth. Once mature, stored data rises by one each tick, up to eight gieb.
 * GeckoLib hatching animation is not ported.
 */

package com.neep.neepmeat.machine.integrator;

import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.data.DataUtil;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.machine.StageMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class IntegratorBlockEntity extends BlockEntity implements Integrator {
    public static final long MAX_DATA = 8 * DataUtil.GIEB;
    public static final int BLOOD_CAPACITY = FluidType.BUCKET_VOLUME;

    private int growthTimeRemaining = 1000;
    private boolean mature;
    private long data;
    private int blood;
    private BlockPos lookTarget = BlockPos.ZERO;

    public IntegratorBlockEntity(BlockPos pos, BlockState state) {
        super(StageMachines.INTEGRATOR_BE.get(), pos, state);
    }

    public void serverTick() {
        if (canGrow()) {
            grow();
        }
        if (mature) {
            data = Math.min(MAX_DATA, data + 1);
            setChanged();
        }
    }

    public boolean canGrow() {
        return !mature && blood >= BLOOD_CAPACITY;
    }

    public void grow() {
        if (blood >= BLOOD_CAPACITY) {
            growthTimeRemaining--;
        }
        if (growthTimeRemaining <= 0) {
            mature = true;
        }
    }

    public int insertBlood(Fluid fluid, int amount) {
        if (mature || amount <= 0 || fluid != BloodFluids.BLOOD.get()) {
            return 0;
        }
        int accepted = Math.min(amount, BLOOD_CAPACITY - blood);
        blood += accepted;
        setChanged();
        return accepted;
    }

    public int blood() {
        return blood;
    }

    public boolean mature() {
        return mature;
    }

    public void setGrowthTimeRemaining(int remaining) {
        this.growthTimeRemaining = remaining;
    }

    public void setData(long data) {
        this.data = data;
        setChanged();
    }

    @Override
    public boolean canEnlighten() {
        return mature;
    }

    @Override
    public void setLookPos(BlockPos pos) {
        this.lookTarget = pos;
        setChanged();
    }

    public BlockPos lookTarget() {
        return lookTarget;
    }

    @Override
    public void spawnBeam(net.minecraft.world.level.Level world, BlockPos pos) {
        setLookPos(pos);
    }

    @Override
    public long getData(DataResource variant) {
        return data;
    }

    @Override
    public float extract(DataResource variant, long amount, TransactionContext transaction) {
        long extracted = Math.min(amount, data);
        if (extracted > 0) {
            data -= extracted;
            setChanged();
            return extracted;
        }
        return 0;
    }

    public FluidStack bloodStack() {
        return blood <= 0 ? FluidStack.EMPTY : new FluidStack(BloodFluids.BLOOD.get(), blood);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("growth_remaining", growthTimeRemaining);
        tag.putBoolean("fully_grown", mature);
        tag.putLong("data", data);
        tag.putInt("blood", blood);
        tag.putLong("look_x", lookTarget.getX());
        tag.putLong("look_y", lookTarget.getY());
        tag.putLong("look_z", lookTarget.getZ());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        growthTimeRemaining = tag.getInt("growth_remaining");
        mature = tag.getBoolean("fully_grown");
        data = tag.getLong("data");
        blood = tag.getInt("blood");
        lookTarget = new BlockPos((int) tag.getLong("look_x"), (int) tag.getLong("look_y"), (int) tag.getLong("look_z"));
    }
}
