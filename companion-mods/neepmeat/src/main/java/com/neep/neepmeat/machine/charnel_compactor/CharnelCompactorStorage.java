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

package com.neep.neepmeat.machine.charnel_compactor;

import com.google.common.collect.MapMaker;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StoragePreconditions;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ExtractionOnlyStorage;
import com.neep.meatlib.transfer.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static net.minecraft.core.Direction.UP;

// Most of this is copied from ComposterWrapper. It looks awful. I'm never doing this again.
@SuppressWarnings("UnstableApiUsage")
public class CharnelCompactorStorage extends SnapshotParticipant<Float>
{
    // It appears that these are the lengths one must go for avoiding BlockEntities
    private static final Map<WorldLocation, CharnelCompactorStorage> CHARNEL_STORAGES = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    private static final ItemVariant OUTPUT = ItemVariant.of(NMItems.CRUDE_INTEGRATION_CHARGE);
    private static final float DO_NOTHING = 0f;
    private static final float EXTRACT_OUTPUT = -1f;
    private final WorldLocation location;
    private final TopStorage upStorage = new TopStorage();
    private final BottomStorage downStorage = new BottomStorage();
    // -1 if output was extracted, otherwise the composter increase probability of the (pending) inserted item.
    private Float increaseProbability = DO_NOTHING;

    private CharnelCompactorStorage(WorldLocation location)
    {
        this.location = location;
    }

    @Nullable
    public static Storage<ItemVariant> getStorage(Level world, BlockPos pos, Direction direction)
    {
        Objects.requireNonNull(direction);

        WorldLocation location = new WorldLocation(world, pos.immutable());
        CharnelCompactorStorage storage = CHARNEL_STORAGES.computeIfAbsent(location, CharnelCompactorStorage::new);
        if (direction == UP)
        {
            return storage.upStorage;
        }
        else
        {
            return storage.downStorage;
        }
    }

    public static void extractOutput(WorldLocation location, boolean spawnItem)
    {
        location.setBlockState(location.getBlockState().setValue(CharnelCompactorBlock.LEVEL, 0));
        Level world = location.world;
        BlockPos pos = location.pos;
        if (spawnItem)
        {
            double d = (world.random.nextFloat() * 0.7f) + 0.15f;
            double e = (world.random.nextFloat() * 0.7f) + 0.06000000238418579 + 0.6;
            double g = (world.random.nextFloat() * 0.7f) + 0.15f;
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + d, pos.getY() + e, pos.getZ() + g, OUTPUT.toStack(1));
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
            world.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static void addLevel(WorldLocation location)
    {
        BlockState state = location.getBlockState();
        int newLevel = Math.min(7, state.getValue(CharnelCompactorBlock.LEVEL) + 3);
        BlockState newState = state.setValue(CharnelCompactorBlock.LEVEL, newLevel);
        location.setBlockState(newState);

        if (newLevel == 7)
        {
            location.world.scheduleTick(location.pos, state.getBlock(), 20);
        }

    }

    protected Float createSnapshot()
    {
        return increaseProbability;
    }

    protected void readSnapshot(Float snapshot)
    {
        // Reset after unsuccessful commit.
        increaseProbability = snapshot;
    }

    protected void onFinalCommit()
    {
        // Apply pending action
        if (increaseProbability == EXTRACT_OUTPUT)
        {
            extractOutput(location, false);
        }
        else if (increaseProbability > 0)
        {
            boolean increaseSuccessful = location.world.getRandom().nextDouble() < increaseProbability;

            if (increaseSuccessful)
            {
                addLevel(location);
            }
        }

        // Reset after successful commit.
        increaseProbability = DO_NOTHING;
    }

    public record WorldLocation(Level world, BlockPos pos)
    {
        private BlockState getBlockState()
        {
            return world.getBlockState(pos);
        }

        private void setBlockState(BlockState state)
        {
            world.setBlockAndUpdate(pos, state);
        }
    }

    private class TopStorage implements InsertionOnlyStorage<ItemVariant>
    {
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // Check amount.
            if (maxAmount < 1) return 0;

            // Check Integrator presence.
            Integrator integrator = Integrator.findIntegrator(location.world(), location.pos(), 10, 0);

            if (increaseProbability != DO_NOTHING || integrator == null || !integrator.canEnlighten())
                return 0;

            // Check that the composter can accept items.
            if (location.getBlockState().getValue(CharnelCompactorBlock.LEVEL) >= 7)
                return 0;

            // Check that the item is compostable.
            float insertedIncreaseProbability = CharnelCompactorBlock.getIncreaseChance(resource.getItem());

            if (insertedIncreaseProbability <= 0)
                return 0;

            // Schedule insertion.
            updateSnapshots(transaction);
            increaseProbability = insertedIncreaseProbability;
            return 1;
        }

        public Iterator<StorageView<ItemVariant>> iterator()
        {
            return Collections.emptyIterator();
        }
    }

    private class BottomStorage implements ExtractionOnlyStorage<ItemVariant>, SingleSlotStorage<ItemVariant>
    {

        private boolean hasOutput()
        {
            // We only have bone meal if the level is 8 and no action was scheduled.
            return increaseProbability == DO_NOTHING && location.getBlockState().getValue(CharnelCompactorBlock.LEVEL) == 8;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // Check amount.
            if (maxAmount < 1) return 0;
            // Check that the resource is bone meal.
            if (!OUTPUT.equals(resource)) return 0;
            // Check that there is bone meal to extract.
            if (!hasOutput()) return 0;

            updateSnapshots(transaction);
            increaseProbability = EXTRACT_OUTPUT;
            return 1;
        }

        public boolean isResourceBlank()
        {
            return getResource().isBlank();
        }

        public ItemVariant getResource()
        {
            return OUTPUT;
        }

        public long getAmount()
        {
            return hasOutput() ? 1 : 0;
        }

        public long getCapacity()
        {
            return 1;
        }
    }
}