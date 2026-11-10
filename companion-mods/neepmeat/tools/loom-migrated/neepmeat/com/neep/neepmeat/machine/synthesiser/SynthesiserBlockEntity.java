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

package com.neep.neepmeat.machine.synthesiser;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.entity.MobEggEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMParticles;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class SynthesiserBlockEntity extends SyncableBlockEntity
{
    public static final float MAX_DISPLACEMENT = -9 / 16f;
    public static final float MIN_DISPLACEMENT = 0;
    public float clientDisplacement;

    protected State state = State.IDLE;
    protected float progress;
    protected int maxProgress;
    protected float increment = 1;
    protected long requiredAmount;
    protected Random random;

    protected SynthesiserStorage storage = new SynthesiserStorage(this);

    public SynthesiserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.random = new Random(pos.asLong());
    }

    public SynthesiserBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.SYNTHESISER, pos, state);
    }

    @Nullable
    protected EntityType<?> getEntityType()
    {
        return storage.getEntityType();
    }

    public void tick()
    {
        if (state == State.IDLE)
        {
            EntityType<?> type = getEntityType();
            MobSynthesisRegistry.Entry entry;
            if (type != null && (entry = MobSynthesisRegistry.get(type)) != null)
            {
                this.progress = 0;
                this.maxProgress = entry.time();
                this.state = State.RUNNING;
                this.requiredAmount = entry.meat();
//                sync();
            }
        }
        else if (state == State.RUNNING)
        {
            float prevProgress = progress;

            progress = Math.min(maxProgress, progress + increment);
            if (progress >= maxProgress && !redstonePowered() && createEntity())
            {
                progress = 0;
                state = State.IDLE;
            }

            if (prevProgress != progress)
            {
                sync();
            }
        }
    }

    public boolean redstonePowered()
    {
        return getBlockState().getValue(SynthesiserBlock.POWERED);
    }

    protected boolean createEntity()
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            MobSynthesisRegistry.Entry entry = MobSynthesisRegistry.get(getEntityType());
            if (entry == null) return false;

            long extracted = storage.meatStorage.extract(NMFluids.MEAT.variant(), entry.meat(), transaction);
            Vec3 entityPos = getEntityPos(worldPosition);
            if (extracted == entry.meat())
            {
                Entity entity = new MobEggEntity(level, getEntityType());
//                Entity entity = getEntityType().create(world);
                entity.setPosRaw(entityPos.x, entityPos.y, entityPos.z);
                entity.setDeltaMovement((random.nextFloat() - 0.5) * 0.1, -0.2, (random.nextFloat() - 0.5) * 0.1);
                level.addFreshEntity(entity);
                ((ServerLevel) level).sendParticles(NMParticles.BLOOD_SHOWER, entityPos.x, worldPosition.getY(), entityPos.z, 50, 0.5, 0, 0.5, 0.02);
                ((ServerLevel) level).sendParticles(NMParticles.BODY_COMPOUND_SHOWER, entityPos.x, worldPosition.getY(), entityPos.z, 50, 0.5, 0.3, 0.5, 0.02);
                transaction.commit();
                return true;
            }
            else transaction.abort();
        }
        return false;
    }

    protected static Vec3 getEntityPos(BlockPos pos)
    {
        return new Vec3(pos.getX() + 0.5, pos.getY() - 1.1, pos.getZ() + 0.5);
    }

    public float getClientDisplacement()
    {
        float delta = Math.min(progress / maxProgress, storage.meatStorage.getAmount() / (float) requiredAmount);
        return maxProgress != 0 ? Mth.lerp(delta, MAX_DISPLACEMENT, MIN_DISPLACEMENT) : MAX_DISPLACEMENT;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("progress", progress);
        nbt.putInt("maxProgress", maxProgress);
        nbt.putFloat("increment", increment);
        nbt.putInt("state", state.ordinal());
        nbt.putLong("requiredAmount", requiredAmount);
        storage.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.progress = nbt.getFloat("progress");
        this.maxProgress = nbt.getInt("maxProgress");
        this.increment = nbt.getFloat("increment");
        this.state = State.values()[nbt.getInt("state")];
        this.requiredAmount = nbt.getLong("requiredAmount");
        storage.readNbt(nbt);
    }

    public Storage<ItemVariant> getItemStorage(Direction ignore)
    {
        return storage.itemStorage;
    }

    public boolean changeEntityType(Player player, ItemStack heldStack)
    {
        // It's tightly coupled, it has every code smell, it looks awful, but I don't care anymore.
        if (!storage.itemStorage.isEmpty())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemStack stack = storage.itemStorage.getAsStack();
                boolean success = player.addItem(stack);

                if (!success)
                {
                    transaction.abort();
                    return false; // Can't continue to insertion as ejection did not take place
                }

                storage.itemStorage.extract(storage.itemStorage.variant, storage.itemStorage.amount, transaction);

                transaction.commit();
                // Proceed to insertion
            }
        }

        if (heldStack.is(NMItems.ESSENTIAL_SALTES))
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long inserted = storage.itemStorage.insert(ItemVariant.of(heldStack), 1, transaction);
                if (inserted != 1)
                {
                    transaction.abort();
                    return false;
                }

                heldStack.shrink(1);
                transaction.commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public void sync()
    {
        super.sync();

        boolean full = !storage.itemStorage.isEmpty();
        if (getBlockState().getValue(SynthesiserBlock.FULL) != full)
        {
            BlockState newState = getBlockState().setValue(SynthesiserBlock.FULL, full);
            level.setBlockAndUpdate(worldPosition, newState);
            setBlockState(newState);
        }
    }

    protected enum State
    {
        IDLE,
        RUNNING;
    }
}