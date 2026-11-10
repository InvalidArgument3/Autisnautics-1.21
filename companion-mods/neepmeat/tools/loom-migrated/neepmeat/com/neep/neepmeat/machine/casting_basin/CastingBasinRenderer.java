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

package com.neep.neepmeat.machine.casting_basin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(value = EnvType.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class CastingBasinRenderer implements BlockEntityRenderer<CastingBasinBlockEntity>
{
    private final ItemRenderer itemRenderer;

    public CastingBasinRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(CastingBasinBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        WritableSingleFluidStorage storage = be.getStorage().inputStorage;
        FluidVariant variant = storage.getResource();
        storage.renderLevel = Mth.lerp(0.1f, storage.renderLevel, storage.getAmount() / (float) storage.getCapacity());
        renderSurface(vertexConsumers, matrices, variant, 10 / 16f, 3 / 16f, 3 / 16f, storage.renderLevel, light);

        ItemStack stack = be.getStorage().outputStorage.getAsStack();
        BakedModel bakedModel = this.itemRenderer.getModel(stack, be.getLevel(), null, 0);
        boolean depth = bakedModel.isGui3d();
        matrices.translate(0.5, 0.66, 0.5);
        if (!depth) matrices.mulPose(Axis.XP.rotationDegrees(-90));
        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY, bakedModel);
    }

    public static void renderSurface(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float start, float height, float depth, float scale, int light)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(Sheets.translucentCullBlockSheet());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        // Magic colourspace transformation copied from Modern Industrialisation
        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scale == 0)
        {
            return;
        }

        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        emitter.square(Direction.UP, depth, depth, 1 - depth, 1 - depth, 1 - (start + height * scale));

        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, light, OverlayTexture.NO_OVERLAY);
    }
}
