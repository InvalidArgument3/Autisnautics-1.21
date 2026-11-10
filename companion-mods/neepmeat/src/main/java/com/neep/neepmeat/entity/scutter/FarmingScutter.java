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

package com.neep.neepmeat.entity.scutter;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMSounds;
import com.neep.meatlib.transfer.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FarmingScutter extends ScutterEntity
{
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(6);
    private final InventoryStorage storage = InventoryStorage.of(inventory, null);

    private final LinkedHashSet<BlockPos> targets = new LinkedHashSet<>(); // Preserve order

    @Nullable private BlockPos storagePos;
    private BlockPos homePos;

    public FarmingScutter(EntityType<? extends ScutterEntity> type, Level world)
    {
        super(type, world);
        homePos = BlockPos.containing(position()); // Becomes 0 0 0, but at least it's not null.
    }

    public void setStoragePos(@Nullable BlockPos pos)
    {
        this.storagePos = pos;
    }

    public void setHomePos(BlockPos homePos)
    {
        this.homePos = homePos;
    }

    public BlockPos getHomePos()
    {
        return homePos;
    }

    public static boolean isGrownCrop(BlockState state)
    {
        return
                state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)
                || state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) >= 3;
    }

    public static boolean hasMoisture(BlockState state)
    {
        return state.hasProperty(BlockStateProperties.MOISTURE)
                || state.is(NMTags.FARMLAND)
                || state.is(Blocks.SOUL_SAND);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        inventory.writeNbt(nbt);
        nbt.put("home", NbtUtils.writeBlockPos(homePos));
        if (storagePos != null)
            nbt.put("storage", NbtUtils.writeBlockPos(storagePos));
        return nbt;
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        inventory.readNbt(nbt);
        this.homePos = NbtUtils.readBlockPos(nbt.getCompound("home"));
        if (nbt.contains("storage"))
            this.storagePos = NbtUtils.readBlockPos(nbt.getCompound("storage"));
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();
        goalSelector.addGoal(1, new ScutterBreakCropGoal(this));
        goalSelector.addGoal(2, new ScutterMoveToCropGoal(this, 0.3f, 32));
        goalSelector.addGoal(3, new ScutterFindGrownCropsGoal(this, 40));
        goalSelector.addGoal(4, new ScutterReturnHomeGoal(this, 0.3f, 32));
//        goalSelector.add(4, new ScutterFindFarmlandGoal(this, 7, 1));
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand)
    {
        if (player.isShiftKeyDown())
        {
            for (int i = 0; i < inventory.getContainerSize(); ++i)
            {
                spawnAtLocation(inventory.getItem(i));
            }

            pickUp();

            return InteractionResult.SUCCESS;
        }
        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!level().isClientSide()
                && level().getGameTime() % 20 == 0
                && isAtHome()
                && !inventory.isEmpty())
        {
            depositItems();
        }

        targets.removeIf(p -> !isGrownCrop(level().getBlockState(p)));
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
    }

    public void harvest(Level world, BlockPos pos, BlockState state, Block block)
    {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        List<ItemStack> stacks = Block.getDrops(state, (ServerLevel) world, pos, blockEntity, this, ItemStack.EMPTY);

        Item seedsItem = block.asItem();
        boolean seedsConsumed = false;
        try (Transaction transaction = Transaction.openOuter())
        {
            for (var stack : stacks)
            {
                if (!seedsConsumed && stack.is(seedsItem))
                {
                    stack = stack.copy(); // Not sure whether the given stack is read only
                    stack.shrink(1);
                    seedsConsumed = true;
                }

                if (!stack.isEmpty())
                    storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            }
            transaction.commit();
        }

        // Make sure the block is broken
        world.destroyBlock(pos, false);

        world.setBlockAndUpdate(pos, replant(state, block));

//        world.playSound(this, getBlockPos(), block.getSoundGroup(state).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
    }

    private BlockState replant(BlockState oldState, Block block)
    {
        if (block instanceof CropBlock crop)
        {
            return crop.getStateForAge(0);
        }
        else if (block instanceof NetherWartBlock)
        {
            return oldState.setValue(NetherWartBlock.AGE, 0);
        }
        return Blocks.AIR.defaultBlockState();
    }

    public void depositItems()
    {
        if (storagePos != null)
        {
            Storage<ItemVariant> homeStorage = ItemStorage.SIDED.find(level(), storagePos, Direction.UP);

            if (homeStorage != null)
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    long transferred = StorageUtil.move(storage, homeStorage, v -> true, Long.MAX_VALUE, transaction);
                    if (transferred > 0)
                    {
                        level().playSound(this, blockPosition(), NMSounds.DEPOSIT_ITEMS.get(), SoundSource.NEUTRAL, 1, 1);
                        transaction.commit();
                    }
                }

            }
        }

        for (var stack : inventory.getItems())
        {
            spawnAtLocation(stack);
        }
        inventory.clearContent();
    }

    public void addTargets(Collection<BlockPos> nearest)
    {
        targets.addAll(nearest);
    }

    public Set<BlockPos> getTargets()
    {
        return targets;
    }

    @Nullable
    @Override
    public ItemStack getPickResult()
    {
        return new ItemStack(NMItems.FARMING_SCUTTER);
    }

    public ImplementedInventory getInventory()
    {
        return inventory;
    }

    public boolean needsEmptying()
    {
        return inventory.emptyStacks() == 0;
    }

    public boolean isAtHome()
    {
        return position().closerThan(getHomePos().getCenter(), 1);
    }
}
