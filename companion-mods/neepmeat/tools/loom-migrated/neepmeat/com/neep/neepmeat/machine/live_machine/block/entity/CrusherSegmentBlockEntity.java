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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.grinder.CrusherRecipeContext;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import com.neep.neepmeat.recipe.CrushingRecipe;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CrusherSegmentBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private final InputSlot slot = new InputSlot(this::sync);
    private float progressIncrement;
    private float maxIncrement;
    private final Random jrandom = new Random();

    public CrusherSegmentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("progress_increment", progressIncrement);
        slot.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.progressIncrement = nbt.getFloat("progress_increment");
        slot.readNbt(nbt);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<?> getComponentType()
    {
        return LivingMachineComponents.CRUSHER_SEGMENT;
    }

    public InputSlot getStorage()
    {
        return slot;
    }

    @Override
    public float progressIncrement()
    {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        if (progressIncrement != this.progressIncrement)
        {
            this.progressIncrement = progressIncrement;
            sync();
        }
    }

    public float minIncrement()
    {
        return 0;
    }

    public static class InputSlot extends WritableStackStorage
    {
        @Nullable
        private CrushingRecipe recipe;
        private float progress;

        public InputSlot(@Nullable Runnable parent)
        {
            super(parent);
        }

        public void tick(float progressIncrement, Storage<ItemVariant> output, float chanceMod, TransactionContext transaction)
        {
            var storage = new SimpleCrushingStorage(this, output, chanceMod);
            if (recipe != null)
            {
                progress += progressIncrement;
                if (progress >= recipe.getTime())
                {
                    if (recipe.ejectOutputs(storage, transaction))
                    {
                        recipe.takeInputs(storage, transaction);
                    }
                    recipe = null;
                    progress = 0;
                    syncIfPossible();
                }
            }
            else if (!isEmpty())
            {
                CrushingRecipe foundRecipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.ADVANCED_CRUSHING, storage).orElse(null);

                if (foundRecipe == null)
                    foundRecipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.GRINDING, storage).orElse(null);

                if (foundRecipe != null)
                {
                    recipe = foundRecipe;
                }
                else if (!isEmpty())
                {
                    recipe = MeatlibRecipes.getInstance().get(NMrecipeTypes.ADVANCED_CRUSHING, new ResourceLocation(NeepMeat.NAMESPACE, "advanced_crushing/destroy")).orElse(null);
                }
                syncIfPossible();
            }
        }

        @Override
        public long getCapacity()
        {
            return 1;
        }

        @Override
        protected long getCapacity(ItemVariant variant)
        {
            return 1;
        }

        @Override
        public void writeNbt(CompoundTag nbt)
        {
            if (recipe != null)
                nbt.putString("recipe", recipe.getId().toString());
            super.writeNbt(nbt);
        }

        @Override
        public void readNbt(CompoundTag nbt)
        {
            super.readNbt(nbt);
            if (nbt.contains("recipe"))
                this.recipe = (CrushingRecipe) MeatlibRecipes.getInstance().get(ResourceLocation.tryParse(nbt.getString("recipe"))).orElse(null);
//                this.recipe = MeatlibRecipes.getInstance().get(NMrecipeTypes.GRINDING, Identifier.tryParse(nbt.getString("recipe"))).orElse(null);
            else
                this.recipe = null;
        }

        @Nullable
        public CrushingRecipe getRecipe()
        {
            return recipe;
        }
    }

    public void clientTick()
    {
        float intensity = progressIncrement / 2;

        // Particles will be more frequent at higher power. Clamp above 1 to prevent / 0.
        int tickInterval = (int) Mth.clamp(1, 1 / (intensity * 2), 100);

        if ((level.getGameTime() % tickInterval) == 0 && progressIncrement() >= minIncrement())
        {
            if (slot.isEmpty() || slot.getRecipe() == null)
                return;

            double px;
            double pz;
            if (jrandom.nextBoolean())
            {
                px = worldPosition.getX() + 0.5 + (jrandom.nextBoolean() ? -1.5 : 1.5);
                pz = getBlockPos().getZ() + 0.5 + ((jrandom.nextFloat() - 0.5)) * 3;
            }
            else
            {
                pz = worldPosition.getZ() + 0.5 + (jrandom.nextBoolean() ? -1.5 : 1.5);
                px = getBlockPos().getX() + 0.5 + ((jrandom.nextFloat() - 0.5)) * 3;
            }
            double py = getBlockPos().getY() + 0.5 + (jrandom.nextFloat() - 0.5) * 0.5;
//            double px = getPos().getX() + 0.5 + ((jrandom.nextFloat() - 0.5)) * 3;
//            double pz = getPos().getZ() + 0.5 + ((jrandom.nextFloat() - 0.5)) * 3;

            double vx = (jrandom.nextFloat() - 0.5) * 0.4;
            double vy = jrandom.nextFloat() * 0.3;
            double vz = (jrandom.nextFloat() - 0.5) * 0.4;

            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, slot.getAsStack()),
                    px, py, pz, vx, vy, vz);
        }
    }

    public static class SimpleCrushingStorage implements CrusherRecipeContext
    {
        private final Storage<ItemVariant> input;
        private final Storage<ItemVariant> output;
        private final XpStorage xpStorage = new XpStorage();
        private final float chanceMod;

        public SimpleCrushingStorage(Storage<ItemVariant> input, Storage<ItemVariant> output, float chanceMod)
        {
            this.input = input;
            this.output = output;
            this.chanceMod = chanceMod;
        }

        @Override
        public Storage<ItemVariant> getInputStorage()
        {
            return input;
        }

        @Override
        public Storage<ItemVariant> getOutputStorage()
        {
            return output;
        }

        @Override
        public Storage<ItemVariant> getExtraStorage()
        {
            return output;
        }

        @Override
        public XpStorage getXpStorage()
        {
            return xpStorage;
        }

        @Override
        public float getChanceMod()
        {
            return chanceMod;
        }
    }
}
