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
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.entity.skyworm.SkywormCollectParcelAction;
import com.neep.neepmeat.entity.skyworm.SkywormEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.DistributorPointBlock;
import com.neep.neepmeat.transport.worm.DistributorParcel;
import com.neep.neepmeat.transport.worm.GlobalDistributorRouting;
import com.neep.neepmeat.transport.worm.RouteLock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DistributorPointBlockEntity extends SyncableBlockEntity implements DistributorPoint, ConfigProvider
{
    public static final String NULL_ADDRESS = "";
    public static final int MIN_COOLDOWN = 300;

    private String address = NULL_ADDRESS;
    private Mode mode = Mode.RECEIVE;
    private int maxCooldown = MIN_COOLDOWN;
    private boolean autoSend = true;

    private boolean powered = false;

    public int timeUntilPickup = 0;
    @Nullable public RouteLock routeLock;

    public boolean sendNext;

    private final WritePort sendPort = f -> sendNext = true;
    private final WritePort addressPoint = f -> setAddress(f.stringValue());

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .applyChanges(this)
            .input(new SimpleEntry("Send", "On data write:\nAttempt to perform a send"), sendPort)
            .input(new SimpleEntry("Set channel", "On data write:\nSet this distributor's channel to the value."), addressPoint)
            .build();

    // TODO: drop when broken
    private final List<DistributorParcel> queuedParcels = new ObjectArrayList<>();

    public DistributorPointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setController(BlockPos pos)
    {
        boolean newPowered = pos != null;
        if (powered != newPowered)
        {
            powered = newPowered;
            GlobalDistributorRouting.updatePoint(this);
            setChanged();
        }
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.DISTRIBUTOR_POINT;
    }

    @Override
    public boolean isPowered()
    {
        return powered;
    }

    @Override
    public void setLevel(Level world)
    {
        super.setLevel(world);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putString("address", address);
        nbt.putInt("mode", mode.ordinal());
        nbt.putInt("max_cooldown", maxCooldown);
        nbt.putBoolean("auto_send", autoSend);
        nbt.put("config", config.writeNbt(new CompoundTag()));

        ListTag parcels = new ListTag();
        for (var parcel : queuedParcels)
        {
            parcels.add(parcel.toNbt());
        }
        nbt.put("parcels", parcels);

        if (routeLock != null)
            nbt.put("route", routeLock.toNbt());
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.address = nbt.getString("address");
        this.mode = Mode.values()[nbt.getInt("mode")];
        this.maxCooldown = nbt.getInt("max_cooldown");
        this.autoSend = nbt.getBoolean("auto_send");
        this.config.readNbt(nbt.getCompound("config"));

        queuedParcels.clear();
        ListTag parcels = nbt.getList("parcels", Tag.TAG_COMPOUND);
        for (int i = 0; i < parcels.size(); ++i)
        {
            CompoundTag compound = parcels.getCompound(i);
            queuedParcels.add(DistributorParcel.fromNbt(compound));
        }

        routeLock = RouteLock.fromNbt(nbt.getCompound("route"));
    }

    @Override
    public String getAddress()
    {
        return address;
    }

    @Override
    public void setAddress(String addr, Mode mode, int maxCooldown, boolean autoSend)
    {
        this.address = addr;
        this.mode = mode;
        this.maxCooldown = Math.max(maxCooldown, MIN_COOLDOWN);
        this.autoSend = autoSend;
        GlobalDistributorRouting.updatePoint(this);
        setChanged();
    }

    public void setAddress(String addr)
    {
//        PlayerLookup.around((ServerWorld) world, pos, 20).forEach(p ->
//        {
////            NarratorSayS2C.CHANNEL.emitter(p).apply(addr, true);
//            NEEPSpeakSayS2C.CHANNEL.emitter(p).apply(addr, pos);
//        });

        this.address = addr;
        GlobalDistributorRouting.updatePoint(this);
        setChanged();
    }

    @Override
    public Mode getMode()
    {
        return mode;
    }

    public void onBroken()
    {
        GlobalDistributorRouting.unregisterPoint(getLevel(), worldPosition);
    }

    public void onPlaced()
    {
        GlobalDistributorRouting.updatePoint(this);
    }

    @Override
    public void receive(DistributorParcel parcel)
    {
        queuedParcels.add(parcel);
        setChanged();
    }

    @Override
    public List<DistributorParcel> getQueuedParcels()
    {
        return queuedParcels;
    }

    @Override
    public Level getPointWorld()
    {
        return getLevel();
    }

    @Override
    public BlockPos getPointPos()
    {
        return getBlockPos();
    }

    @Override
    public void scheduleSend(RouteLock routeReference, SkywormAnimationTimes times)
    {
        this.timeUntilPickup = times.descend();
        this.routeLock = routeReference;
        setChanged();

        // Spawn the worme
        createWorm(getLevel(), getBlockState(), getBlockPos(), times);
    }

    public static void createWorm(Level world, BlockState state, BlockPos pos, SkywormAnimationTimes times)
    {
        Direction facing = state.getValue(DistributorPointBlock.VERTICAL_DIRECTION);
        boolean fromBelow = facing == Direction.DOWN;

        Vec3 targetPosition = pos.relative(facing, 1).getCenter();
        Vec3 position = targetPosition.add(0, facing.getStepY() * 200, 0);
        var worm = SkywormEntity.setup(world, position);
        worm.setFromBelow(fromBelow);
        worm.setAllowWobble(false);

        worm.setAction(new SkywormCollectParcelAction(worm, position.y, targetPosition.y, times));

        world.addFreshEntity(worm);
    }

    @Override
    public BlockPos getDumpPos()
    {
        return getBlockPos().relative(getBlockState().getValue(DistributorPointBlock.VERTICAL_DIRECTION));
    }

    @Override
    public int getMaxCooldown()
    {
        return maxCooldown;
    }

    @Override
    public int getCooldown()
    {
        return timeUntilPickup;
    }

    @Override
    public boolean shouldAutoSend()
    {
        return autoSend;
    }

    public void invalidateNetwork()
    {

    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public enum Mode
    {
        SEND,
        RECEIVE,
        SEND_AND_RECEIVE;

        public static Mode fromNbt(int mode)
        {
            return values()[mode];
        }

        public boolean canReceive()
        {
            return this == RECEIVE || this == SEND_AND_RECEIVE;
        }

        public boolean canSend()
        {
            return this == SEND || this == SEND_AND_RECEIVE;
        }
    }
}
