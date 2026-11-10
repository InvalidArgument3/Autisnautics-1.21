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

package com.neep.neepmeat.api.big_block;

import com.google.common.collect.Lists;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BigBlockStructureEntity extends SyncableBlockEntity
{
    @Nullable protected BlockPos controllerPos;
    protected List<ResourceLocation> apis = Lists.newArrayList();
    @Nullable private BlockApiCache<Void, Void> cache;

    public BigBlockStructureEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void setController(@Nullable BlockPos pos)
    {
        this.controllerPos = pos;
    }

    public void enableApi(BlockApiLookup<?, ?> api)
    {
        apis.add(api.getId());
    }

    public VoxelShape translateShape(VoxelShape outlineShape)
    {
        if (controllerPos == null)
            return Shapes.block();

        BlockPos relative = worldPosition.subtract(controllerPos);
        return outlineShape.move(-relative.getX(), -relative.getY(), -relative.getZ());
    }

    public VoxelShape translateChopShape(VoxelShape outlineShape)
    {
        if (controllerPos == null)
            return Shapes.block();

        return Shapes.joinUnoptimized(translateShape(outlineShape), Shapes.block(), BooleanOp.AND);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        if (controllerPos != null)
            nbt.put("controller_pos", NbtUtils.writeBlockPos(controllerPos));

        if (!apis.isEmpty())
        {
            ListTag list = new ListTag();
            for (var id : apis)
            {
                list.add(StringTag.valueOf(id.toString()));
            }
            nbt.put("apis", list);
        }
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        if (nbt.contains("controller_pos"))
        {
            this.controllerPos = NbtUtils.readBlockPos(nbt.getCompound("controller_pos"));
        }
        else
            this.controllerPos = null;

        apis.clear();
        if (nbt.contains("apis"))
        {
            ListTag list = nbt.getList("apis", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); ++i)
            {
                String string = list.getString(i);
                apis.add(ResourceLocation.tryParse(string));
            }
        }
    }

    @Nullable
    public BlockPos getControllerPos()
    {
        return controllerPos;
    }

    @Nullable
    protected <T extends BlockEntity> T getControllerBE(Class<T> clazz)
    {
        if (level instanceof ServerLevel serverWorld)
        {
            if (controllerPos == null)
            {
                return null;
            }
            else if (cache == null)
            {
                cache = BlockApiCache.create(MeatLib.VOID_LOOKUP, serverWorld, controllerPos);
            }

            @Nullable BlockEntity be = cache.getBlockEntity();
            if (clazz.isInstance(be))
                return (T) be;
            else
                return null;
        }

        return null;
    }
}
