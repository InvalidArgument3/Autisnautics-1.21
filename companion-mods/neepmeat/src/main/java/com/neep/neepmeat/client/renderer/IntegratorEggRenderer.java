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
import com.mojang.math.Axis;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import com.neep.neepmeat.util.NMMaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

@OnlyIn(Dist.CLIENT)
public class IntegratorEggRenderer extends GeoBlockRenderer<IntegratorBlockEntity>
{
    private final RandomSource random = RandomSource.create();
    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/integrator_basic_overlay.png");

    public IntegratorEggRenderer(BlockEntityRendererProvider.Context context)
    {
        super(new IntegratorEggModel<IntegratorBlockEntity>());

//        addRenderLayer(new IntegratorOverlayRenderLayer(this));
    }

    public static void renderEgg(PoseStack matrices, IntegratorBlockEntity blockEntity, MultiBufferSource vertexConsumers, float tickDelta)
    {
        matrices.pushPose();
        matrices.pushPose();
        if (blockEntity.canGrow())
        {
//            float eggScale = 1 + (float) Math.sin(blockEntity.getWorld().getTime() / 50f) / 16;
            float eggScale = 1 + NMMaths.sin(blockEntity.getLevel().getGameTime(), tickDelta, 1 / 50f) / 16;
            matrices.translate(0.5, 0, 0.5);
            matrices.scale(eggScale, eggScale, eggScale);
            matrices.translate(-0.5, 0, -0.5);
        }

        BlockRenderDispatcher manager = Minecraft.getInstance().getBlockRenderer();
        int overlay = 0;
        manager.getModelRenderer().tesselateBlock(
                blockEntity.getLevel(),
                manager.getBlockModel(blockEntity.getBlockState()),
                blockEntity.getBlockState(),
                blockEntity.getBlockPos(),
                matrices,
                vertexConsumers.getBuffer(RenderType.cutout()),
                true,
                RandomSource.create(),
                0,
                overlay
        );
        matrices.popPose();

        matrices.translate(-1, 0, -1);
        matrices.scale(3, 2, 3);
        matrices.popPose();
    }

    public void defaultRender(PoseStack matrices, IntegratorBlockEntity be, MultiBufferSource vertexConsumers, RenderType renderType, VertexConsumer buffer, float yaw, float tickDelta, int packedLight)
    {
        if (!be.isMature)
        {
            renderEgg(matrices, be, vertexConsumers, tickDelta);
        }
        else
        {
            Vec2 vec = NMMaths.flattenY(be.getLookTarget().subtract(Vec3.atCenterOf(be.getBlockPos())));
            be.targetFacing = NMMaths.getAngle(vec);

            be.facing = NMMaths.angleLerp(0.03f, be.facing, be.targetFacing);

            // Rotate towards target
            renderBase(matrices, be, vertexConsumers);
            matrices.pushPose();
            matrices.translate(0.5d, 0d, 0.5d);
            matrices.mulPose(Axis.YP.rotation(be.facing));
            matrices.translate(-0.5d, 0d, -0.5d);
//            matrices.translate(0, 1.8 + Math.sin((be.getWorld().getTime() + tickDelta) / 20) / 15, 0);
            float s = NMMaths.sin(be.animationOffset + be.getLevel().getGameTime(), tickDelta, 1 / 20f);
            matrices.translate(0, 1.8 + s / 15, 0);

            GeoModel<IntegratorBlockEntity> modelProvider = getGeoModel();
//            GeoModel model = modelProvider.getModel(modelProvider.getModelResource(be));
//            modelProvider.setCustomAnimations(be, this.getInstanceId(be), );
            matrices.pushPose();
            matrices.translate(0, 0.01f, 0);
//            matrices.translate(0.5, 0, 0.5);
//            super.defaultRender(matrices, be, vertexConsumers, renderType, buffer, yaw, tickDelta, packedLight);

            Color renderColor = getRenderColor(animatable, tickDelta, packedLight);
            float red = renderColor.getRedFloat();
            float green = renderColor.getGreenFloat();
            float blue = renderColor.getBlueFloat();
            float alpha = renderColor.getAlphaFloat();
            int packedOverlay = getPackedOverlay(animatable, 0, tickDelta);
            BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(animatable));

            preRender(matrices, animatable, model, vertexConsumers, buffer, false, tickDelta, packedLight, packedOverlay, red, green, blue, alpha);



            if (firePreRenderEvent(matrices, model, vertexConsumers, tickDelta, packedLight))
            {
                // Render main model
                Minecraft.getInstance().getTextureManager().bindForSetup(getTextureLocation(be));

                RenderType renderType1 = RenderType.entityTranslucent(getTextureLocation(be));
                buffer = vertexConsumers.getBuffer(renderType1);
                matrices.pushPose();
                actuallyRender(matrices, animatable, model, renderType1,
                        vertexConsumers, buffer, false, tickDelta, packedLight, packedOverlay, red, green, blue, alpha);
                matrices.popPose();

                // Render enlightenment overlay
                float dataPropt = (float) be.getData(DataResource.NORMAL) / IntegratorBlockEntity.MAX_DATA;
                RenderType cameo = RenderType.entityTranslucent(LAYER);
                buffer = vertexConsumers.getBuffer(cameo);
                actuallyRender(matrices, animatable, model, cameo,
                        vertexConsumers, buffer, false, tickDelta, packedLight, packedOverlay, red, green, blue, dataPropt);
            }

            renderFinal(matrices, animatable, model, vertexConsumers, buffer, tickDelta, packedLight, packedOverlay, red, green, blue, alpha);

            matrices.popPose();
            matrices.popPose();
        }
    }

    public void renderBase(PoseStack matrices, IntegratorBlockEntity be, MultiBufferSource vertexConsumers)
    {
        BERenderUtils.renderModelSmooth(NMExtraModels.INTEGRATOR_BASE, matrices, be.getLevel(), be.getBlockPos(), vertexConsumers);
    }
}
