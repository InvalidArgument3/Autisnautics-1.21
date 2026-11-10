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
import com.neep.meatlib.network.PacketBufUtil;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class HalberdGrabAnimation extends AnimatedAction<MeatgunComponent, HalberdGrabAnimation> implements MeatgunAnimation
{
    private Vec3 offset = Vec3.ZERO;

    private final Sequence<HalberdGrabAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(HalberdGrabAnimation parent, MeatgunComponent component, int counter)
        {
            if (counter > 60)
                markFinished();
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float playerYawDeg = Mth.wrapDegrees(Minecraft.getInstance().player.getViewYRot(tickDelta));
            float targetYawDeg = NMMaths.rectToPol(offset).y;

            matrices.translate(0 / 16f, -16 / 16f, 0);

            float angle = Mth.wrapDegrees((float) (playerYawDeg - targetYawDeg));

            float zOff = 4 / 16f;
            float yOff = 6 / 16f;
            matrices.translate(-10 / 16f, -10 / 16f, 0);
//            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            matrices.mulPose(Axis.YP.rotationDegrees(angle));
            matrices.mulPose(Axis.XP.rotationDegrees(-90));
            matrices.translate(0 / 16f, 16 / 16f, 0);
            return false;
        }
    };

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);

        if (buf != null)
            offset = PacketBufUtil.readVec3d(buf);
        else
            offset = Vec3.ZERO;

        setSequence(up);
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return false;
    }
}
