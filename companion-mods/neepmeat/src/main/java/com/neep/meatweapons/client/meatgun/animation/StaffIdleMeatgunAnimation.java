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
import com.mojang.math.Axis;
import com.neep.meatweapons.component.MeatgunComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class StaffIdleMeatgunAnimation extends AnimatedAction<MeatgunComponent, StaffIdleMeatgunAnimation> implements MeatgunAnimation
{
    private final Sequence<StaffIdleMeatgunAnimation> idle = new Sequence<>()
    {
        @Override
        public void tick(StaffIdleMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            apply(matrices, 0, 0, 0, leftHand);
            return true;
        }
    };

    public static void apply(PoseStack matrices, float xRot, float yRot, float zRot, boolean leftHand)
    {
        float i = leftHand ? -1 : 1;
        float d = leftHand ? 16 / 16f : 0;
        matrices.translate(0, -4 / 16f, 0 / 16f);
        matrices.translate(d, 0, 1.5);
        matrices.mulPose(Axis.YP.rotationDegrees(i * (5 + yRot)));
        matrices.mulPose(Axis.XP.rotationDegrees(xRot));
        matrices.translate(-d, 0, -1.5);

        matrices.translate(d, 0, d);
        matrices.mulPose(Axis.ZP.rotationDegrees(i * (-20 + zRot)));
        matrices.translate(-d, 0, -d);
    }

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);
        setSequence(idle);
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return true;
    }
}
