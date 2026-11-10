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

package com.neep.neepmeat.machine.crucible;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.recipe.RenderingBasinRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class CrucibleBlockEntity extends SyncableBlockEntity
{
    protected CrucibleStorageImpl storage;

    public CrucibleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new CrucibleStorageImpl(this);
    }

    public CrucibleBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.CRUCIBLE, pos, state);
    }

    public CrucibleStorageImpl getStorage()
    {
        return storage;
    }

    public CombinedStorage<FluidVariant, SingleVariantStorage<FluidVariant>> getOutput()
    {
        List<SingleVariantStorage<FluidVariant>> storages = new ArrayList<>();
        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().equals(Direction.Axis.Y))
                continue;

            BlockPos offset = worldPosition.relative(direction);
            if (level.getBlockEntity(offset) instanceof AlembicBlockEntity be && be.getBlockState().getValue(FatCollectorBlock.FACING) == direction)
            {
                storages.add(be.getStorage(null));
            }
        }
        return new CombinedStorage<>(storages);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        storage.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        storage.readNbt(nbt);
    }

    public void receiveItemEntity(ItemEntity entity)
    {
        ItemStack stack = entity.getItem();
        ItemVariant variant = ItemVariant.of(stack);
        try (Transaction transaction = Transaction.openOuter())
        {
            long decrement = storage.itemStorage.insert(variant, stack.getCount(), transaction);
            stack.shrink((int) decrement);
            transaction.commit();
        }
    }

    public long processItem(long maxAmount, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            RenderingBasinRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.RENDERING, storage).orElse(null);
            if (recipe == null)
                recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.ORE_FAT_RENDERING, storage).orElse(null);

            ItemVariant processItem;
            if (recipe != null && (processItem = recipe.takeInputs(storage, (int) maxAmount, inner)) != null)
            {
                inner.commit();
                transaction.addCloseCallback((t, r) -> MeatlibStorageUtil.closeCallback(t, r, () ->
                    spawnParticles((ServerLevel) level, worldPosition, processItem, 20)));
                return maxAmount;
            }
            inner.abort();
        }

        return 0;
    }

    public static void spawnParticles(ServerLevel world, BlockPos pos, ItemVariant item, int amount)
    {
        ItemStack stack = item.toStack();
        Random random = new Random();
        for (int p = 0; p < amount; ++p)
        {
            double i = random.nextGaussian(0.5, 0.2);
            double j = random.nextFloat();
            double k = random.nextGaussian(0.5, 0.2);
            world.sendParticles(new ItemParticleOption(NMParticles.COLOURED_ITEM, stack), pos.getX() + i, pos.getY() + 1, pos.getZ() + k, 1, 0, 0.2, 0, 0.02);
        }
        for (int p = 0; p < amount; ++p)
        {
            double i = random.nextGaussian(0.5, 0.2);
            double j = random.nextFloat();
            double k = random.nextGaussian(0.5, 0.2);
            world.sendParticles(ParticleTypes.SPLASH, pos.getX() + i, pos.getY() + 1, pos.getZ() + k, 1, 0, 0.2, 0, 0.02);
        }

        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 1f, 0.8f);
    }
}
