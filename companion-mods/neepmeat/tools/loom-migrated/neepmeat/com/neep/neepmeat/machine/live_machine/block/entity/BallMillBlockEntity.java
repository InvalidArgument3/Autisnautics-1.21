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
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipe;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.BallMillBlock;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BallMillBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private float progressIncrement;

    // Rendering
    public float angle;
    public float rotSpeedRadians;

    public boolean updateFromNeighbour = true;

    public final InputSlot inputSlot = new InputSlot(this::setChanged);

    public BallMillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.BALL_MILL;
    }

    @Override
    public float progressIncrement()
    {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        if (this.progressIncrement != progressIncrement)
        {
            this.progressIncrement = progressIncrement;
            sync();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("progress_increment", progressIncrement);
        inputSlot.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.progressIncrement = nbt.getFloat("progress_increment");
        inputSlot.readNbt(nbt);
    }

    public void clientTick()
    {
        // Snap to adjacent
        if (updateFromNeighbour || level.getGameTime() % 5 == 0)
        {
            if (getBlockState().getValue(BallMillBlock.CONNECTION) != BallMillBlock.AxisConnection.NONE)
            {
                BallMillBlockEntity other = null;

                if (updateFromNeighbour)
                {
                    BallMillBlockEntity plus = null;
                    BallMillBlockEntity minus = null;

                    if (level.getBlockEntity(BallMillBlock.offset(worldPosition, getBlockState(), -1)) instanceof BallMillBlockEntity be1)
                        minus = be1;
                    if (level.getBlockEntity(BallMillBlock.offset(worldPosition, getBlockState(), 1)) instanceof BallMillBlockEntity be1)
                        plus = be1;

                    if (plus != null && plus.progressIncrement() > 0)
                        other = plus;
                    else if (minus != null && minus.progressIncrement() > 0)
                        other = minus;

                    if (other != null)
                    {
                        angle = other.angle;
                        rotSpeedRadians = other.rotSpeedRadians;
                        setProgressIncrement(other.progressIncrement);
                    }
                }
                else
                {
                    if (level.getBlockEntity(BallMillBlock.offset(worldPosition, getBlockState(), -1)) instanceof BallMillBlockEntity be1)
                        other = be1;

                    if (other != null)
                    {
                        angle = other.angle;
                        rotSpeedRadians = other.rotSpeedRadians;
                        setProgressIncrement(other.progressIncrement);
                    }
                }
            }

            updateFromNeighbour = false;
        }
    }

    public static class InputSlot extends WritableStackStorage
    {
        public float progress;
        @Nullable private BallMillRecipe recipe;

        public InputSlot(@Nullable Runnable parent)
        {
            super(parent, 1);
        }

        public void tick(float progressIncrement, Storage<ItemVariant> output, float chanceMod, TransactionContext transaction)
        {
            var storage = new BallMillRecipe.BallMillContext.Process(this, output);

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
                BallMillRecipeGenerator.INSTANCE.getFirstMatch(storage)
                        .ifPresent(foundRecipe -> recipe = foundRecipe);

                syncIfPossible();
            }
        }
    }
}
