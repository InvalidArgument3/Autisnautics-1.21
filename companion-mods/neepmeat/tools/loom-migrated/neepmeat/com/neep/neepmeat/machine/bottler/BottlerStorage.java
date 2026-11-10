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

package com.neep.neepmeat.machine.bottler;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.implant.item.LivingToolImplant;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.LivingToolImplantItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public class BottlerStorage implements NbtSerialisable
{
    private final BottlerBlockEntity parent;
    private final WritableStackStorage itemStorage;

    private Storage<FluidVariant> fillingStorage = null;

    public BottlerStorage(BottlerBlockEntity parent)
    {
        this.parent = parent;
        itemStorage = new WritableStackStorage(parent::sync, 1)
        {
            @Override
            protected boolean canInsert(ItemVariant variant)
            {
                return super.canInsert(variant);
            }

            @Override
            protected void onFinalCommit()
            {
                updateFillingStorage();
                super.onFinalCommit();
            }
        };
    }

    private Storage<FluidVariant> makeFillingStorage()
    {
        ItemStack stack = itemStorage.getAsStack();
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(itemStorage);
        Storage<FluidVariant> found = FluidStorage.ITEM.find(stack, context);
        if (found != null)
            return found;

        if (LivingToolImplantItem.applicable(itemStorage.variant.getItem()))
        {
            ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);
            if (manager != null && manager.isInstalled(LivingToolImplant.ID))
            {
                found = LivingToolImplantItem.getStorage(itemStorage.getAsStack(), context);
                if (found != null)
                    return found;
            }
        }

        return Storage.empty();
    }

    public void updateFillingStorage()
    {
        fillingStorage = makeFillingStorage();
    }

    public WritableStackStorage getItemStorage()
    {
        return itemStorage;
    }

    public Storage<FluidVariant> getFluidStorage()
    {
        if (fillingStorage == null)
            updateFillingStorage();

        return fillingStorage;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("itemStorage", itemStorage.toNbt(new CompoundTag()));
//        nbt.put("fluidStorage", fluidStorage.writeNbt(new NbtCompound()));

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        itemStorage.readNbt(nbt.getCompound("itemStorage"));
//        fluidStorage.readNbt(nbt.getCompound("fluidStorage"));
    }
}
