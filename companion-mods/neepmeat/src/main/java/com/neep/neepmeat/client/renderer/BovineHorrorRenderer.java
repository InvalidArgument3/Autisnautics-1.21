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
import com.mojang.math.Axis;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.GenericModel;
import com.neep.neepmeat.entity.bovine_horror.BovineHorrorEntity;
import com.neep.neepmeat.util.SightUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BovineHorrorRenderer extends GeoEntityRenderer<BovineHorrorEntity>
{
    public BovineHorrorRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new GenericModel<>(NeepMeat.NAMESPACE,
                "geo/bovine_horror.geo.json",
                "textures/entity/bovine_horror.png",
                "animations/bovine_horror.animation.json"
                ));

        this.shadowRadius = 1.5F;
    }

    public void render(BovineHorrorEntity animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        animatable.prevVisibility = (float) Mth.lerp(0.1, animatable.prevVisibility, animatable.getVisibility());

        poseStack.pushPose();
//        if (animatable.isPhase2())
//        {
//            Vec3d vel = animatable.getVelocity();
//            double hlen = vel.horizontalLength();
//
//            Vector3f hor = new Vector3f((float) vel.x, 0, (float) vel.z);
//            hor.normalize();
//            hor.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(90));
//            poseStack.multiply(hor.getDegreesQuaternion((float) (hlen * 40)));
//        }

        super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    public Color getRenderColor(BovineHorrorEntity animatable, float partialTick, int packedLight)
    {
        float alpha = animatable.prevVisibility;
        if (SightUtil.canPlayerSee(Minecraft.getInstance().player))
        {
            alpha = 1;
        }
        return Color.ofRGBA(1, 1, 1, alpha);
    }

    public RenderType getRenderType(BovineHorrorEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick)
    {
        if (SightUtil.canPlayerSee(Minecraft.getInstance().player))
        {
            return RenderType.entityCutout(texture);
        }
        return RenderType.entityTranslucent(texture);
    }

    protected void applyRotations(BovineHorrorEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick)
    {
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - rotationYaw));
    }
}
