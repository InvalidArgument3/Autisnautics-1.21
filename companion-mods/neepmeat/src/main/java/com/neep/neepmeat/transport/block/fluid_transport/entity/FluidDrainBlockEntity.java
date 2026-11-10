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

package com.neep.neepmeat.transport.block.fluid_transport.entity;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.FluidBuffer;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.fluid.PersistentFluid;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.neep.neepmeat.transport.block.fluid_transport.machine.TankBlockEntity.showContents;

@SuppressWarnings("UnstableApiUsage")
public class FluidDrainBlockEntity extends SyncableBlockEntity implements FluidBuffer.FluidBufferProvider
{
    private int transferCooldown = 0;
    protected final WritableSingleFluidStorage buffer;

    public FluidDrainBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.FLUID_DRAIN, pos, state);
        this.buffer = new WritableSingleFluidStorage(8 * FluidConstants.BUCKET, this::setChanged);
    }

    public boolean needsCooldown()
    {
        return this.transferCooldown > 0;
    }

    private void setCooldown(int cooldown)
    {
        this.transferCooldown = cooldown;
    }

    @Override
    public WritableSingleFluidStorage getBuffer(Direction direction)
    {
        return buffer;
    }

    public boolean grabFluid(Level world, BlockPos pos, BlockState state)
    {
        BlockPos upPos = pos.above();
        BlockPos sourcePos = findFluid(world, upPos, state);

        if (sourcePos != null)
        {
            FluidState fluidState = world.getFluidState(sourcePos);
            try (Transaction transaction = Transaction.openOuter())
            {
                BlockState fluidBlockState = world.getBlockState(sourcePos); // Don't change this to fluidstate.getBlockState. That will ignore waterlogged blocks.
                long targetAmount = fluidState.getAmount() * FluidConstants.BLOCK / 8;
                long transferred = this.buffer.insert(FluidVariant.of(((FlowingFluid) fluidState.getType()).getSource()), targetAmount, transaction);

                if (transferred >= targetAmount)
                {
                    if (fluidBlockState.getBlock() instanceof BucketPickup drainable)
                    {
                        drainable.pickupBlock(world, sourcePos, fluidBlockState);
                        transaction.commit();
                    }
                    else if (fluidBlockState.getBlock() instanceof LiquidBlock)
                    {
                        // Handle non-drainable fluids. Hopefully the above check will prevent deletion of non-fluid blocks.
                        world.setBlockAndUpdate(sourcePos, Blocks.AIR.defaultBlockState());
                        transaction.commit();
                    }
                }
            }
       }
        return true;
    }

    protected BlockPos findFluid(Level world, BlockPos origin, BlockState state)
    {
        FluidState originState = world.getFluidState(origin);
        if (originState.isEmpty())
        {
            return null;
        }
        else if (originState.isSource())
        {
            return origin;
        }
        else if (originState.getType() instanceof PersistentFluid persistentFluid)
        {
            return origin;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> frontier = new ArrayDeque<>();
        frontier.add(origin);

        for (int i = 0; i < 8; ++i)
        {
            while (!frontier.isEmpty())
            {
                BlockPos current = frontier.poll();
                visited.add(current);
                for (Direction direction : Direction.values())
                {
                    BlockPos newPos = current.relative(direction);
                    FluidState fluidState = world.getFluidState(newPos);

                    if (visited.contains(newPos) || fluidState.isEmpty())
                        continue;

                    if (fluidState.isSource())
                    {
                        return newPos;
                    }
                    else
                    {
                        frontier.add(newPos);
                    }
                }
            }
        }
        return null;
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, FluidDrainBlockEntity blockEntity)
    {
        --blockEntity.transferCooldown;
        if (!blockEntity.needsCooldown())
        {
            blockEntity.setCooldown(16);
            blockEntity.grabFluid(world, pos, state);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        buffer.toNbt(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        buffer.readNbt(tag);
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        if (WritableSingleFluidStorage.handleInteract(buffer, level, player, hand))
        {
            return true;
        }
        else if (!level.isClientSide())
        {
            showContents((ServerPlayer) player, level, getBlockPos(), getBuffer(null));
            return true;
        }
        return true;
    }
}
