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

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import com.neep.neepmeat.transport.worm.DistributorDestination;
import com.neep.neepmeat.transport.worm.DistributorParcel;
import com.neep.neepmeat.transport.worm.DistributorRouting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DistributorPointProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(LivingMachineComponents.DISTRIBUTOR_POINT);
    private static final SkywormAnimationTimes TIMES = new SkywormAnimationTimes(80, 20, 70, 10);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.DISTRIBUTOR_POINT).ifPresent(result -> {
            DistributorPointBlockEntity point = (DistributorPointBlockEntity) result.t1().iterator().next();
            DistributorRouting.get(point.getPointWorld()).registerPoint(point);
            if (point.getMode().canReceive()) {
                handleReceive(point, be.getCombinedItemOutput(), be.getCombinedFluidOutput());
            }
            if (point.getMode().canSend()) {
                handleSend(point, be.getCombinedItemInput(), be.getCombinedFluidInput());
            }
        });
    }

    private void handleReceive(DistributorPoint point, Storage<ItemVariant> itemOutput, Storage<FluidVariant> fluidOutput) {
        List<DistributorParcel> queue = point instanceof DistributorPointBlockEntity entity ? entity.getQueuedParcels() : List.of();
        BlockPos dump = point instanceof DistributorPointBlockEntity entity ? entity.getDumpPos() : point.getPointPos();
        while (!queue.isEmpty()) {
            DistributorParcel parcel = queue.remove(queue.size() - 1);
            if (parcel.isEmpty()) {
                continue;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                for (ResourceAmount<ItemVariant> item : parcel.items()) {
                    long transferred = itemOutput.insert(item.resource(), item.amount(), transaction);
                    long left = item.amount() - transferred;
                    if (left > 0 && point.getPointWorld() != null) {
                        ItemStack stack = item.resource().toStack((int) Math.min(left, Integer.MAX_VALUE));
                        point.getPointWorld().addFreshEntity(new ItemEntity(point.getPointWorld(),
                                dump.getX() + 0.5, dump.getY() + 0.5, dump.getZ() + 0.5, stack));
                    }
                }
                for (ResourceAmount<FluidVariant> fluid : parcel.fluids()) {
                    fluidOutput.insert(fluid.resource(), fluid.amount(), transaction);
                }
                transaction.commit();
            }
        }
    }

    private void handleSend(DistributorPointBlockEntity point, Storage<ItemVariant> itemInput, Storage<FluidVariant> fluidInput) {
        if (point.getPointWorld() == null) {
            return;
        }
        DistributorRouting routing = DistributorRouting.get(point.getPointWorld());
        if (point.timeUntilPickup > 0) {
            point.timeUntilPickup--;
        } else if (point.routeLock != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                DistributorParcel parcel = extractParcel(itemInput, fluidInput, transaction);
                if (!parcel.isEmpty()) {
                    transaction.commit();
                    routing.send(parcel, new DistributorDestination(point.getPointWorld(), point.routeLock.destPos()), TIMES);
                } else if (point.getPointWorld().getServer() != null) {
                    point.routeLock.release(point.getPointWorld().getServer());
                }
                point.timeUntilPickup = point.getMaxCooldown();
                point.routeLock = null;
                point.setChanged();
            }
        } else if (point.sendNext || (point.shouldAutoSend() && point.getLevel().getGameTime() % 20 == 0)) {
            point.sendNext = false;
            DistributorDestination destination = routing.findAndRoute(point.getPointPos(), point.getAddress());
            if (destination == null) {
                return;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                DistributorParcel parcel = extractParcel(itemInput, fluidInput, transaction);
                if (!parcel.isEmpty()) {
                    point.scheduleSend(routing.acquireRoute(destination.pos()), TIMES);
                }
            }
        }
    }

    private static DistributorParcel extractParcel(Storage<ItemVariant> items, Storage<FluidVariant> fluids, TransactionContext transaction) {
        return new DistributorParcel(extractAvailable(items, 9, 64, transaction), extractAvailable(fluids, 9, 8 * FluidConstants.BUCKET, transaction));
    }

    private static <T> List<ResourceAmount<T>> extractAvailable(Storage<T> storage, int stacks, long max, TransactionContext transaction) {
        List<ResourceAmount<T>> result = new ArrayList<>();
        for (int i = 0; i < stacks; i++) {
            try (Transaction inner = ((Transaction) transaction).openNested()) {
                StorageView<T> view = null;
                for (StorageView<T> candidate : storage.nonEmptyViews()) {
                    view = candidate;
                    break;
                }
                if (view == null || view.getResource() == null) {
                    break;
                }
                long extracted = storage.extract(view.getResource(), Math.min(max, view.getAmount()), inner);
                if (extracted > 0) {
                    result.add(new ResourceAmount<>(view.getResource(), extracted));
                    inner.commit();
                }
            }
        }
        return result;
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "distributor");
    }
}
