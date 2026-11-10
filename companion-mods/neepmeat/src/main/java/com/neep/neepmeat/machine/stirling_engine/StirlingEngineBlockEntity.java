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

package com.neep.neepmeat.machine.stirling_engine;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.screen_handler.StirlingEngineScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class StirlingEngineBlockEntity extends SyncableBlockEntity implements MenuProvider, MotorEntity
{
    private MotorisedBlock prevMotorised = null;
    protected StirlingEngineStorage storage;

    public static final int ENERGY_CAPACITY = 40000;

    public float angle;

    protected float energyStored;
    protected long outputPower;

    protected int burnTime;
    protected int fuelTime;

    protected LazyBlockApiCache<MotorisedBlock, Void> cache;

    protected final ContainerData propertyDelegate = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            return switch (index)
                {
                    case 0 -> burnTime;
                    case 1 -> fuelTime;
                    case 2 -> (int) energyStored;
                    case 3 -> (int) outputPower;
                    default -> 0;
                };
        }

        @Override
        public void set(int index, int value)
        {
//            switch (index)
//            {
//                case 0 -> burnTime = value;
//                case 1 -> fuelTime = value;
//                case 2 -> energyStored = value;
//                case 3 -> outputPower = value;
//            }
        }

        @Override
        public int getCount()
        {
            return 4;
        }
    };

    public StirlingEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new StirlingEngineStorage(this);
        updateCache();
    }

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.STIRLING_ENGINE, pos, state);
    }

    @Override
    public void setBlockState(BlockState state)
    {
        super.setBlockState(state);
        if (prevMotorised != null)
            prevMotorised.setInputPower(0);
        updateCache();
    }

    private void updateCache()
    {
        Direction facing = getBlockState().getValue(StirlingEngineBlock.FACING);
        cache = LazyBlockApiCache.of(MotorisedBlock.LOOKUP, worldPosition.relative(facing), this::getLevel, () -> null);
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
        nbt.putInt("burn_time", burnTime);
        nbt.putInt("fuel_time", fuelTime);
        nbt.putFloat("energy", energyStored);
        nbt.putLong("output_power", outputPower);
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
        this.burnTime = nbt.getInt("burn_time");
        this.fuelTime = nbt.getInt("fuel_time");
        this.energyStored = nbt.getFloat("energy");
        this.outputPower = nbt.getLong("output_power");
    }

    public void serverTick()
    {
        this.burnTime = Math.max(0, burnTime - 1);

        float newThingyStored = energyStored;
        if (isBurning())
        {
            newThingyStored = Math.min(ENERGY_CAPACITY, energyStored + 20);
        }

        if (burnTime == 0)
        {
            int time = storage.decrementFuel();
            if (time > 0)
            {
                this.burnTime = time;
                this.fuelTime = time;
            }

            updateBurning();
        }

        // Output depends on stored energy and is limited to 80eJ/t
        long prevOutputPower = outputPower;
//        outputPower = (long) MathHelper.clamp((energyStored / (float) ENERGY_CAPACITY) * 80f, 0, energyStored);
        outputPower = isBurning() ? 150 : 0;

        MotorisedBlock motorised = cache.find();
        if (motorised != null)
        {
            if (prevOutputPower != outputPower || prevMotorised != motorised)
                motorised.setInputPower((float) getMechPUPower());

            newThingyStored = Math.max(0, newThingyStored - outputPower);
            prevMotorised = motorised;
            motorised.motorTick(this);
        }

        if (newThingyStored != energyStored)
        {
            energyStored = newThingyStored;
            sync();
        }
    }

    public boolean isBurning()
    {
        return burnTime > 0;
    }

    protected void updateBurning()
    {
        BlockState state = getBlockState();
        state = state.setValue(AbstractFurnaceBlock.LIT, this.isBurning());
        getLevel().setBlock(worldPosition, state, Block.UPDATE_ALL);
    }

    public StirlingEngineStorage getStorage()
    {
        return storage;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container." + NeepMeat.NAMESPACE + ".stirling_engine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new StirlingEngineScreenHandler(syncId, inv, storage.inventory, propertyDelegate);
    }

    public static float energyToSpeed(float energy)
    {
        // E=Iw^2. Assume the flywheel has a moment of inertia of 40kgm^2, divide by 20 to get velocity in rad / tick.
        float w1 = (float) Math.sqrt(2f * energy / 40) / 20;
        return (float) (w1 * 180 / Math.PI); // Convert to degrees / tick
    }

    @Override
    public float getRotorAngle()
    {
        return angle;
    }

    @Override
    public float getSpeed()
    {
//        return energyToSpeed(energyStored);
        // Ah, forget it
        return outputPower / 10f;
    }

    @Override
    public double getMechPUPower()
    {
        return outputPower / (double) PowerUtil.BASE_POWER;
    }

    @Override
    public MotorisedBlock getConnectedBlock()
    {
        return cache != null ? cache.find() : null;
    }
}
