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

package com.neep.neepmeat.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.api.block.PlacementPreview;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PlacementPreviewRegistry
{
    private static final Map<PlacementPreview, Supplier<Handler>> constructors = new IdentityHashMap<>();
    private static final Map<PlacementPreview, Handler> ACTIVE = new IdentityHashMap<>();

    public static void register(PlacementPreview preview, Supplier<Handler> handler)
    {
        constructors.put(preview, handler);
    }

    public static Handler get(PlacementPreview preview)
    {
        var handler = ACTIVE.get(preview);
        if (handler == null)
        {
            var constructor = constructors.get(preview);
            if (constructor != null)
            {
                handler = constructor.get();
                ACTIVE.put(preview, handler);
            }
            else
            {
                throw new IllegalStateException("no handler registered for " + preview);
            }
        }

        return handler;
    }

    public interface Handler
    {
        @Nullable
        /// hitResult represents usage, placementContext represents placement.
        BlockPlacementHelper.Result tick(Minecraft client, BlockPos pos, boolean canPlace, BlockHitResult hitResult, BlockPlaceContext placementContext);
        boolean render(Minecraft client, BlockPlacementHelper.Result result, BlockPos pos, PoseStack matrices, MultiBufferSource consumers, float tickDelta);

    }
}
