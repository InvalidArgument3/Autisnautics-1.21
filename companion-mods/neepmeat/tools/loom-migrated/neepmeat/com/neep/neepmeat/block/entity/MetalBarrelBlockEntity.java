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

package com.neep.neepmeat.block.entity;

import com.neep.neepmeat.block.MetalBarrelBlock;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetalBarrelBlockEntity extends RandomizableContainerBlockEntity
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ContainerOpenersCounter stateManager = new ContainerOpenersCounter(){

        @Override
        protected void onOpen(Level world, BlockPos pos, BlockState state)
        {
            if (state.getValue(MetalBarrelBlock.TYPE) != MetalBarrelBlock.Type.BOTTOM)
                MetalBarrelBlockEntity.this.playSound(state, NMSounds.METAL_BARREL_OPEN);

            MetalBarrelBlockEntity.this.setOpen(state, true);
        }

        @Override
        protected void onClose(Level world, BlockPos pos, BlockState state)
        {
            if (state.getValue(MetalBarrelBlock.TYPE) != MetalBarrelBlock.Type.BOTTOM)
                MetalBarrelBlockEntity.this.playSound(state, NMSounds.METAL_BARREL_CLOSE);

            MetalBarrelBlockEntity.this.setOpen(state, false);
        }

        @Override
        protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount)
        {
        }

        @Override
        protected boolean isOwnContainer(Player player)
        {
            if (player.containerMenu instanceof ChestMenu)
            {
                Container inventory = ((ChestMenu)player.containerMenu).getContainer();
                return inventory == MetalBarrelBlockEntity.this
                        || (inventory instanceof CompoundContainer doubleInventory && doubleInventory.contains(MetalBarrelBlockEntity.this));
            }
            return false;
        }
    };

    public MetalBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        if (!this.trySaveLootTable(nbt))
        {
            ContainerHelper.saveAllItems(nbt, this.inventory);
        }
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbt))
        {
            ContainerHelper.loadAllItems(nbt, this.inventory);
        }
    }

    @Override
    public int getContainerSize()
    {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems()
    {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list)
    {
        this.inventory = list;
    }

    public MenuProvider openHandler(Player player, Level world, BlockPos pos, BlockState state,
                                                 Container top, Container botttom)
    {
        // JAAAAAAANK
        return new MenuProvider()
        {
            @Override
            public Component getDisplayName()
            {
                return getDefaultName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
            {
                if (botttom != null && top != null)
                {
                    return ChestMenu.sixRows(syncId, playerInventory, new CompoundContainer(top, botttom));
                }
                else if (top != null)
                {
                    return ChestMenu.threeRows(syncId, playerInventory, top);
                }
                return null; // Invalid state, so return null.
            }
        };
    }

    @Override
    protected Component getDefaultName()
    {
        return Component.translatable("container.barrel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory)
    {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    public void startOpen(Player player)
    {
        if (!this.remove && !player.isSpectator())
        {
            this.stateManager.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player)
    {
        if (!this.remove && !player.isSpectator())
        {
            this.stateManager.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void tick()
    {
        if (!this.remove)
        {
            this.stateManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    void setOpen(BlockState state, boolean open)
    {
        this.level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, open), Block.UPDATE_ALL);
    }

    void playSound(BlockState state, SoundEvent soundEvent)
    {
        Vec3i vec3i = state.getValue(BarrelBlock.FACING).getNormal();
        double d = (double)this.worldPosition.getX() + 0.5 + (double)vec3i.getX() / 2.0;
        double e = (double)this.worldPosition.getY() + 0.5 + (double)vec3i.getY() / 2.0;
        double f = (double)this.worldPosition.getZ() + 0.5 + (double)vec3i.getZ() / 2.0;
        this.level.playSound(null, d, e, f, soundEvent, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
    }

    public Storage<ItemVariant> getStorage(@Nullable Direction direction)
    {
        BlockState state = getBlockState();
        @Nullable MetalBarrelBlockEntity other = MetalBarrelBlock.getOther(level, worldPosition, state);

        if (other == null)
        {
            return InventoryStorage.of(this, direction);
        }
        else
        {
            return new CombinedSlottedStorage<>(List.of(
                    InventoryStorage.of(this, direction),
                    InventoryStorage.of(other, direction)
            ));
        }
    }
}
