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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.fluid.PersistentFluid;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import com.neep.meatlib.transfer.ContainerItemContext;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StoragePreconditions;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.SingleSlotStorage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@SuppressWarnings("UnstableApiUsage")
public class PipetteItem extends BaseItem
{
    protected static ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> API_PROVIDER = (stack, context) -> new StackStorage(context);

    public PipetteItem(TooltipSupplier tooltip, Properties settings)
    {
        super(tooltip, settings);
        FluidStorage.ITEM.registerForItems(API_PROVIDER, this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack pipetteStack = user.getItemInHand(hand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        BlockPos pos = blockHitResult.getBlockPos();
        if (blockHitResult.getType() == HitResult.Type.BLOCK)
        {
            if (!world.mayInteract(user, pos))
                return InteractionResultHolder.fail(pipetteStack);

            BlockState state = world.getBlockState(blockHitResult.getBlockPos());
            FluidState fluidState = state.getFluidState();
            if (fluidState.getType() instanceof PersistentFluid fluid)
            {
                long amount = fluid.dropletAmount(fluidState);
                FluidVariant variant = fluid.getVariant(fluidState);
                if (amount > 0)
                {
                    try (Transaction transaction = Transaction.openOuter())
                    {
                        var pipetteStorage = new StackStorage(ContainerItemContext.ofPlayerHand(user, hand));
                        long inserted = pipetteStorage.insert(variant, amount, transaction);

                        if (fluid.extractOrNothing(world, pos, fluidState, inserted))
                            transaction.commit();

                        return InteractionResultHolder.success(user.getItemInHand(hand));
                    }
                }
            }

        }

        return InteractionResultHolder.pass(pipetteStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        if (!world.isClientSide() && context.getPlayer() != null)
        {
            Storage<FluidVariant> blockStorage = FluidStorage.SIDED.find(world, context.getClickedPos(), context.getClickedFace());
            Storage<FluidVariant> itemStorage = FluidStorage.ITEM.find(context.getItemInHand(), ContainerItemContext.ofPlayerHand(context.getPlayer(), context.getHand()));

            // Only clear fluid if the targeted block does not contain fluid.
            if (context.getPlayer().isShiftKeyDown() && itemStorage != null && blockStorage == null)
            {
                // Clear the storage
                try (Transaction transaction = Transaction.openOuter())
                {
                    FluidVariant resource = StorageUtil.findExtractableResource(itemStorage, transaction);
                    if (resource != null && !resource.isBlank())
                    {
                        itemStorage.extract(resource, Long.MAX_VALUE, transaction);
                        world.playSound(null, context.getClickedPos(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.2f, 1);
                    }
                    transaction.commit();
                }
            }
            else if (blockStorage != null && itemStorage != null)
            {
                // Transfer to or from block
                if (StorageUtil.move(blockStorage, itemStorage, variant -> true, Long.MAX_VALUE, null) > 0)
                {
                    world.playSound(null, context.getClickedPos(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.2f, 1);
                    context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResult.SUCCESS;
                }

                if (StorageUtil.move(itemStorage, blockStorage, variant -> true, Long.MAX_VALUE, null) > 0)
                {
                    world.playSound(null, context.getClickedPos(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.2f, 1);
                    context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        FluidVariant variant = StackStorage.getResource(ItemVariant.of(stack));
        long amount = StackStorage.getAmount(ItemVariant.of(stack));
        if (amount > 0)
        {
            return Component.translatable(getDescriptionId() + ".full", FluidVariantAttributes.getName(variant), MiscUtil.dropletsToMb(amount));
        }
        return Component.translatable(getDescriptionId(stack));
    }

    @Override
    public String getDescriptionId(ItemStack stack)
    {
        return super.getDescriptionId(stack);
    }

    public static class StackStorage implements SingleSlotStorage<FluidVariant>
    {
        public static final String KEY_ROOT = NeepMeat.NAMESPACE + ":storage";
        public static final String KEY_RESOURCE = "resource";
        public static final String KEY_AMOUNT = "amount";
        public static final String KEY_CAPACITY = "capacity";

        protected final ContainerItemContext context;
        protected final long capacity;

        public StackStorage(ContainerItemContext context)
        {
            this.context = context;
            this.capacity = FluidConstants.BUCKET;
        }

        public static FluidVariant getResource(ItemVariant stack)
        {
            CompoundTag nbt = stack.copyOrCreateNbt();

            CompoundTag root = nbt.getCompound(KEY_ROOT);

            return FluidVariant.fromNbt(root.getCompound(KEY_RESOURCE));
        }

        public static long getAmount(ItemVariant stack)
        {
            CompoundTag nbt = stack.copyOrCreateNbt();
            CompoundTag root = nbt.getCompound(KEY_ROOT);
            return root.getLong(KEY_AMOUNT);
        }

        @Override
        public long insert(FluidVariant insertedResource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(insertedResource, maxAmount);

            long amount = getAmount();
            FluidVariant resource = getResource();
            if ((insertedResource.equals(resource) || resource.isBlank()))
            {
                long insertedAmount = Math.min(maxAmount, getCapacity() - amount);

                if (insertedAmount > 0)
                {
                    if (resource.isBlank())
                    {
                        setResource(insertedResource, transaction);
                        setAmount(insertedAmount, transaction);
                    }
                    else
                    {
                        setAmount(amount + insertedAmount, transaction);
                    }

                    return insertedAmount;
                }
            }
            return 0;
        }

        @Override
        public long extract(FluidVariant extractedResource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(extractedResource, maxAmount);

            FluidVariant resource = getResource();
            long amount = getAmount();

            if (extractedResource.equals(resource))
            {
                long extractedAmount = Math.min(maxAmount, amount);

                if (extractedAmount > 0)
                {
                    setAmount(amount - extractedAmount, transaction);

                    if (amount - extractedAmount == 0)
                    {
                        setResource(FluidVariant.BLANK, transaction);
                    }

                    return extractedAmount;
                }
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank()
        {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource()
        {
            return getResource(context.getItemVariant());
        }

        public void setResource(FluidVariant resource, TransactionContext transaction)
        {
            var stack = context.getItemVariant();
            CompoundTag nbt = stack.copyOrCreateNbt();

            CompoundTag root = nbt.getCompound(KEY_ROOT);
            root.put(KEY_RESOURCE, resource.toNbt());
            nbt.put(KEY_ROOT, root);

            context.exchange(ItemVariant.of(stack.getItem(), nbt), 1, transaction);
        }

        @Override
        public long getAmount()
        {
            return getAmount(context.getItemVariant());
        }

        public void setAmount(long amount, TransactionContext transaction)
        {
            var stack = context.getItemVariant();
            CompoundTag nbt = stack.copyOrCreateNbt();

            CompoundTag root = nbt.getCompound(KEY_ROOT);
            root.putLong(KEY_AMOUNT, amount);
            nbt.put(KEY_ROOT, root);

            context.exchange(ItemVariant.of(stack.getItem(), nbt), 1, transaction);
        }

        @Override
        public long getCapacity()
        {
            return capacity;
        }

//        public void setCapacity(long capacity)
//        {
//            NbtCompound root = stack.getOrCreateSubNbt(KEY_ROOT);
//            root.putLong(KEY_CAPACITY, capacity);
//            stack.setSubNbt(KEY_ROOT, root);
//        }

//        @Override
//        protected NbtCompound createSnapshot()
//        {
//            var stack = context.getItemVariant();
//            return stack.copyOrCreateNbt().copy();
//        }
//
//        @Override
//        protected void readSnapshot(NbtCompound snapshot)
//        {
//            var stack = context.getItemVariant();
//            stack.setNbt(snapshot);
//            context.exchange(ItemVariant.of(stack.getItem()), 1);
//        }
    }
}
