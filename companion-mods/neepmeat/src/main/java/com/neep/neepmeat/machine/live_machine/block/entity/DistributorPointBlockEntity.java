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
 *
 * NeepBus inputs are not on this block entity. The address, mode, and parcel queue are.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.entity.skyworm.SkywormEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.DistributorPointBlock;
import com.neep.neepmeat.transport.worm.DistributorParcel;
import com.neep.neepmeat.transport.worm.DistributorRouting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DistributorPointBlockEntity extends SyncableBlockEntity implements DistributorPoint {
    public static final String NULL_ADDRESS = "";
    private String address = NULL_ADDRESS;
    private Mode mode = Mode.RECEIVE;
    private boolean powered;
    private final List<DistributorParcel> queuedParcels = new ArrayList<>();

    public DistributorPointBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.DISTRIBUTOR_BE.get(), pos, state);
    }

    public void setAddress(String address) {
        this.address = address;
        setChanged();
        if (level != null) {
            DistributorRouting.get(level).registerPoint(this);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        setChanged();
        if (level != null) {
            DistributorRouting.get(level).registerPoint(this);
        }
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        setChanged();
        if (level != null) {
            DistributorRouting.get(level).registerPoint(this);
        }
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void receive(DistributorParcel parcel) {
        queuedParcels.add(parcel);
        setChanged();
    }

    public List<DistributorParcel> getQueuedParcels() {
        return queuedParcels;
    }

    @Override
    public Level getPointWorld() {
        return getLevel();
    }

    @Override
    public BlockPos getPointPos() {
        return getBlockPos();
    }

    public static void createWorm(Level world, BlockState state, BlockPos pos, SkywormAnimationTimes times) {
        Direction facing = state.getValue(DistributorPointBlock.VERTICAL_DIRECTION);
        Vec3 target = Vec3.atCenterOf(pos.relative(facing));
        Vec3 position = target.add(0, facing.getStepY() * 200, 0);
        SkywormEntity worm = SkywormEntity.setup(world, position);
        worm.begin(target.y, times);
        world.addFreshEntity(worm);
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.DISTRIBUTOR_POINT;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putString("address", address);
        nbt.putInt("mode", mode.ordinal());
        nbt.putBoolean("powered", powered);
        ListTag parcels = new ListTag();
        for (DistributorParcel parcel : queuedParcels) {
            parcels.add(parcel.toNbt());
        }
        nbt.put("parcels", parcels);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.address = nbt.getString("address");
        this.mode = Mode.fromNbt(nbt.getInt("mode"));
        this.powered = nbt.getBoolean("powered");
        queuedParcels.clear();
        ListTag parcels = nbt.getList("parcels", Tag.TAG_COMPOUND);
        for (int i = 0; i < parcels.size(); i++) {
            queuedParcels.add(DistributorParcel.fromNbt(parcels.getCompound(i)));
        }
    }

    public enum Mode {
        SEND,
        RECEIVE,
        SEND_AND_RECEIVE;

        public static Mode fromNbt(int mode) {
            return values()[mode];
        }

        public boolean canReceive() {
            return this == RECEIVE || this == SEND_AND_RECEIVE;
        }

        public boolean canSend() {
            return this == SEND || this == SEND_AND_RECEIVE;
        }
    }
}
