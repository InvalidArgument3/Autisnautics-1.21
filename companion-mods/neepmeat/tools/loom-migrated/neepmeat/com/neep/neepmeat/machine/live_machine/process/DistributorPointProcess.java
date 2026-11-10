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

import com.neep.meatlib.storage.MeatlibStorageUtil;
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
import com.neep.neepmeat.transport.worm.GlobalDistributorRouting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.List;

public class DistributorPointProcess implements Process
{
    public static final List<ComponentType<?>> REQUIRED = List.of(LivingMachineComponents.DISTRIBUTOR_POINT);

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.DISTRIBUTOR_POINT).ifPresent(r ->
        {
            var distributors = r.t1();

            // TODO: handle multiple distributors

            var point = distributors.iterator().next();

            if (point.getMode().canReceive())
                handleReceive(point, be.getCombinedItemOutput(), be.getCombinedFluidOutput());

            if (point.getMode().canSend())
                handleSend(point, be.getCombinedItemInput(), be.getCombinedFluidInput());
        });
    }

    private void handleReceive(DistributorPoint point, Storage<ItemVariant> itemOutput, Storage<FluidVariant> combinedFluidOutput)
    {
        var queue = point.getQueuedParcels();
        BlockPos dumpPos = point.getDumpPos();
        while (!queue.isEmpty())
        {
            var parcel = queue.remove(queue.size() - 1);

            if (!parcel.isEmpty())
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    for (var item : parcel.items())
                    {
                        long transferred = itemOutput.insert(item.resource(), item.amount(), transaction);

                        // Dump into the world if there was no space
                        if (transferred < item.amount())
                        {
                            MeatlibStorageUtil.scatterAmount(point.getPointWorld(), dumpPos,
                                    new ResourceAmount<>(item.resource(), item.amount() - transferred));
                        }
                    }

                    for (var fluid : parcel.fluids())
                    {
                        long transferred = combinedFluidOutput.insert(fluid.resource(), fluid.amount(), transaction);

                        // There's no way of dumping fluid into the world
                    }

                    transaction.commit();
                }
            }
        }
    }

    private void handleSend(DistributorPointBlockEntity point, Storage<ItemVariant> itemInput, Storage<FluidVariant> combinedFluidInput)
    {
        SkywormAnimationTimes times = new SkywormAnimationTimes(80, 20, 70, 10);

        if (point.timeUntilPickup > 0)
        {
            point.timeUntilPickup--;
        }
        else if (point.timeUntilPickup == 0 && point.routeLock != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                var parcel = extractParcel(itemInput, combinedFluidInput, transaction);

                if (!parcel.isEmpty())
                {
                    transaction.commit();
                    GlobalDistributorRouting.getInstance().send(
                            parcel,
                            point.routeLock.asDestination(point.getPointWorld().getServer()),
                            times
                    );
                }
                else
                {
                    point.routeLock.release(point.getPointWorld().getServer());
                }

                point.timeUntilPickup = point.getMaxCooldown();
                point.routeLock = null;
                point.setChanged();
            }
        }
        else if (point.timeUntilPickup == 0 && (point.sendNext
                || (point.shouldAutoSend() && point.getLevel().getGameTime() % 20 == 0)))
        {
            point.sendNext = false;

            var global = GlobalDistributorRouting.getInstance();
            var dest = global.findAndRoute(new DistributorDestination(point), point.getAddress());
            if (dest != null)
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    var parcel = extractParcel(itemInput, combinedFluidInput, transaction);

                    if (!parcel.isEmpty())
                    {
                        point.scheduleSend(global.acquireRoute(dest), times);
                    }
                }
            }
        }
    }

    private static DistributorParcel extractParcel(Storage<ItemVariant> itemStorage, Storage<FluidVariant> fluidStorage, TransactionContext transaction)
    {
        var items = extractAvailable(itemStorage, 9, 64, transaction);
        var fluids = extractAvailable(fluidStorage, 9, 8 * FluidConstants.BUCKET, transaction);

        return new DistributorParcel(items, fluids);
    }

    private static <T> List<ResourceAmount<T>> extractAvailable(Storage<T> storage, int stacks, long max, TransactionContext transaction)
    {
        List<ResourceAmount<T>> result = new ObjectArrayList<>();
        for (int i = 0; i < stacks; i++)
        {
            try (Transaction inner = transaction.openNested())
            {
                T variant = StorageUtil.findExtractableResource(storage, inner);
                if (variant != null)
                {
                    long extracted = storage.extract(variant, max, inner);

                    result.add(new ResourceAmount<>(variant, extracted));
                    inner.commit();
                }
            }
        }

        return result;
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "distributor_point");
    }
}
