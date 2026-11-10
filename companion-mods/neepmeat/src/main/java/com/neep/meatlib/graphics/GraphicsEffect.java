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

package com.neep.meatlib.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;

@OnlyIn(Dist.CLIENT)
public interface GraphicsEffect
{
    GraphicsEffectType EMPTY = new GraphicsEffectType();

    void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta);

    default void renderAfter(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta) {}

    void tick();

    boolean isRemoved();

    Level getWorld();
}
