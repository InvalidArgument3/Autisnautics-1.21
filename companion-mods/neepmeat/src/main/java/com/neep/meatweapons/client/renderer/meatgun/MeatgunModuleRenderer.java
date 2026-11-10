/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
import net.minecraft.client.resources.model.ModelResourceLocation; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.client.renderer.meatgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import dev.monarkhes.myron_neepmeat.impl.client.model.MyronBakedModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface MeatgunModuleRenderer<T extends MeatgunModule>
{
    ItemColors COLOURS = new ItemColors();

    void render(ItemStack stack, MeatgunComponent component, T module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay);

    default void renderItem(
            ItemStack stack,
            ItemDisplayContext renderMode,
            boolean leftHanded,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay,
            BakedModel model,
            boolean flat
    )
    {
        if (!stack.isEmpty())
        {
            matrices.pushPose();

//            if (model instanceof MyronBakedModel)
//            {
//                matrices.translate(0.5, 0, 0.5);
//            }

            boolean bl2 = false;

            RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, bl2);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

            renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer, flat);

            matrices.popPose();
        }
    }

    default void renderItem(
            ItemStack stack,
            ItemDisplayContext renderMode,
            boolean leftHanded,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay,
            BakedModel model
    )
    {
        renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, false);
    }

    static void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer consumer, boolean flat)
    {
        RandomSource random = RandomSource.create();
        long l = 42L;

        for (Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, consumer, model.getQuads(null, direction, random), stack, light, overlay, flat);
        }

        random.setSeed(42L);
        renderBakedItemQuads(matrices, consumer, model.getQuads(null, null, random), stack, light, overlay, flat);
    }

    static void renderBakedItemQuads(PoseStack matrices, VertexConsumer consumer, List<BakedQuad> quads, ItemStack stack, int light, int overlay, boolean flat)
    {
        PoseStack.Pose entry = matrices.last();

        for (BakedQuad bakedQuad : quads)
        {
            int i = -1;
            if (bakedQuad.isTinted())
            {
                i = COLOURS.getColor(stack, bakedQuad.getTintIndex());
            }

            float f = (float) (i >> 16 & 0xFF) / 255.0F;
            float g = (float) (i >> 8 & 0xFF) / 255.0F;
            float h = (float) (i & 0xFF) / 255.0F;

            if (!flat)
            {
                consumer.putBulkData(entry, bakedQuad, f, g, h, light, overlay);
            }
            else
            {
                float brightness = 1;

                // Not sure whether there's a more efficient alternative, but iI wOrKS
                BakedQuad newQuad = new BakedQuad(bakedQuad.getVertices(), bakedQuad.getTintIndex(), Direction.UP, bakedQuad.getSprite(), false);
                consumer.putBulkData(entry, newQuad, new float[]{brightness, brightness, brightness, brightness},
                        f, g, h,
                        new int[]{light, light, light, light}, overlay, true);
            }
        }
    }

    default BakedModel getModel(Minecraft client, ResourceLocation identifier)
    {
        return client.getItemRenderer().getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(identifier));
    }

    @FunctionalInterface
    interface Factory<T extends MeatgunModule>
    {
        MeatgunModuleRenderer<T> create(Minecraft client);
    }
}
