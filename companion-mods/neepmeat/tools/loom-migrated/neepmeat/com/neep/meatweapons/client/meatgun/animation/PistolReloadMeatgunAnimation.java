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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PistolReloadMeatgunAnimation extends AnimatedAction<MeatgunComponent, PistolReloadMeatgunAnimation> implements MeatgunAnimation
{
    private int length = 10;

    private final Sequence<PistolReloadMeatgunAnimation> down = new Sequence<>()
    {
        @Override
        public void tick(PistolReloadMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            if (counter > 5)
            {
                int waitTime = length - 10;

                if (waitTime != 0)
                    setSequence(wait);
                else
                    setSequence(up);
            }
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float delta = (counter + tickDelta) / 5;
            matrices.translate(0, -2 * delta, 0);

            return true;
        }
    };

    private final Sequence<PistolReloadMeatgunAnimation> wait = new Sequence<>()
    {
        @Override
        public void tick(PistolReloadMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            int waitTime = length - 10;
            if (counter >= waitTime)
                setSequence(up);
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            matrices.translate(0, -2, 0);
            return true;
        }
    };

    private final Sequence<PistolReloadMeatgunAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(PistolReloadMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            if (counter >= 5)
                markFinished();
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float delta = 1 - (counter + tickDelta) / 6;
            matrices.translate(0, -2 * delta, 0);
            return true;
        }
    };

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);

        if (buf != null)
            length = buf.readVarInt();
        else
            length = 10;

        setSequence(down);
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return true;
    }
}
