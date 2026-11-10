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

package com.neep.meatweapons.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatweapons.screen.TinkerTableScreenHandler;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.entity.ImplantRegistry;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.machine.upgrade_manager.ImplantManagerDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TinkerTableBlockEntity extends SyncableBlockEntity implements MenuProvider, ImplantManagerDelegate
{
    private final ImplementedInventory inventory = new ImplementedInventory()
    {
        private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

        @Override
        public NonNullList<ItemStack> getItems()
        {
            return items;
        }

        @Override
        public void setChanged()
        {
            TinkerTableBlockEntity.this.sync();
        }
    };

    public TinkerTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        inventory.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        inventory.readNbt(nbt);
    }

    public ImplementedInventory getInventory()
    {
        return inventory;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new TinkerTableScreenHandler(syncId, playerInventory, inventory, getBlockPos());
    }

    @Override
    public @Nullable ImplantManager getImplantManager()
    {
        return NMComponents.IMPLANT_MANAGER.getNullable(inventory.getItem(0));
    }

    @Override
    public @Nullable ItemLike removeUpgrade(ResourceLocation identifier)
    {
        var manager = getImplantManager();
        if (manager != null)
        {
            if (manager.removeImplant(identifier))
            {
                return ImplantRegistry.getItem(identifier);
            }
        }
        return null;
    }
}
