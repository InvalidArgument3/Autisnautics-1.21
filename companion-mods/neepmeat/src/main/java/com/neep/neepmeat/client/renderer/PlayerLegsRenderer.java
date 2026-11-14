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

package com.neep.neepmeat.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.client.NMImplantsClient;
import com.neep.neepmeat.client.model.entity.SpiderLegsRenderer;
import com.neep.neepmeat.implant.entity.SpiderLegsImplant;
import com.neep.neepmeat.implant.entity.ImplantRenderingHelper;
import com.neep.neepmeat.mixin.client.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PlayerLegsRenderer
{
    // Evaluation order:
    // GameRenderer::tick
    // - Camera::updateEyePos - uses last value of modified eye height with next value of player position
    // Entity::tick
    // - SpiderLegsImplant::tick
    // - Movement logic - Updates player position
    // - SpiderLegsImplant::endTick

    // Independently smoothing camera Y will not work as step increments will cause momentary jerking
    // Fixed by switching off smoothing when a large error is detected

    public static void init()
    {
    }

    public static void onRender(AbstractClientPlayer player, float what, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int i, PlayerRenderer renderer)
    {
        SpiderLegsImplant implant = ((ImplantRenderingHelper) player).neepmeat$getSpiderLegs();
        if (implant != null)
        {
            ResourceLocation id = implant.getModelId();
            SpiderLegsRenderer<Player> model = (SpiderLegsRenderer<Player>) NMImplantsClient.getSpiderLegsRenderer(id); // EEK
            model.render(player, implant, matrices, tickDelta, vcp, i);
        }
    }

    public static void onUpdateCamera(Camera camera, BlockGetter area, Player player, boolean thirdPerson, boolean inverseView, float tickDelta)
    {
        SpiderLegsImplant implant = ((ImplantRenderingHelper) player).neepmeat$getSpiderLegs();
        Vec3 pos = camera.getPosition();
        if (implant != null)
        {
//            double y = MathHelper.lerp(tickDelta, player.prevY, player.getY()) // Player abs Y is not smoothed
//                    + MathHelper.lerp(tickDelta, implant.prevSmoothEyeHeight, implant.smoothEyeHeight);

            CameraAccessor accessor = (CameraAccessor) camera;
            accessor.callSetPos(pos.x,
                    Mth.lerp(tickDelta, player.yo, player.getY()) // Player abs Y is not smoothed
                            + Mth.lerp(tickDelta, implant.prevSmoothEyeHeight, implant.smoothEyeHeight), // Smooths sneaking and Y adjustment due to legs
                    pos.z);
        }
    }

    public static void onUpdateEyeHeight(Camera camera, Player player)
    {
    }

    public static void onSetPose(AbstractClientPlayer player, PlayerRenderer renderer)
    {
        SpiderLegsImplant implant = ((ImplantRenderingHelper) player).neepmeat$getSpiderLegs();
        if (implant != null)
        {
            var model = renderer.getModel();
            model.leftLeg.visible = false;
            model.rightLeg.visible = false;
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }
    }
}
