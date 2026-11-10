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

package com.neep.neepmeat.machine.deployer;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.entity.player.FakePlayerEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("UnstableApiUsage")
public class DeployerBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    protected final float minPower = 0.02f;
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(9);
    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);
    public float shuttleOffset;
    public boolean powered;
    public boolean roundRobin = false;
    public boolean interactBlock = true;
    protected MotorEntity motor;
    protected LazySupplier<FakePlayerEntity> playerSupplier = LazySupplier.of(() ->
    {
        if (level instanceof ServerLevel serverWorld)
        {
            //            fake.delegateInventory(inventory, this::markDirty);
            return FakePlayerEntity.get(serverWorld);
        }
        return null;
    });
    private int roundRobinIndex = 0;
    private float power = 0;

    public DeployerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.powered = nbt.getBoolean("powered");
        this.roundRobin = nbt.getBoolean("round_robin");
        this.interactBlock = nbt.getBoolean("interact_block");
        this.roundRobinIndex = nbt.getInt("round_index");
        this.inventory.readNbt(nbt.getCompound("inventory"));
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putBoolean("powered", powered);
        nbt.putBoolean("round_robin", roundRobin);
        nbt.putBoolean("interact_block", interactBlock);
        nbt.putInt("round_index", roundRobinIndex);
        nbt.put("inventory", inventory.writeNbt(new CompoundTag()));
    }

    private int currentStack()
    {
        int slot;
        if (roundRobin)
        {
            slot = roundRobin();
        }
        else
        {
            slot = chooseNonEmptySlot(level.getRandom());
        }

        return slot;
    }

    private int roundRobin()
    {
        roundRobinIndex %= 9; // Just in case
        int slot = roundRobinIndex;

        int j = (roundRobinIndex + 1) % 9;

        // Navigate to next non-empty stack
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            if (!inventory.getItem((j + i) % 9).isEmpty())
            {
                roundRobinIndex = (j + i) % 9;
                break;
            }
        }

        return slot;
    }

    private int chooseNonEmptySlot(RandomSource random)
    {
        int i = 0;
        int j = 1;

        for (int k = 0; k < inventory.getContainerSize(); ++k)
        {
            if (!(inventory.getItem(k)).isEmpty() && random.nextInt(j++) == 0)
            {
                i = k;
            }
        }

        return i;
    }

    public void deploy(ServerLevel world, int slot)
    {
        ItemStack stack = inventory.removeItemNoUpdate(slot);
        if (stack.isEmpty() && !interactBlock)
            return;

        Direction facing = getBlockState().getValue(BaseFacingBlock.FACING);
        ServerPlayer player = this.playerSupplier.get();
        player.setServerLevel(world);
        Vec3i unit = facing.getNormal();
        player.setPosRaw(
                worldPosition.getX() + 0.5 + unit.getX() * 0.5,
                worldPosition.getY() + 0.5 - player.getEyeHeight(player.getPose()) + unit.getY() * 0.5,
                worldPosition.getZ() + 0.5 + unit.getZ() * 0.5
        );

        player.setXRot(pitch(facing));
        player.setYRot(yaw(facing));

        // Temporarily transfer the stack to the player's inventory
        player.inventory.setItem(player.inventory.selected, stack.isEmpty() ? ItemStack.EMPTY : stack);

        BlockPos targetPos = worldPosition.relative(facing);

        Vec3 hitPos = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitPos, facing.getOpposite(), targetPos, true);

        if (interactBlock)
        {
            BlockState state = world.getBlockState(hit.getBlockPos());
            InteractionResult actionResult = state.use(world, player, InteractionHand.MAIN_HAND, hit);
            if (!actionResult.consumesAction())
            {
                InteractionResult useResult = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            }
        }
        else
        {
            player.gameMode.useItem(player, world, stack, InteractionHand.MAIN_HAND);
        }

        // Move the item and all produced items back to this inventory
        ItemStack removed = player.inventory.removeItemNoUpdate(player.inventory.selected);
        inventory.setItem(slot, removed);
        Inventory playerInventory = player.getInventory();
        if (!playerInventory.isEmpty())
        {
            playerInventory.dropAll();
        }
    }

    private float pitch(Direction facing)
    {
        return switch (facing)
        {
            case DOWN -> 90;
            case UP -> -90;
            case NORTH, WEST, EAST, SOUTH -> 0;
        };
    }
    
    private float yaw(Direction facing)
    {
        return switch (facing)
        {
            case DOWN, UP, SOUTH -> 0;
            case NORTH -> 180F;
            case WEST -> 90F;
            case EAST -> -90F;
        };
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        // I can't be bothered to add a timer
        if (!level.isClientSide() && level.getGameTime() % 10 == 0 && motor.getMechPUPower() >= minPower)
        {
            int slot = currentStack();
            deploy((ServerLevel) level, slot);

            return true;
        }
        return false;
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
        if (!powered && power > 0) // Rising edge
        {
            powered = true;
            sync();
        }
        else if (powered && power == 0)
        {
            powered = false;
            sync(); // Falling edge
        }
    }

    @Override
    public void setRemoved()
    {
        if (playerSupplier.isInitialised())
        {
            playerSupplier.get().remove(Entity.RemovalReason.DISCARDED);
        }
        super.setRemoved();
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(power < minPower, power, minPower);
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        tryUpdateComparators();
    }

    public Container getInventory()
    {
        return inventory;
    }

    public Storage<ItemVariant> getStorage(Direction unused)
    {
        return inventoryStorage;
    }
}
