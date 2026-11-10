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

import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
public class GrinderStorage extends SimpleContainer implements CrusherRecipeContext
{
    protected CrusherBlockEntity parent;
    protected WritableStackStorage inputStorage;
    protected WritableStackStorage outputStorage;
    protected WritableStackStorage extraStorage;
    protected XpStorage xpStorage;

    public GrinderStorage(CrusherBlockEntity parent)
    {
        this.parent = parent;
        this.inputStorage = new WritableStackStorage(parent::sync)
        {
            @Override
            public boolean canInsert(ItemVariant resource)
            {
                return true;
            }
        };

        this.outputStorage = new WritableStackStorage(parent::sync, 32)
        {
            @Override
            public boolean supportsInsertion()
            {
                return false;
            }
        };

        this.extraStorage = new WritableStackStorage(parent::sync, 32)
        {
            @Override
            public boolean supportsInsertion()
            {
                return false;
            }
        };

        this.xpStorage = new XpStorage();
    }

    public Storage<ItemVariant> getItemStorage(Direction direction)
    {
        if (direction == Direction.UP || direction == null)
        {
            return inputStorage;
        }
        else if (direction == parent.getBlockState().getValue(CrusherBlock.FACING) || direction == Direction.DOWN)
        {
            return outputStorage;
        }
        return null;
    }

    public WritableStackStorage getInputStorage()
    {
        return inputStorage;
    }

    public XpStorage getXpStorage()
    {
        return xpStorage;
    }

    @Override
    public float getChanceMod()
    {
        return 0;
    }

    public void writeNbt(CompoundTag nbt)
    {
        CompoundTag inputNbt = new CompoundTag();
        inputStorage.writeNbt(inputNbt);
        nbt.put("input", inputNbt);

        CompoundTag outputNbt = new CompoundTag();
        outputStorage.writeNbt(outputNbt);
        nbt.put("output", outputNbt);

        CompoundTag extraNbt = new CompoundTag();
        extraStorage.writeNbt(extraNbt);
        nbt.put("extra", extraNbt);

        CompoundTag xpNbt = new CompoundTag();
        xpStorage.writeNbt(xpNbt);
        nbt.put("xp", xpNbt);
    }

    public void readNbt(CompoundTag nbt)
    {
        CompoundTag inputNbt = nbt.getCompound("input");
        inputStorage.readNbt(inputNbt);

        CompoundTag outputNbt = nbt.getCompound("output");
        outputStorage.readNbt(outputNbt);

        CompoundTag extraNbt = nbt.getCompound("extra");
        extraStorage.readNbt(extraNbt);

        CompoundTag xpNbt = nbt.getCompound("xp");
        xpStorage.readNbt(xpNbt);
    }

    public Storage<ItemVariant> getOutputStorage()
    {
        return outputStorage;
    }

    public Storage<ItemVariant> getExtraStorage()
    {
        return extraStorage;
    }

    public void dropItems(Level world, BlockPos pos)
    {
        Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inputStorage.getAsStack());
        Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, outputStorage.getAsStack());
    }

}
