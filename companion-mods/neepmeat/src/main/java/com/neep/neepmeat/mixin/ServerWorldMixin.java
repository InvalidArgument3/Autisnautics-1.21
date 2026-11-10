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

package com.neep.neepmeat.mixin;

import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.enlightenment.EnlightenmentEventManager;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import com.neep.neepmeat.transport.event.WorldChunkEvents;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.PipeCacheImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements IServerWorld
{
    @Shadow public abstract void removePlayer(ServerPlayer player, Entity.RemovalReason reason);

    @Shadow public abstract DimensionDataStorage getPersistentStateManager();

    @Unique public PipeCacheImpl neepmeat$itemNetwork = new PipeCacheImpl((ServerLevel) (Object) this);
    @Unique public EnlightenmentEventManager neepmeat$enlightenmentEventManager = new EnlightenmentEventManager();

//    @Unique private BloodNetworkManager neepmeat$bloodNetworkManager = new BloodNetworkManager((ServerWorld) (Object) this);
    @Unique private final LazySupplier<BloodNetworkManager> neepmeat$bloodNetworkManager = LazySupplier.of( () ->
        new BloodNetworkManager((ServerLevel) (Object) this));

    @Inject(method = "unloadEntities", at = @At(value = "HEAD"))
    public void onUnloadEntities(LevelChunk chunk, CallbackInfo ci)
    {
        WorldChunkEvents.UNLOAD_ENTITIES.invoker().load(chunk);
    }

    @Override
    public PipeCacheImpl getItemNetwork()
    {
        return neepmeat$itemNetwork;
    }

    @Override
    public EnlightenmentEventManager getEnlightenmentEventManager()
    {
        return neepmeat$enlightenmentEventManager;
    }

    public BloodNetworkManager getBloodNetworkManager() { return neepmeat$bloodNetworkManager.get(); }

//    @Override
//    public DistributorRouting newpmeat$getDistributorRouting()
//    {
//        return IServerWorld.super.newpmeat$getDistributorRouting();
//    }
}
