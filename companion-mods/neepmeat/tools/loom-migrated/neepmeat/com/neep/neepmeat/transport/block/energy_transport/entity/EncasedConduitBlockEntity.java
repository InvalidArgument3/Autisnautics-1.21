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

package com.neep.neepmeat.transport.block.energy_transport.entity;

import com.neep.meatlib.blockentity.BlockEntityClientSerializable;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.EncasedBlockEntity;
import com.neep.neepmeat.transport.block.energy_transport.EncasedVascularConduitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EncasedConduitBlockEntity extends VascularConduitBlockEntity implements EncasedBlockEntity, BlockEntityClientSerializable
{
    private BlockState camoState = Blocks.AIR.defaultBlockState();
    private VoxelShape cachedShape = null;

    public EncasedConduitBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.ENCASED_VASCULAR_CONDUIT, pos, state);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        var nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        BlockState camoState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("camo_state"));
        if (camoState != this.camoState)
        {
            this.camoState = camoState;
            this.cachedShape = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("camo_state", NbtUtils.writeBlockState(camoState));
    }

    @Override
    public BlockState getCamoState()
    {
        return camoState;
    }

    @Override
    public void setCamoState(BlockState camoState)
    {
        this.camoState = camoState;
        cachedShape = null;
        sync();
    }

    public void onNeighbourUpdate()
    {
        cachedShape = null;
    }

    @Override
    public VoxelShape getCamoShape()
    {
        if (cachedShape == null)
        {
            cachedShape = Shapes.block();
            if (getCamoState().is(getBlockState().getBlock()) || getCamoState().isAir())
            {
                return cachedShape;
            }
            else
            {
                if (getBlockState().getBlock() instanceof EncasedVascularConduitBlock block)
                {
                    cachedShape = block.getPipeOutlineShape(getBlockState(), level, getBlockPos());
                }

                VoxelShape camoShape = camoState.getShape(level, worldPosition);
                cachedShape = Shapes.join(Shapes.or(camoShape, cachedShape), Shapes.block(), BooleanOp.AND);
            }
        }
        return cachedShape;
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        load(nbt);
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        saveAdditional(nbt);
    }

    @Override
    public void onReceiveNbt(CompoundTag nbt)
    {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
    }
}
