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

package com.neep.meatweapons.client.meatgun.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.component.MeatgunComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public interface MeatgunAnimation
{
    void start(@Nullable FriendlyByteBuf buf);

    boolean canStop(MeatgunAnimation replace);

    boolean finished();

    void tick(MeatgunComponent component);

    boolean applyRender(PoseStack matrices, float tickDelta, boolean leftHand);

    MeatgunAnimation EMPTY = new MeatgunAnimation()
    {
        @Override
        public void start(@Nullable FriendlyByteBuf buf) { }

        @Override
        public boolean canStop(MeatgunAnimation replace)
        {
            return true;
        }

        @Override
        public boolean finished()
        {
            return false;
        }

        @Override
        public void tick(MeatgunComponent component)
        {

        }

        @Override
        public boolean applyRender(PoseStack matrices, float tickDelta, boolean leftHand)
        {
            return true;
        }
    };

}
