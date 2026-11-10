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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class IntegratorOverlayRenderLayer extends GeoRenderLayer<IntegratorBlockEntity>
{
    private static final ResourceLocation LAYER = new ResourceLocation(NeepMeat.NAMESPACE, "textures/entity/integrator_basic_overlay.png");

    public IntegratorOverlayRenderLayer(GeoRenderer<IntegratorBlockEntity> entityRendererIn)
    {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrices, IntegratorBlockEntity be, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
    {
        RenderType armorRenderType = RenderType.armorCutoutNoCull(LAYER);

        getRenderer().reRender(getDefaultBakedModel(be), matrices, bufferSource, be, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);
    }
}
