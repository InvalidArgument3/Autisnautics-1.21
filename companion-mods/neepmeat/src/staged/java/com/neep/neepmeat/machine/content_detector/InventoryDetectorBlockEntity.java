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

package com.neep.neepmeat.machine.content_detector;

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.screen_handler.ContentDetectorScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.machine.InventoryDetectorInventory;
import com.neep.neepmeat.util.FilterUtils;
import com.neep.neepmeat.util.ItemUtil;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import com.neep.meatlib.transfer.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("UnstableApiUsage")
public class InventoryDetectorBlockEntity extends BlockEntity implements MenuProvider, InventoryDetector
{
    public static final String NBT_POWERED = "powered";
    public static final String NBT_MODE = "mode";
    public static final String NBT_COUNT_MODE = "count_mode";
    public static final String NBT_BEHAVIOUR_MODE = "behaviour_mode";

    public InventoryDetectorInventory filterInventory = new InventoryDetectorInventory();

    protected BlockApiCache<Storage<ItemVariant>, Direction> cache;
    protected boolean powered;
    protected int mode = 0;
    protected CountMode countMode = CountMode.ANY_AMOUNT;
    protected BehaviourMode behaviourMode = BehaviourMode.REGULATE;

    private boolean prevPowered;

    public InventoryDetectorBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.INVENTORY_DETECTOR, pos, state);
    }

    public InventoryDetectorBlockEntity(BlockEntityType<InventoryDetectorBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public CountMode getCountMode()
    {
        return countMode;
    }

    @Override
    public void cycleCountMode()
    {
        countMode = CountMode.values()[(countMode.ordinal() + 1) % CountMode.values().length];
        setChanged();
    }

    @Override
    public BehaviourMode getBehaviourMode()
    {
        return behaviourMode;
    }

    @Override
    public void cycleBehaviourMode()
    {
        behaviourMode = BehaviourMode.values()[(behaviourMode.ordinal() + 1) % BehaviourMode.values().length];
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        filterInventory.writeNbt(tag);
        tag.putBoolean(NBT_POWERED, powered);
        tag.putInt(NBT_MODE, mode);
        tag.putInt(NBT_COUNT_MODE, countMode.ordinal());
        tag.putInt(NBT_BEHAVIOUR_MODE, behaviourMode.ordinal());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        filterInventory.readNbt(tag);
        powered = tag.getBoolean(NBT_POWERED);
        mode = tag.getInt(NBT_MODE);
        countMode = CountMode.values()[tag.getInt(NBT_COUNT_MODE)];
        behaviourMode = BehaviourMode.values()[tag.getInt(NBT_BEHAVIOUR_MODE)];
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new ContentDetectorScreenHandler(syncId, inv, this.filterInventory, this);
    }

    public void refreshCache()
    {
        if (level instanceof ServerLevel serverWorld)
        {
            Direction facing = getBlockState().getValue(InventoryDetectorBlock.FACING);
            cache = BlockApiCache.create(ItemStorage.SIDED, serverWorld, worldPosition.relative(facing));
        }
    }

    public void observeStorage()
    {
        Direction facing = getBlockState().getValue(InventoryDetectorBlock.FACING);
        Storage<ItemVariant> observedStorage;
        if (cache != null && (observedStorage = cache.find(facing.getOpposite())) != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                // Get a list of ItemVariants in the filter with no duplicates
                List<ItemVariant> variants = StreamSupport.stream(InventoryStorage.of(filterInventory, facing).spliterator(), false)
                        .filter(ItemUtil::notBlank)
                        .map(StorageView::getResource)
                        .distinct()
                        .collect(Collectors.toList());

                // Determine the check to perform
                FilterUtils.Filter filter = switch (countMode)
                {
                    case GREATER_IN_FILTER -> ((obs, filt) -> obs < filt);
                    case GREATER_IN_STORAGE -> ((obs, filt) -> obs > filt);
                    case EQUAL -> ((obs, filt) -> obs == filt);
                    default -> ((obs, filt) -> true);
                };

                List<?> filtered = variants.stream().filter(variant ->
                {
                    Optional<Long> observedView = ItemUtil.totalAmount(InventoryStorage.of(filterInventory, facing.getOpposite()), variant, transaction);
                    Optional<Long> filterView = ItemUtil.totalAmount(observedStorage, variant, transaction);

                    if (observedView.isPresent() && filterView.isPresent())
                    {
                        return filter.test(filterView.get(), observedView.get());
                    }
                    return false;
                }).toList();

                int size = filtered.size();
                int size2 = StreamSupport.stream(observedStorage.spliterator(), false)
                        .map(StorageView::getResource)
                        .filter(FilterUtils.containsVariant(variants))
                        .toList().size();

                if (behaviourMode == BehaviourMode.REGULATE)
                {
                    if (mode == 0) // Waiting for stacks to arrive
                    {
                        if (size == variants.size() && size != 0)
                        {
                            powered = true;
                            mode = 1;
                        }
                    }
                    else if (mode == 1) // Waiting for stacks to leave
                    {
                        if (size2 == 0)
                        {
                            powered = false;
                            mode = 0;
                        }
                    }
                }
                else if (behaviourMode == BehaviourMode.ABSOLUTE)
                {
                    powered = size == variants.size();
                }

                transaction.commit();
            }
        }
        else powered = false;

    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, InventoryDetectorBlockEntity be)
    {
        if ((world.getGameTime() % 10) != 0)
            return;

        if (be.cache == null)
            be.refreshCache();

        be.observeStorage();


        if (be.prevPowered != be.powered)
        {
            be.prevPowered = be.powered;
            BlockState newState = state.setValue(InventoryDetectorBlock.POWERED, be.powered);
            world.setBlock(pos, newState, InventoryDetectorBlock.UPDATE_CLIENTS);
            be.updateNeighbors(world, pos, state);
        }
    }

    protected void updateNeighbors(Level world, BlockPos pos, BlockState state)
    {
        Direction direction = state.getValue(InventoryDetectorBlock.FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        world.neighborChanged(blockPos, state.getBlock(), pos);
        world.updateNeighborsAtExceptFromFacing(blockPos, state.getBlock(), direction);
    }

    public Storage<ItemVariant> getStorage(Direction ignored)
    {
        return filterInventory.getStorage();
    }

}