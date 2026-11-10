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

package com.neep.neepmeat.machine.item_mincer;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.client.screen.tablet.graph.GraphRegistry;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.api.AutoEjector;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class ItemMincerBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    public static final float MIN_POWER = 0.04f;
    public static final float MAX_INCREMENT = 0.8f;

    protected float power;
    protected float progressIncrement;
    protected State state = State.IDLE;
    protected float processWork;

    protected ItemMincerStorage storage;

    protected final AutoEjector<FluidVariant> ejector;

    protected final FluidPump pump = new FluidPump()
    {
        @Override
        public float getFlow()
        {
            return -0.5f;
        }

        @Override
        public AcceptorModes getMode()
        {
            return AcceptorModes.PUSH;
        }

        @Override
        public boolean isStorage()
        {
            return true;
        }
    };

    private double angle;

    public ItemMincerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new ItemMincerStorage(this);
        this.ejector = addListener(AutoEjector.simpleFluid(() -> state.getValue(ItemMincerBlock.FACING), this, storage.outputStorage, pump));
    }

    public ItemMincerBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.ITEM_MINCER, pos, state);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            switch (state)
            {
                case IDLE ->
                {
                    int energy = canStart();
                    if (energy != 0)
                    {
                        state = State.PROCESSING;
                        processWork = energy;
                        sync();
                    }
                }
                case PROCESSING ->
                {
                    processWork = Math.max(0, processWork - progressIncrement);
                    if (processWork <= 0)
                    {
                        produceOutput(transaction);
                        state = State.IDLE;
                    }
                }
            }
            transaction.commit();
        }

        ejector.tick(null);

        return true;
    }

    protected int canStart()
    {
        if (!storage.inputStorage.isEmpty())
        {
            FoodProperties food = storage.inputStorage.getResource().getObject().getFoodProperties();
            return food != null ? food.isFastFood() ? 8 : 16 : 0;
        }
        return 0;
    }

    protected void produceOutput(TransactionContext context)
    {
        if (canStart() > 0)
        {
            try (Transaction inner = context.openNested())
            {
                FoodProperties food = storage.inputStorage.getResource().getObject().getFoodProperties();
                FluidVariant outputVariant = MeatFluidUtil.getVariant(food);
                storage.outputStorage.insert(outputVariant, FluidConstants.INGOT, inner);
                storage.inputStorage.extract(storage.inputStorage.getResource(), 1, inner);
                inner.commit();
            }
        }
    }

    public void clientTick()
    {
        angle += 1.1;

        if (state == State.PROCESSING && !storage.inputStorage.isEmpty() && progressIncrement > 0)
        {
            double radius = 0.2;

            double cx = getBlockPos().getX() + 0.5;
            double cy = getBlockPos().getY() + 1;
            double cz = getBlockPos().getZ() + 0.5;

            double px = cx + Math.sin(angle) * radius;
            double pz = cz + Math.cos(angle) * radius;

            double vx = (px - cx) * 0.5;
            double vy = 0.35;
            double vz = (pz - cz) * 0.5;

            level.addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, storage.inputStorage.getAsStack()),
                    px, cy, pz,
                    vx, vy, vz
            );
        }
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
        if (power > MIN_POWER)
            this.progressIncrement = Mth.clamp(power, 0, MAX_INCREMENT);
        sync();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("power", progressIncrement);
        nbt.putInt("state", state.ordinal());
        nbt.putFloat("energy", processWork);
        storage.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.progressIncrement = nbt.getFloat("power");
        this.state = State.values()[nbt.getInt("state")];
        this.processWork = nbt.getFloat("energy");
        storage.readNbt(nbt);
    }

    public WritableStackStorage getInputStorage(Direction direction)
    {
        return storage.inputStorage;
    }

    public WritableSingleFluidStorage getOutputStorage(Direction direction)
    {
        Direction facing = getBlockState().getValue(ItemMincerBlock.FACING);
        return facing == direction ? storage.outputStorage : null;
    }

    public FluidPump getPump(Direction direction)
    {
        Direction facing = getBlockState().getValue(ItemMincerBlock.FACING);
        return facing == direction ? pump : null;
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(false, progressIncrement, MIN_POWER);
    }

    public enum State
    {
        IDLE,
        PROCESSING
    }

    public static GraphData makePowerGraph()
    {
        return GraphRegistry.arbitraryStandardPower(MIN_POWER, MAX_INCREMENT, 40e3f);
    }
}
