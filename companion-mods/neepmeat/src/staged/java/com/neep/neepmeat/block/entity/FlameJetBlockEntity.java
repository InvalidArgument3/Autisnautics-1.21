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

package com.neep.neepmeat.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.Burner;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.block.FlameJetBlock;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankBlockEntity;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FlameJetBlockEntity extends SyncableBlockEntity implements Burner
{
    public static final long BURN_AMOUNT = 27;

    WritableSingleFluidStorage storage = new WritableSingleFluidStorage(4 * FluidConstants.BUCKET, this::sync)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.isOf(Fluids.LAVA);
        }
    };

    private boolean consumedByTransducer;
    private boolean tryRunning;

    public FlameJetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public FlameJetBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FLAME_JET, pos, state);
    }

    public Storage<FluidVariant> getFluidStorage(Direction direction)
    {
        Direction facing = getBlockState().getValue(FlameJetBlock.FACING);
        return direction != facing ? storage : null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putBoolean("powered", tryRunning);
        storage.toNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.tryRunning = nbt.getBoolean("powered");
        storage.readNbt(nbt);
    }

    public boolean isPowered()
    {
        return tryRunning;
    }

    public boolean canBurn()
    {
        return isPowered() && storage.getResource().isOf(Fluids.LAVA) && storage.getAmount() >= BURN_AMOUNT;
    }

    public void update()
    {
        tryRunning = getBlockState().getValue(FlameJetBlock.MODE).active(level.hasNeighborSignal(worldPosition));
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, FlameJetBlockEntity be)
    {
        if (state.getValue(FlameJetBlock.RUNNING) != be.canBurn())
        {
            world.setBlock(pos, state.cycle(FlameJetBlock.RUNNING), Block.UPDATE_CLIENTS);
        }

        if (be.canBurn())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                be.storage.extract(be.storage.getResource(), BURN_AMOUNT, transaction);
                transaction.commit();
            }

            if (!be.consumedByTransducer)
            {
                Direction facing = be.getBlockState().getValue(FlameJetBlock.FACING);
                AABB box = new AABB(pos).inflate(facing.getStepX() * 6, facing.getStepY() * 6, facing.getStepZ() * 6);
                List<LivingEntity> entityList = world.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, e -> true);
                entityList.forEach(entity ->
                {
                    entity.setRemainingFireTicks(10);
                    entity.hurt(world.damageSources().onFire(), 1.5f);
                });
                }
        }

        be.consumedByTransducer = false;
    }

    public static void clientTick(Level world, BlockPos pos, BlockState blockState, FlameJetBlockEntity be)
    {
        Direction facing = blockState.getValue(FlameJetBlock.FACING);
        if (!(facing == Direction.UP && world.getBlockState(pos.above(2)).is(NMBlocks.TRANSDUCER)))
        {
            var random = world.getRandom();
            if (be.canBurn())
            {
                for (int i = 0; i < 3; ++i)
                {
                    world.addParticle(ParticleTypes.FLAME,
                            pos.getX() + 0.5 + (random.nextFloat() - 0.5) / 2,
                            pos.getY() + 0.5 + (random.nextFloat() - 0.5) / 2,
                            pos.getZ() + 0.5 + (random.nextFloat() - 0.5) / 2,
                            facing.getStepX() / 2f, facing.getStepY() / 2f, facing.getStepZ() / 2f);
                }
            }
        }
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        if (WritableSingleFluidStorage.handleInteract(storage, level, player, hand))
        {
            return true;
        }
        else if (!level.isClientSide())
        {
            TankBlockEntity.showContents((ServerPlayer) player, level, getBlockPos(), storage);
            return true;
        }
        return false;
    }

    @Override
    public double getOutputPower()
    {
        consumedByTransducer = true;
        if (canBurn())
        {
            // Equivalent energy output of a bucket of lava in a transducer
            int energyPerBucket = 150 * 20000;
            double energyPerDroplet = (double) energyPerBucket / FluidConstants.BUCKET;
            return BURN_AMOUNT * energyPerDroplet / 2; // Divide by two because 1000 is too much
        }

        return 0;
    }

    public @Nullable Burner getBurner(Direction direction)
    {
        if (direction == getBlockState().getValue(FlameJetBlock.FACING))
            return this;

        return null;
    }
}
