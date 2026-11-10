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

package com.neep.neepmeat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.mixin.client.ModelPartAccessor;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.entity.CatEarsImplant;
import com.neep.neepmeat.implant.entity.ImplantRenderingHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@OnlyIn(Dist.CLIENT)
public class CatEarsFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "cat_ears"), "cat_ears");
    public static final ResourceLocation COLOUR_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/cat_ears.png");

    public static final Map<ResourceLocation, ResourceLocation> TEXTURES = new HashMap<>();

    private final ModelPart root;

    public CatEarsFeatureRenderer(ModelPart root, RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
        this.root = root;
    }

    public CatEarsFeatureRenderer(EntityRendererProvider.Context context, RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        this(context.bakeLayer(MODEL_LAYER), renderer);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vcp, int light, AbstractClientPlayer player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
    {
        CatEarsImplant implant = ((ImplantRenderingHelper) player).neepmeat$getCatEars();
        if (implant != null && player.isSkinLoaded() && !player.isInvisible())
        {
            ModelPart ears;
            try
            {
                ears = root.getChild(implant.getSkin().toString());
            }
            catch (NoSuchElementException e)
            {
                ears = root.getChild("neepmeat:skin");
            }

            ResourceLocation texture = TEXTURES.getOrDefault(implant.getSkin(), player.getSkinTextureLocation());
            VertexConsumer vertexConsumer = vcp.getBuffer(RenderType.entityCutoutNoCull(texture));

            int m = LivingEntityRenderer.getOverlayCoords(player, 0.0F);

            boolean outerLayer = player.isModelPartShown(PlayerModelPart.HAT);

            for (int n = 0; n < 2; n++)
            {
                int i = 2 * n - 1;

                matrices.pushPose();
                getParentModel().head.translateAndRotate(matrices);
                matrices.translate(i * (-1 - 1.5) / 16f, -5 / 16f, 0.0F);
                if (outerLayer)
                {
                    float f = 1.1f;
                    matrices.translate(0, -3 / 16f, 0);
                    matrices.scale(f, f, 1);
                    matrices.translate(0, 3 / 16f, 0);
                }

                ears.setPos(0, 2, 0);
                ears.zRot = i * 0 * Mth.DEG_TO_RAD;
                ears.xScale = i;
                ears.yScale = 1;
                ears.x = 0;
                ears.y = 0;

                int col = implant.getCol();
                float r = ((col >> 16) & 255) / 256f;
                float g = ((col >> 8) & 255) / 256f;
                float b = (col & 255) / 256f;

                // Can't use ModelPart::render as we need to render the inner ears using a different colour
                ears.translateAndRotate(matrices);
                ModelPartAccessor accessor = (ModelPartAccessor) (Object) ears;
                accessor.callRenderCuboids(matrices.last(), vertexConsumer, light, m, r, g, b, 1);

                if (ears.hasChild("inner"))
                {
                    col = implant.getInnerCol();
                    r = ((col >> 16) & 255) / 256f;
                    g = ((col >> 8) & 255) / 256f;
                    b = (col & 255) / 256f;
                    ears.getChild("inner").render(matrices, vertexConsumer, light, m, r, g, b, 1);
                }

//                Matrix3f normal = matrices.peek().getNormalMatrix();
//                Matrix4f mat = matrices.peek().getPositionMatrix();
//                vertexConsumer.vertex(mat, 0, 0, 0).setColor(1, 1, 1, 1).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).setNormal(normal, 0, 1, 0);
//                vertexConsumer.vertex(0, 2, 0, 1, 1, 1, 1, 0, 0, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
//                vertexConsumer.vertex(2, 2, 0, 1, 1, 1, 1, 1, 0, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
//                vertexConsumer.vertex(2, 0, 0, 1, 1, 1, 1, 1, 1, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);

                matrices.popPose();
            }
        }
    }

    public static LayerDefinition getTexturedModelData()
    {
        MeshDefinition md = new MeshDefinition();

        float xOff = -1.5f;
        float w = 3f;

        var root = md.getRoot();
        root.addOrReplaceChild("neepmeat:skin",
                CubeListBuilder.create().texOffs(32, 0).addBox(xOff, -6.0F, -2.0F, w, 3.0F, 1.0F), PartPose.ZERO);

        var colour = root.addOrReplaceChild("neepmeat:colour",
                CubeListBuilder.create().texOffs(0, 0).addBox(xOff, -6.0F, -2.0F, w, 3.0F, 1.0F), PartPose.ZERO);
        colour.addOrReplaceChild("inner",
                CubeListBuilder.create().texOffs(0, 4).addBox(xOff, -6.0F, -2.0F, w, 3.0F, 1.0F, new CubeDeformation(0.0f)), PartPose.ZERO);

        return LayerDefinition.create(md, 64, 64);
    }
}
