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

package com.neep.neepmeat.machine.hydraulic_press;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.recipe.RecipeBehaviour;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.casting_basin.CastingBasinBlockEntity;
import com.neep.neepmeat.machine.casting_basin.CastingBasinStorage;
import com.neep.neepmeat.recipe.AbstractPressingRecipe;
import com.neep.neepmeat.recipe.MobSqueezingRecipe;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class HydraulicPressBlockEntity extends SyncableBlockEntity
{
    public static final long EXTEND_AMOUNT = FluidConstants.BUCKET;
    public static final int TICKS_EXTENDED = 30;
    
    protected boolean recipeControlled = true;
    protected short recipeState;
    protected int extensionTicks;

    @Nullable protected AbstractPressingRecipe<CastingBasinStorage> currentRecipe;
    protected SqueezingRecipeBehaviour behaviour = new SqueezingRecipeBehaviour();

    public float renderExtension;

    public WritableSingleFluidStorage fluidStorage = new WritableSingleFluidStorage(EXTEND_AMOUNT, this::sync)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return super.canInsert(variant) && variant.isOf(Fluids.WATER) && (!recipeControlled || (recipeState == 0 && hasRecipe()));
        }

        @Override
        protected boolean canExtract(FluidVariant variant)
        {
            return super.canExtract(variant) && variant.isOf(Fluids.WATER) && (!recipeControlled || recipeState == 2);
        }
    };

    public HydraulicPressBlockEntity(BlockEntityType<HydraulicPressBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public HydraulicPressBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.HYDRAULIC_PRESS, pos, state);
    }

    public void setState(int state)
    {
        this.recipeState = (short) state;
    }

    protected boolean hasRecipe()
    {
        return currentRecipe != null || behaviour.getCurrentRecipe() != null;
    }

    protected void startPressRecipe(CastingBasinStorage storage, AbstractPressingRecipe<CastingBasinStorage> recipe)
    {
        if (recipe == null || !storage.item(null).isEmpty())
            return;

        storage.lock();
        this.currentRecipe = recipe;
        this.recipeState = 0;
    }

    protected void finishPressRecipe(CastingBasinStorage storage)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            boolean success = currentRecipe.takeInputs(storage, transaction);
            if (success)
            {
                transaction.commit();
            }
            else
            {
                transaction.abort();
            }
        }
    }

    @Override
    protected void finishRecipe()
    {
        if (level.getBlockEntity(worldPosition.below()) instanceof CastingBasinBlockEntity basin)
        {
            finishPressRecipe(basin.getStorage());
        }
        else behaviour.finishRecipe();
    }

    protected void stopPressRecipe(@Nullable CastingBasinStorage storage)
    {
        if (storage != null) storage.unlock();

        this.recipeState = 0;
        this.currentRecipe = null;
    }

    protected void stopRecipe()
    {
        CastingBasinStorage storage = level.getBlockEntity(worldPosition.below()) instanceof CastingBasinBlockEntity be ? be.getStorage() : null;
        stopPressRecipe(storage);
        behaviour.interrupt();
    }

    // I'll clean this up one day. I promise!
    public void tick()
    {
        if (level.getBlockEntity(worldPosition.below()) instanceof CastingBasinBlockEntity basin)
        {
            if (currentRecipe == null)
            {
                AbstractPressingRecipe<CastingBasinStorage> recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.FAT_PRESSING, basin.getStorage()).orElse(null);

                if (recipe == null)
                    recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.PRESSING, basin.getStorage()).orElse(null);

                startPressRecipe(basin.getStorage(), recipe);

            }
        }
        else if (level.isEmptyBlock(worldPosition.below()) && behaviour.getCurrentRecipe() == null)
        {
            AABB box = AABB.of(new BoundingBox(worldPosition.below()));
            List<Entity> entities = level.getEntities(EntityTypeTest.forClass(Entity.class), box, e -> true);

            MobSqueezingRecipe squeezingRecipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.MOB_SQUEEZING, new MobSqueezeContext(level, worldPosition, entities)).orElse(null);
            behaviour.startRecipe(squeezingRecipe);
        }

//            || behaviour.getCurrentRecipe() != null && !behaviour.getCurrentRecipe().matches(new MobSqueezeContext(world, pos)))
        if (currentRecipe != null && !(level.getBlockEntity(worldPosition.below()) instanceof CastingBasinBlockEntity))
        {
            stopRecipe();
            setState(2);
        }

        tickExtension();
    }

    private void tickExtension()
    {
        if (currentRecipe == null && behaviour.getCurrentRecipe() == null)
            return;

        if (recipeState == 0 && fluidStorage.getAmount() >= EXTEND_AMOUNT)
        {
            this.extensionTicks = 0;
            this.recipeState = 1;
        }
        if (recipeState == 1)
        {
            extensionTicks = Math.min(TICKS_EXTENDED, extensionTicks + 1);
            if (extensionTicks >= TICKS_EXTENDED)
            {
                this.recipeState = 2;
                finishRecipe();
            }
        }
        if (recipeState == 2 && fluidStorage.getAmount() == 0)
        {
            stopRecipe();
        }
    }

    public WritableSingleFluidStorage getStorage(Direction direction)
    {
//        Direction facing = getCachedState().get(HydraulicPressBlock.FACING);
//        return direction == null || direction == facing || direction == facing.getOpposite() ? fluidStorage : null;
        return fluidStorage;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        fluidStorage.toNbt(nbt);
        nbt.putShort("recipeState", recipeState);
        nbt.putBoolean("recipeControlled", recipeControlled);
        if (currentRecipe != null)
            nbt.putString("currentRecipe", currentRecipe.getId().toString());

        behaviour.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        fluidStorage.readNbt(nbt);
        this.recipeState = nbt.getShort("recipeState");
        this.recipeControlled = nbt.getBoolean("recipeControlled");

        this.currentRecipe = MiscUtil.ifPresentOrNull(nbt, "currentRecipe",
            s -> (AbstractPressingRecipe<CastingBasinStorage>) MeatlibRecipes.getInstance().get(ResourceLocation.tryParse(s)).orElse(null));

        behaviour.readNbt(nbt);
    }

    public class SqueezingRecipeBehaviour extends RecipeBehaviour<MobSqueezingRecipe> implements NbtSerialisable
    {
        @Override
        public void startRecipe(MobSqueezingRecipe recipe)
        {
            if (recipe == null)
                return;

            setRecipe(recipe);
            recipeState = 0;
        }

        @Override
        public void interrupt()
        {
            recipeState = 0;
            this.currentRecipe = null;
        }

        @Override
        public void finishRecipe()
        {
            if (currentRecipe != null)
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    AABB box = AABB.of(new BoundingBox(worldPosition.below()));
                    List<Entity> entities = level.getEntities(EntityTypeTest.forClass(Entity.class), box, e -> true);
                    if (!entities.isEmpty())
                        currentRecipe.ejectOutputs(new MobSqueezeContext(level, worldPosition, entities), transaction);

                    transaction.commit();
                }
            }
        }
    }

    public static Storage<FluidVariant> getFluidStorageFromTop(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity be, @Nullable Direction direction)
    {
        if (world.getBlockEntity(pos.below()) instanceof HydraulicPressBlockEntity press)
        {
            return press.fluidStorage;
        }
        return null;
    }

    public enum Mode
    {
        PRESSING,
        FAT_PRESSING,
        SQUEEZING;
    }
}
