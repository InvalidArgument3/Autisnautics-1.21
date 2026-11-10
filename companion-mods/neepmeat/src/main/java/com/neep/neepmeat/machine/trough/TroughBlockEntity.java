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

package com.neep.neepmeat.machine.trough;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TroughBlockEntity extends SyncableBlockEntity implements MotorisedBlock
{
    public static final FluidVariant RESOURCE = FluidVariant.of(NMFluids.ANIMAL_FEED.still());

    private final TroughStorage storage = new TroughStorage(2 * FluidConstants.BUCKET, this::sync);

    private final Storage<ItemVariant> itemInputStorage = new Storage<>()
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            long conversion = NeepMeat.CONFIG.feedingTrough.feedPerItem.getRealValue();
            long insertableFluid = storage.getCapacity() - storage.getAmount();
            // Avoid long overflow
            long insertableItems = (insertableFluid - (insertableFluid % conversion)) / conversion;
            insertableItems = Math.min(insertableItems, maxAmount);
            long fluidAmount = Math.max(0, Math.min(insertableFluid, insertableItems * conversion));
            fluidAmount -= fluidAmount % conversion;

            storage.insert(RESOURCE, fluidAmount, transaction);

            return fluidAmount / conversion;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) { return 0; }

        @Override
        public @NotNull Iterator<StorageView<ItemVariant>> iterator() { return Collections.emptyIterator(); }
    };

    public TroughBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public TroughBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FEEDING_TROUGH, pos, state);
    }

    public WritableSingleFluidStorage getStorage(@Nullable Direction ignored)
    {
        return storage;
    }

    private int age = 0;
    private int waitTicks = 100;

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.toNbt(nbt);
        nbt.putInt("wait_ticks", waitTicks);
        nbt.putInt("age", age); // Save age so that all troughs with the same power will not activate simultaneously.
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
        this.waitTicks = nbt.getInt("wait_ticks");
        this.age = nbt.getInt("age");
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        ++age;

        if (age % waitTicks == 0)
        {
            feedAnimals();
        }

        if (motor.getMechPUPower() > 0 && level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverWorld)
        {
//            serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, NMFluids.STILL_FEED.getDefaultState().getBlockState()),
                serverWorld.sendParticles( NMParticles.MEAT_BIT,
                    getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5,
                    1,
                    0.5, 0.5, 0.5, 0.01);
        }

        return true;
    }

    @Override
    public void setInputPower(float power)
    {
        waitTicks = (int) Mth.clamp((1 - power) * 60 * 20, 100, 60 * 20);
    }

    public void feedAnimals()
    {
        if (storage.isResourceBlank())
            return;

        if (level instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(ParticleTypes.COMPOSTER, worldPosition.getX() + 0.5, worldPosition.getY() + 0.6, worldPosition.getZ() + 0.5, 4, 0.25, 0.25, 0.25, 0.1);
        }
        try (Transaction transaction = Transaction.openOuter())
        {
            AABB box = AABB.of(new BoundingBox(worldPosition).inflatedBy(5));
            var entities = level.getEntities(
                    EntityTypeTest.forClass(Animal.class), box, e -> true)
                    .stream().collect(Collectors.partitioningBy(AgeableMob::isBaby));

            List<Animal> adults = entities.get(false);
            List<Animal> babies = entities.get(true);

            Collections.shuffle(adults);
            Collections.shuffle(babies);

            try (Transaction sub1 = transaction.openNested())
            {
                if (!babies.isEmpty() && extractFeed(NeepMeat.CONFIG.feedingTrough.useAmount.getRealValue() / 2, sub1))
                {
                    babies.get(0).setAge(60);
                    sub1.commit();
                }
                else
                {
                    sub1.abort();
                }
            }

            try (Transaction inner = transaction.openNested())
            {
                if (adults.size() >= 2 && extractFeed(NeepMeat.CONFIG.feedingTrough.useAmount.getRealValue(), inner))
                {
                    for (int i = 0; i < Math.min(2, adults.size()); ++i)
                    {
                        Animal mob = adults.get(i);
                        mob.setAge(0);
                        mob.setInLove(null);
                    }
                    inner.commit();
                }
                else
                {
                    inner.abort();
                }
            }

            transaction.commit();
        }
    }

    private boolean extractFeed(long amount, TransactionContext transaction)
    {
        return storage.extractDirect(TroughBlockEntity.RESOURCE, amount, transaction) == amount;
    }

    public boolean handleInteract(Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        // Try to fill from the item
        if (stack.is(NMTags.GRAINS) || stack.is(NMBlocks.FLESH_POTATO.asItem()))
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                var storage = getStorage(null);
                long perItem = NeepMeat.CONFIG.feedingTrough.feedPerItem.getRealValue();
                long inserted = storage.insert(RESOURCE, perItem, transaction);
                if (inserted >= perItem)
                {
                    stack.shrink(1);
                    player.setItemInHand(hand, stack);
                    transaction.commit();
                    return true;
                }
                transaction.abort();
            }
        }
        else
        {
            return WritableSingleFluidStorage.handleInteract(getStorage(null), world, player, hand);
        }
        return false;
    }

    public Storage<ItemVariant> getItemStorage(@Nullable Direction ignored)
    {
        return itemInputStorage;
    }

    private static class TroughStorage extends WritableSingleFluidStorage
    {
        public TroughStorage(long capacity, Runnable finalCallback)
        {
            super(capacity, finalCallback);
        }

        @Override
        public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        public long extractDirect(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction)
        {
            return super.extract(extractedVariant, maxAmount, transaction);
        }
    }
}
