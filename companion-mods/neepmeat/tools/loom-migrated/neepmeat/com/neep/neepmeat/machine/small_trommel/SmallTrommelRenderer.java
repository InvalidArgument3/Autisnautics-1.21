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

package com.neep.neepmeat.machine.small_trommel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

@Environment(value = EnvType.CLIENT)
public class SmallTrommelRenderer implements BlockEntityRenderer<SmallTrommelBlockEntity>
{
    public SmallTrommelRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(SmallTrommelBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        Direction facing = be.getBlockState().getValue(SmallTrommelBlock.FACING);
//        be.currentSpeed = (float) (be.rotorSpeed * MathHelper.lerp(0.1, be.currentSpeed, be.getRunningRate() * 40));
//        be.angle = MathHelper.wrapDegrees(be.angle + be.currentSpeed * delta);
        float angle = 0;
        if (be.getLevel().getBlockEntity(be.getBlockPos().relative(facing.getOpposite())) instanceof MotorEntity motor)
        {
            angle = motor.getRotorAngle();
        }

        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.ZN.rotationDegrees(angle));
        matrices.translate(-0.5, -0.5, -0.5);
        BERenderUtils.renderModel(NMExtraModels.SMALL_TROMMEL_MESH, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);
        FluidVariant fluid = be.currentFluid;
        if (fluid != null && be.totalProgress > 0)
        {
            matrices.pushPose();
            be.renderProgress = NMMaths.lerpForwards(0.1f, be.renderProgress, be.progress / be.totalProgress);
            matrices.translate(0.5, 0.5, 0.7);
            matrices.scale(0.6f, 0.6f, 1.7f);
            matrices.translate(-0.5, -0.5, 0);
            matrices.mulPose(Axis.XP.rotationDegrees(-90));
            renderFluidColumn(vertexConsumers, matrices, be.currentFluid, 0, 1, 1, be.renderProgress);
            matrices.popPose();
        }
    }

    public static void renderFluidColumn(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float startXYZ, float depth, float endY, float scaleY)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), false));
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scaleY == 0)
        {
            return;
        }

        float startY = startXYZ;
        float dist = startY + (endY - startY) * scaleY;

        assert renderer != null;
        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            if (direction == Direction.UP)
            {
                emitter.square(Direction.UP, depth, depth, 1 - depth, 1 - depth, 1 - dist);
            }
            else if (direction != Direction.DOWN)
            {
                emitter.square(direction, depth, startY, 1 - depth, dist, depth);
            }

            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, 0x00F0_00F0, OverlayTexture.NO_OVERLAY);
        }
    }
}
