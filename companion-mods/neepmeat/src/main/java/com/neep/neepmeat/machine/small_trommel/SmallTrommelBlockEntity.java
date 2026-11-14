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

package com.neep.neepmeat.machine.small_trommel;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.client.screen.tablet.graph.GraphRegistry;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.api.AutoEjector;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class SmallTrommelBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    public static final int PU_PROCESS_ENERGY = 30;

    public static final float MIN_POWER = 0.02f;
    public static final float MAX_INCREMENT = 0.5f;

    public SmallTrommelStorage storage;
    public FluidVariant currentFluid;

    protected int totalProgress;
    protected float progress;
    public float renderProgress;
    private float progressIncrement;
    protected Random random;
    private float power = 0;

    private final AutoEjector<FluidVariant> ejector;

    public SmallTrommelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new SmallTrommelStorage(this);
        this.random = new Random(pos.asLong());
        Direction facing = state.getValue(SmallTrommelBlock.FACING);
        this.ejector = AutoEjector.simpleFluid(() -> Direction.DOWN, this::getLevel, pos.relative(facing), storage.fluidOutput, SmallTrommelBlock.Structure.PUMP);
    }

    public SmallTrommelBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.SMALL_TROMMEL, pos, state);
    }

//    public Storage<FluidVariant> getInputStorage(Direction direction)
//    {
//        return direction == Direction.UP ? storage.fluidInput : null;
//    }

    public Storage<FluidVariant> getOutputFluidStorage()
    {
        return storage.fluidOutput;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);

        storage.writeNbt(nbt);

        nbt.putFloat("progress", progress);
        nbt.putFloat("progressIncrement", progressIncrement);
        nbt.putInt("totalProgress", totalProgress);

        if (currentFluid != null)
            nbt.put("currentFluid", currentFluid.toNbt());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);

        storage.readNbt(nbt);

        this.progress = nbt.getFloat("progress");
        this.progressIncrement = nbt.getFloat("progressIncrement");
        this.totalProgress = nbt.getInt("totalProgress");
        this.currentFluid = FluidVariant.fromNbt(nbt.getCompound("currentFluid"));
    }

    public void convert()
    {
        TrommelRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.TROMMEL, storage).orElse(null);
        if (recipe == null)
            recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.FAT_TROMMEL, storage).orElse(null);

        if (recipe != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                if (recipe.takeInputs(storage, transaction) && recipe.ejectOutputs(storage, transaction))
                    transaction.commit();
                else
                    transaction.abort();
            }
        }
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        Direction facing = getBlockState().getValue(SmallTrommelBlock.FACING);
        if (!storage.itemOutput.isEmpty())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemPipeUtil.storageToAny((ServerLevel) level, storage.itemOutput(), worldPosition.relative(facing), facing, transaction);
                transaction.commit();
            }
        }

        totalProgress = PU_PROCESS_ENERGY;

        SingleVariantStorage<FluidVariant> fluidInput = storage.input();

        currentFluid = !fluidInput.getResource().isBlank() ? fluidInput.getResource() : null;
        if (currentFluid != null && progressIncrement > 0)
        {
            progress = Math.min(totalProgress, progress + progressIncrement);
        }
        else
        {
            progress = 0;
        }

        if (progress >= totalProgress)
        {
            progress = 0;
            convert();
        }

        softSync();

        ejector.tick(null);

        return false;
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
        if (power >= MIN_POWER)
        {
//            progressIncrement = MathHelper.lerp(power, INCREMENT_MIN, 2);
            progressIncrement = Mth.clamp(power, 0, 1);
        }
        else
        {
            progressIncrement = 0;
        }
    }

    public Storage<ItemVariant> getOutputItemStorage()
    {
        return storage.itemOutput;
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(power < MIN_POWER, power, MIN_POWER);
    }

    public static class Structure extends BlockEntity
    {
        public Structure(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        public Structure(BlockPos pos, BlockState state)
        {
            super(NMBlockEntities.SMALL_TROMMEL_STRUCTURE, pos, state);
        }
    }

    public static GraphData makePowerGraph()
    {
        return GraphRegistry.arbitraryStandardPower(MIN_POWER, MAX_INCREMENT, 40e3F);
    }
}
