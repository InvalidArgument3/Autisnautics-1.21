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

package com.neep.neepmeat.machine.grinder;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.block.entity.MotorisedMachineBlockEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.recipe.CrushingRecipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.CombinedStorage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class CrusherBlockEntity extends MotorisedMachineBlockEntity
{
    public static final float MIN_POWER = 0.02f;
    public static final float MAX_POWER = 0.5f;

    protected Random jrandom = new Random();
    protected GrinderStorage storage = new GrinderStorage(this);
    protected int cooldownTicks = 2;
    protected int processLength;

    protected float progress;

    // TODO: make not stupid
    protected ResourceLocation currentRecipeId;
    protected CrushingRecipe currentRecipe;

    // Rendering
    public double sinAngle = 0;

    public CrusherBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, MIN_POWER, MAX_POWER);
    }

    public CrusherBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.CRUSHER, pos, state);
    }

    public CrushingRecipe getCurrentRecipe()
    {
        return currentRecipe;
    }

    public void setCurrentRecipe(@Nullable CrushingRecipe recipe, @Nullable ResourceLocation id)
    {
        this.currentRecipe = recipe;
        this.currentRecipeId = id;
    }

    public void readCurrentRecipe()
    {
        if (level != null)
        {
            Optional<? extends MeatlibRecipe<?>> optional = MeatlibRecipes.getInstance().get(currentRecipeId);
            optional.ifPresentOrElse(recipe -> this.currentRecipe = (CrushingRecipe) recipe,
                    () -> this.currentRecipe = null);
        }
    }

    public GrinderStorage getStorage()
    {
        return storage;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
        nbt.putFloat("power", power);
        nbt.putFloat("progress", progress);
        nbt.putInt("process_length", processLength);
        nbt.putFloat("progress_increment", progressIncrement);

        if (currentRecipeId != null)
            nbt.putString("current_recipe", currentRecipeId.toString());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
        this.power = nbt.getFloat("power");
        this.progress = nbt.getFloat("progress");
        this.processLength = nbt.getInt("process_length");
        this.currentRecipeId = ResourceLocation.parse(nbt.getString("current_recipe"));
        this.progressIncrement = nbt.getFloat("progress_increment");
        readCurrentRecipe();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    public void tick()
    {
        readCurrentRecipe();

        // Eject outputs
        if (!storage.outputStorage.isEmpty() || !storage.extraStorage.isEmpty())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ejectOutput(transaction);
                transaction.commit();
            }
        }

        if (currentRecipe != null)
        {
            progress = Math.min(processLength, progress + progressIncrement);

            if (progress >= this.processLength || !getCurrentRecipe().matches(storage))
            {
                endRecipe();
                this.progress = 0;
            }
        }
        else
        {
            ++progress;

            if (progress >= this.cooldownTicks)
            {
                startRecipe();
                this.progress = 0;
            }
        }

        super.serverTick();
    }

    private void startRecipe()
    {
        if (currentRecipe == null && storage.outputStorage.isEmpty() && !storage.inputStorage.isEmpty())
        {
            net.minecraft.world.item.crafting.RecipeHolder<?> holder = MeatlibRecipes.getInstance().getFirstHolder(NMrecipeTypes.GRINDING, storage).orElse(null);
            CrushingRecipe recipe = holder == null ? null : (CrushingRecipe) holder.value();

            try (Transaction transaction = Transaction.openOuter())
            {
                if (recipe != null && recipe.ejectOutputs(storage, transaction))
                {
                    setCurrentRecipe(recipe, holder.id());
                    this.processLength = recipe.getTime();
                    sync();
                }
                transaction.abort();
            }
        }
    }

    private void endRecipe()
    {
        if (currentRecipe != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                if (getCurrentRecipe().matches(storage)
                        && getCurrentRecipe().ejectOutputs(storage, transaction)
                        && getCurrentRecipe().takeInputs(storage, transaction))
                {
                    ejectOutput(transaction);
                    transaction.commit();
                }
                else transaction.abort();
            }
            this.setCurrentRecipe(null, null);
        }
        sync();
    }

    protected void ejectOutput(TransactionContext transaction)
    {
        Direction facing = getBlockState().getValue(CrusherBlock.FACING);
        CombinedStorage<ItemVariant, WritableStackStorage> combined = new CombinedStorage<>(List.of(storage.outputStorage, storage.extraStorage));
        ItemPipeUtil.storageToAny((ServerLevel) getLevel(), combined, worldPosition, facing, transaction);
    }

    protected void ejectXP()
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            Direction facing = getBlockState().getValue(CrusherBlock.FACING);
            Vec3 xpPos = Vec3.upFromBottomCenterOf(worldPosition, 0.5).add(facing.getStepX() * 0.6, facing.getStepY() * 0.6, facing.getStepZ() * 0.6);
            ExperienceOrb.award((ServerLevel) level, xpPos, (int) Math.ceil(storage.getXpStorage().getAmount()));
            storage.xpStorage.extract(Float.MAX_VALUE, transaction);
            transaction.commit();
        }
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        tick();
        return currentRecipe != null;
    }

    @Override
    public float getLoadTorque()
    {
        return 400f;
    }

    public void clientTick()
    {
        float intensity = progressIncrement / maxIncrement;

        // Particles will be more frequent at higher power. Clamp above 1 to prevent / 0.
        int tickInterval = (int) Mth.clamp(1, 1 / (intensity * 2), 100);

        if ((level.getGameTime() % tickInterval) == 0
                && currentRecipe != null
                && !storage.inputStorage.isEmpty()
                && progressIncrement > 0)
        {
            double px = getBlockPos().getX() + 0.5 + (jrandom.nextFloat() - 0.5) * 0.5;
            double py = getBlockPos().getY() + 0.8 + (jrandom.nextFloat() - 0.5) * 0.5;
            double pz = getBlockPos().getZ() + 0.5 + (jrandom.nextFloat() - 0.5) * 0.5;

            double vx = (jrandom.nextFloat() - 0.5) * 0.2;
            double vy = jrandom.nextFloat() * Math.max(0.3, 0.5 * intensity);
            double vz = (jrandom.nextFloat() - 0.5) * 0.2;

            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, storage.inputStorage.getAsStack()),
                px, py, pz, vx, vy, vz);
        }
    }
}
