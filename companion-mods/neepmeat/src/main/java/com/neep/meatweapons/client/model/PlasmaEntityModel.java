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

package com.neep.meatweapons.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatweapons.entity.FusionBlastEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PlasmaEntityModel extends EntityModel<FusionBlastEntity>
{
	private final ModelPart bb_main;

	public PlasmaEntityModel(ModelPart modelPart)
	{
//		textureWidth = 32;
//		textureHeight = 32;

//		bb_main = new ModelPart(this);
		bb_main = modelPart.getChild(PartNames.CUBE);
//		bb_main.rotate(new MatrixStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(24f)));
//		bb_main.setTextureOffset(0, 0).addCuboid(-1.5F, -3.0F, 0.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
//		bb_main.cub(-1.5F, -3.0F, 0.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setAngles(FusionBlastEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{

	}

	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha)
	{
		bb_main.render(matrices, vertices, light, overlay);
	}

	public static LayerDefinition getTexturedModelData()
	{
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild(PartNames.CUBE, CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -1F, -1F, 2F, 2F, 2F), PartPose.offset(0F, 0F, 0F));
		return LayerDefinition.create(modelData, 32, 32);
	}

}
