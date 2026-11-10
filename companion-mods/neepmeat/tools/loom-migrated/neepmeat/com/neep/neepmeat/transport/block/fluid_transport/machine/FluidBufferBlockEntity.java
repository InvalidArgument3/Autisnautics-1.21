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

package com.neep.neepmeat.transport.block.fluid_transport.machine;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.ingredient.IngredientType;
import com.neep.meatlib.recipe.ingredient.Ingredients;
import com.neep.neepmeat.api.storage.FluidBuffer;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.plc.component.TableComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidBufferBlockEntity extends SyncableBlockEntity implements FluidBuffer.FluidBufferProvider
{
    protected final TableComponent<FluidVariant> tableComponent = new Component();

    protected final WritableSingleFluidStorage buffer;

    public FluidBufferBlockEntity(BlockEntityType type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.buffer = new WritableSingleFluidStorage((FluidConstants.BUCKET), this::sync);
    }

    public FluidBufferBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FLUID_BUFFER, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        buffer.toNbt(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        buffer.readNbt(tag);
    }

    @Override
    @Nullable
    public WritableSingleFluidStorage getBuffer(Direction direction)
    {
        return buffer;
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        if (WritableSingleFluidStorage.handleInteract(buffer, level, player, hand))
        {
            return true;
        }
        else if (!level.isClientSide())
        {
            TankBlockEntity.showContents((ServerPlayer) player, level, worldPosition, buffer);
            return true;
        }
        return true;
    }

    public TableComponent<FluidVariant> getTableComponent(Void v)
    {
        return tableComponent;
    }

    @SuppressWarnings("UnstableApiUsage")
    protected class Component implements TableComponent<FluidVariant>
    {
        @Override
        public Storage<FluidVariant> getStorage()
        {
            return FluidBufferBlockEntity.this.getBuffer(null);
        }

        @Override
        public IngredientType<?> getType()
        {
            return Ingredients.FLUID;
        }
    }
}
