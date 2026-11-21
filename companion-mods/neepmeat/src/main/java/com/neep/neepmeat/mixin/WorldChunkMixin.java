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

import com.neep.neepmeat.transport.event.WorldChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelChunk.class)
public class WorldChunkMixin
{
    @Inject(method = "loadEntities", at = @At(value = "TAIL"))
    void onLoadEntities(CallbackInfo ci)
    {
        WorldChunkEvents.LOAD_ENTITIES.invoker().load((LevelChunk) (Object) this);
    }

    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;setWorld(Lnet/minecraft/world/World;)V"))
    void onSetBlockEntity(BlockEntity blockEntity, CallbackInfo ci)
    {
        WorldChunkEvents.BE_SET_WORLD.invoker().apply((LevelChunk) (Object) this, blockEntity);
    }

    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"))
    void onRemoveOldBlockEntity(BlockEntity blockEntity, CallbackInfo ci)
    {
        WorldChunkEvents.BE_MANUAL_REMOVE.invoker().apply((LevelChunk) (Object) this, blockEntity);
    }

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, BlockEntity blockEntity)
    {
        WorldChunkEvents.BE_MANUAL_REMOVE.invoker().apply((LevelChunk) (Object) this, blockEntity);
    }
}
