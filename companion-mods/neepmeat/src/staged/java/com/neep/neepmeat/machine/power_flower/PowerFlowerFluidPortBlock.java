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

package com.neep.neepmeat.machine.power_flower;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PowerFlowerFluidPortBlock extends BaseBlock implements EntityBlock, PowerFlower
{
    public PowerFlowerFluidPortBlock(RegistrationContext ctx, ItemSettings block, Properties settings)
    {
        super(ctx, block, settings.noOcclusion());
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.POWER_FLOWER_FLUID_PORT.create(pos, state);
    }

    public static class PFPortBlockEntity extends SyncableBlockEntity
    {
        @Nullable private BlockPos controllerPos;
        @Nullable private BlockApiCache<Void, Void> cache;

        private final FluidPump fluidPump = FluidPump.of(0.5f, () -> AcceptorModes.PULL);

        public PFPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        @Override
        public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
        {
            super.saveAdditional(nbt, registries);
            if (controllerPos != null)
                nbt.put("parent_pos", NbtUtils.writeBlockPos(controllerPos));
        }

        @Override
        public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
        {
            super.loadAdditional(nbt, registries);
            if (nbt.contains("parent_pos"))
                this.controllerPos = NbtUtils.readBlockPos(nbt.getCompound("parent_pos"));
        }

        @Nullable
        public Storage<FluidVariant> getStorage(Direction direction)
        {
            var controller = getControllerBE();
            if (controller != null)
            {
                return controller.getFluidStorage();
            }
            return Storage.empty();
        }

        @Nullable
        public FluidPump getPump(Direction direction)
        {
            return fluidPump;
        }

        @Nullable
        private PowerFlowerControllerBlockEntity getControllerBE()
        {
            if (level instanceof ServerLevel serverWorld)
            {
                if (controllerPos == null)
                {
                    cache = null;
                    return null;
                }
                else if (cache == null)
                {
                    cache = BlockApiCache.create(MeatLib.VOID_LOOKUP, serverWorld, controllerPos);
                }

                if (cache.getBlockEntity() instanceof PowerFlowerControllerBlockEntity controller)
                {
                    return controller;
                }
                else
                {
                    // Invalidate of the controller has been removed.
                    controllerPos = null;
                    cache = null;
                    return null;
                }
            }

            return null;
        }

        public void setController(BlockPos controller)
        {
            if (!Objects.equals(this.controllerPos, controller))
            {
                this.controllerPos = controller;
                level.blockUpdated(getBlockPos(), getBlockState().getBlock());
            }
        }
    }
}
