/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BuilderBlockEntity extends BlockEntity implements PLCActuator, PLCActuator.Provider {
    public static final int SLOTS = 27;
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(SLOTS, this::setChanged);
    private int selectedSlot;
    private String selectedPattern = "";
    private @Nullable BlockPos target;
    private Vec3 targetVec;
    private double tipX;
    private double tipY;
    private double tipZ;

    public BuilderBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.BUILDER_BE.get(), pos, state);
        this.tipX = pos.getX() + 0.5;
        this.tipY = pos.getY() + 1;
        this.tipZ = pos.getZ() + 0.5;
        this.targetVec = new Vec3(tipX, tipY, tipZ);
    }

    public ImplementedInventory inventory() {
        return inventory;
    }

    public void serverTick() {
        if (target == null) {
            return;
        }
        double dx = targetVec.x - tipX;
        double dy = targetVec.y - tipY;
        double dz = targetVec.z - tipZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0E-4) {
            return;
        }
        double step = Math.min(1.0, dist);
        tipX += dx / dist * step;
        tipY += dy / dist * step;
        tipZ += dz / dist * step;
    }

    public boolean placeSelected(BlockPos pos, Direction face) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        if (selectedSlot >= 0 && selectedSlot < inventory.getContainerSize()) {
            return place(server, inventory.getItem(selectedSlot), pos, face);
        }
        return false;
    }

    public boolean place(ServerLevel server, ItemStack stack, BlockPos targetPos, Direction facing) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Vec3 hitPos = Vec3.atCenterOf(targetPos);
        BlockHitResult hit = new BlockHitResult(hitPos, facing.getOpposite(), targetPos, true);
        var player = FakePlayerFactory.getMinecraft(server);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return blockItem.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit)).consumesAction();
    }

    public boolean destroyBlock(BlockPos pos, Consumer<ItemStack> drops) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        float speed = state.getDestroySpeed(level, pos);
        float obsidian = Blocks.OBSIDIAN.defaultBlockState().getDestroySpeed(level, pos);
        if (state.isAir() || speed < 0 || speed > obsidian) {
            return false;
        }
        List<ItemStack> stacks = Block.getDrops(state, server, pos, level.getBlockEntity(pos));
        if (!level.destroyBlock(pos, false)) {
            return false;
        }
        stacks.forEach(drops);
        return true;
    }

    public boolean insertDrop(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (current.isEmpty()) {
                inventory.setItem(slot, remaining.copy());
                remaining = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(current, remaining) && current.getCount() < current.getMaxStackSize()) {
                int move = Math.min(remaining.getCount(), current.getMaxStackSize() - current.getCount());
                current.grow(move);
                remaining.shrink(move);
                inventory.setItem(slot, current);
            }
        }
        if (!remaining.isEmpty() && level != null) {
            Block.popResource(level, worldPosition, remaining);
        }
        return remaining.isEmpty();
    }

    public void setSlot(int slot) {
        selectedSlot = Mth.clamp(slot, 0, SLOTS - 1);
        setChanged();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    @Override
    public BlockPos getBasePos() {
        return worldPosition;
    }

    @Override
    public void spawnItem(ItemStack stored) {
        if (stored != null && !stored.isEmpty() && level != null) {
            Containers.dropItemStack(level, tipX, tipY, tipZ, stored.copy());
        }
    }

    @Override
    public ItemStack getStored(PLC plc) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStored(PLC plc, ItemStack stored) {
    }

    @Override
    public void dumpStored(PLC plc) {
    }

    @Override
    public boolean reachedTarget(@Nullable PLC plc) {
        return target == null || targetVec.distanceToSqr(tipX, tipY, tipZ) <= 0.1 * 0.1;
    }

    @Override
    public void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException {
        this.target = target;
        this.targetVec = target == null ? new Vec3(tipX, tipY, tipZ) : Vec3.upFromBottomCenterOf(target, 0);
    }

    @Override
    public double getX() {
        return tipX;
    }

    @Override
    public double getY() {
        return tipY;
    }

    @Override
    public double getZ() {
        return tipZ;
    }

    @Override
    public boolean actuatorRemoved() {
        return isRemoved();
    }

    @Override
    public PLCActuator getPlcActuator() {
        return this;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.writeNbt(new CompoundTag()));
        tag.putInt("selected_slot", selectedSlot);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.readNbt(tag.getCompound("inventory"));
        selectedSlot = tag.getInt("selected_slot");
    }
}
