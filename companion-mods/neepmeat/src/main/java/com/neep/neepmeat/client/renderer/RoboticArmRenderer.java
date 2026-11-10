/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * The same two-bone solve as upstream. Upstream drew these parts with Flywheel and left the
 * block-entity draws commented out. This jar does not compile the Flywheel visual, so the
 * JSON part models are drawn here.
 */
public class RoboticArmRenderer implements BlockEntityRenderer<RoboticArmBlockEntity> {
    private static final ResourceLocation SPINNY = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/spinny_bit");
    private static final ResourceLocation SEGMENT_1 = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/segment_1");
    private static final ResourceLocation SEGMENT_2 = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "block/robotic_arm/segment_2");

    public RoboticArmRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public boolean shouldRenderOffScreen(RoboticArmBlockEntity be) {
        return true;
    }

    @Override
    public void render(RoboticArmBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        be.prevX = Mth.lerp(0.2, be.prevX, be.getX());
        be.prevY = Mth.lerp(0.2, be.prevY, be.getY());
        be.prevZ = Mth.lerp(0.2, be.prevZ, be.getZ());

        double targetX = be.prevX;
        double targetY = be.prevY;
        double targetZ = be.prevZ;

        double relX = targetX - (be.getBlockPos().getX() + 0.5);
        double relY = targetY - (be.getBlockPos().getY() + 1 + 2 / 16f);
        double relZ = targetZ - (be.getBlockPos().getZ() + 0.5);

        float yaw = (float) Math.toDegrees(Math.atan2(relX, relZ));

        float l = 2 + 5 / 16f;
        double lx = Math.sqrt(relX * relX + relZ * relZ);
        double ly = relY;
        double d5 = Math.sqrt(lx * lx + ly * ly);
        if (d5 < 1.0E-4) {
            return;
        }

        double x1 = lx / d5 * Math.min(d5, 2 * l - 0.0001);
        double y1 = ly / d5 * Math.min(d5, 2 * l - 0.0001);
        double d = Math.sqrt(x1 * x1 + y1 * y1);
        double x2 = x1 / d;
        double y2 = y1 / d;
        double a = Math.acos((-(d * d)) / (-2 * d * l));
        double x3 = l * (x2 * cos(a) - y2 * sin(a));
        double y3 = l * (x2 * sin(a) + y2 * cos(a));

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(yaw));
        matrices.translate(-0.5, -0.5, -0.5);
        renderPart(SPINNY, matrices, vertexConsumers, light, overlay, RenderType.solid());

        matrices.pushPose();
        matrices.translate(0, 2 - 1 / 16f, 0);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle1 = Math.atan2(x3, y3);
        matrices.mulPose(Axis.XP.rotation((float) angle1));
        matrices.translate(-0.5, 12 / 16f, -0.5);
        renderPart(SEGMENT_1, matrices, vertexConsumers, light, overlay, RenderType.solid());
        matrices.popPose();

        matrices.translate(0, y3 + 2 - 1 / 16f, x3);
        matrices.translate(0.5, -12 / 16f, 0.5);
        double angle2 = Math.atan2(lx - x3, ly - y3);
        matrices.mulPose(Axis.XP.rotation((float) angle2));
        matrices.translate(-0.5, 12 / 16f, -0.5);
        renderPart(SEGMENT_2, matrices, vertexConsumers, light, overlay, RenderType.cutout());
        matrices.popPose();
    }

    private static void renderPart(ResourceLocation model, PoseStack matrices, MultiBufferSource buffers, int light, int overlay, RenderType type) {
        BakedModel baked = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(model));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                matrices.last(), buffers.getBuffer(type), Blocks.AIR.defaultBlockState(), baked, 1.0F, 1.0F, 1.0F, light, overlay);
    }
}
