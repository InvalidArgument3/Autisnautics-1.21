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
import com.neep.meatlib.entity.MobKillUtil;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.init.NMDamageSources;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.ItemInputComponent;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class LargestHopperBlockEntity extends SyncableBlockEntity implements ItemInputComponent
{
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(4, this::sync);
    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);

    public LargestHopperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        inventory.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        inventory.readNbt(nbt);
    }

    @Override
    public void sync()
    {
        super.sync();
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.ITEM_INPUT;
    }

    public void insertEntity(ItemEntity itemEntity)
    {
        if (itemEntity.isRemoved())
            return;

        ItemStack stack = itemEntity.getItem();
        if (inventory.isEmpty())
        {
            inventory.setItem(0, itemEntity.getItem());
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
            inventory.setChanged();
        }
        else
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long inserted = inventoryStorage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                if (inserted == 0)
                {
                    return;
                }
                if (inserted == stack.getCount())
                {
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                }
                else
                {
                    stack.shrink((int) inserted);
                }
                transaction.commit();
            }
        }
    }

    public InventoryStorage getStorage(Direction unused)
    {
        return inventoryStorage;
    }

    public ImplementedInventory getInventory()
    {
        return inventory;
    }

    public void damageEntities(float power, List<ItemStack> resultStacks, int looting)
    {
        if (level.getGameTime() % 10 == 0)
        {
            AABB box = new AABB(worldPosition).inflate(1, 0, 1);

            level.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, e -> true).forEach(entity ->
            {
                float damageAmount = power * 20;
                DamageSource damageSource = level.damageSources().source(NMDamageSources.CRUSHER);

                if (entity.isAlive() && entity.canBeSeenAsEnemy() && entity.hurtTime == 0)
                {
                    if (entity.getHealth() <= damageAmount)
                    {
                        MobKillUtil.dropLoot((ServerLevel) level, entity, damageSource, resultStacks, true, looting);
                        entity.setDropsLoot(false);
                        if (entity instanceof ServerPlayer player && !level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).get())
                        {
                            extractPlayerInventory(player, resultStacks);
                        }
                        entity.hurt(damageSource, Float.MAX_VALUE);
                    }
                    entity.hurt(damageSource, damageAmount);
                }
            });
        }
    }

    public static void extractPlayerInventory(ServerPlayer player, List<ItemStack> outputStacks)
    {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            var stack = inventory.removeItemNoUpdate(i);
            if (!stack.isEmpty())
                outputStacks.add(stack);
        }
    }
}
