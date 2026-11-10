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

package com.neep.neepmeat.client.model.entity;

import com.neep.neepmeat.entity.hound.HoundEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class HoundEntityModel extends HierarchicalModel<HoundEntity>
{

    public static final String MAIN = "main";
    public static final String EXTREMITY_1 = "extremity_1";
    public static final String EXTREMITY_2 = "extremity_2";
    public static final String EXTREMITY_3 = "extremity_3";

    protected ModelPart root;

    public static LayerDefinition getTexturedModelData()
    {
        MeshDefinition modelData = new MeshDefinition();

        PartDefinition root = modelData.getRoot();

        root.addOrReplaceChild(MAIN, CubeListBuilder.create()
                .texOffs(0, 0)
//                .cuboid(-8.0F, 0.0F, -8.0F, 16.0F, 32.0F, 16.0F, new Dilation(0.0F)),
                .addBox(-8F, -8F, -8F, 16F, 32F, 16F),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        return LayerDefinition.create(modelData, 128, 128);
    }

    public HoundEntityModel(ModelPart root)
    {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    @Override
    public ModelPart root()
    {
        return root;
    }

    @Override
    public void setAngles(HoundEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
    {

    }
}
