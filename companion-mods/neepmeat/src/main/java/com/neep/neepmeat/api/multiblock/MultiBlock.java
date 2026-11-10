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

package com.neep.neepmeat.api.multiblock;

import net.minecraft.core.HolderLookup;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiBlock
{
    class Entity extends BlockEntity
    {
        protected BlockPos controllerPos;

        public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        @Override
        public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
        {
            super.loadAdditional(nbt, registries);
            CompoundTag nbt2 = nbt.getCompound("controller");
            if (nbt2 != null)
            {
                setController(NbtUtils.readBlockPos(nbt2));
            }
            else
            {
                setController(null);
            }
        }

        public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
        {
            super.saveAdditional(nbt, registries);
            if (hasController())
            {
                nbt.put("controller", NbtUtils.writeBlockPos(controllerPos));
            }
        }

        public BlockPos getControllerPos()
        {
            return controllerPos;
        }

        public ControllerBlockEntity getController()
        {
            if (getControllerPos() != null && getLevel().getBlockEntity(getControllerPos()) instanceof ControllerBlockEntity be)
            {
                return be;
            }
            return null;
        }

        public void onParentBreak(ServerLevel world)
        {
            if (hasController())
                getController().componentBroken(world);
        }

        public void setController(BlockPos controllerPos)
        {
            this.controllerPos = controllerPos;
            this.setChanged();
        }

        public boolean hasController()
        {
            return getController() != null;
        }

    }
}
