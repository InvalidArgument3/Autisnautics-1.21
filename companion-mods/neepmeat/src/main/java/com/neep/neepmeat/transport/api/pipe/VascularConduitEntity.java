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

package com.neep.neepmeat.transport.api.pipe;

import com.google.common.collect.Maps;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.blood_network.BloodNetwork;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public interface VascularConduitEntity extends VascularConduitEntityProvider
{
    BlockApiLookup<VascularConduitEntity, Void> LOOKUP = BlockApiLookup.get(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "vascular_conduit_entity"),
            VascularConduitEntity.class, Void.class);

    Map<BlockEntityType<?>, Function<? extends BlockEntity, VascularConduitEntity>> MAP = Maps.newHashMap();

    static <T extends BlockEntity> void registerPersistentNetwork(Function<T, VascularConduitEntity> function, BlockEntityType<T> type)
    {
        MAP.put(type, function);
    }

    @Nullable
    static <T extends BlockEntity> VascularConduitEntity find(T blockEntity)
    {
        var function = MAP.get(blockEntity.getType());
        if (function != null)
        {
            // This may be a little naughty
            Function<T, VascularConduitEntity> casted = (Function<T, VascularConduitEntity>) function;
            return casted.apply(blockEntity);
        }
        return null;
    }

    static VascularConduitEntity find(Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof VascularConduitEntity entity)
            return entity;

        return LOOKUP.find(world, pos, null);
    }

    @Nullable
    BloodNetwork getNetwork();

    void setNetwork(@Nullable BloodNetwork network);

    default BlockPos getBlockPos()
    {
        if (this instanceof BlockEntity be)
            return be.getBlockPos();

        throw new NotImplementedException();
    }

    @Override
    default VascularConduitEntity get() { return this; }

    enum UpdateReason
    {
        ADDED,
        REMOVED,
        CHANGED
    }
}
