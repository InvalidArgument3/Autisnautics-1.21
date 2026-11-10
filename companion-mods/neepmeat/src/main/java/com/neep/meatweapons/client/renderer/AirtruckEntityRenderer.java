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

package com.neep.meatweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatweapons.client.model.AirtruckModel;
import com.neep.meatweapons.entity.AirtruckEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.loading.math.MolangParser;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AirtruckEntityRenderer extends GeoEntityRenderer<AirtruckEntity>
{
    public AirtruckEntityRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new AirtruckModel());
    }

    //    // any of the built-in renderers because they have incompatible class parameter bounds.
//    static
//    {
//        AnimationController.addModelFetcher(animatable -> animatable instanceof Entity entity ?
//                (IAnimatableModel<Object>) AnimationUtils.getGeoModelForEntity(entity) : null);
//    }
//
//    protected GeoModelProvider<T> modelProvider;
//    protected final List<GeoLayerRenderer<T>> layerRenderers = Lists.newArrayList();
//    private Identifier whTexture;
//    private VertexConsumerProvider rtb = null;
//
//    public AirtruckEntityRenderer(EntityRendererFactory.Context context)
//    {
//        super(context);
//        this.modelProvider = (GeoModelProvider<T>) new AirtruckModel();
//    }
//
//    @Override
//    public Identifier getTexture(T entity)
//    {
//        return getTextureLocation(entity);
//    }
//
//    @Override
//    public VertexConsumerProvider getCurrentRTB()
//    {
//        return rtb;
//    }
//
//    @Override
//    public GeoModelProvider<T> getGeoModelProvider()
//    {
//        return modelProvider;
//    }
//
//    @Override
//    public Identifier getTextureLocation(T instance)
//    {
//        return this.modelProvider.getTextureResource(instance);
//    }
//
//    @Override
//    public Identifier getTextureResource(T instance)
//    {
//        return getTextureLocation(instance);
//    }
//
    public static float ease(float x)
    {
//        return (float) (1 / (1 + Math.exp(-9 * (x - 0.5))));
        return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
    }

    @Override
    public RenderType getRenderType(AirtruckEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick)
    {
        return RenderType.entityCutout(texture);
    }

    @Override
    public void actuallyRender(PoseStack matrices, AirtruckEntity entity, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer vcp, boolean isReRender, float tickDelta, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        matrices.pushPose();

        float f = Mth.rotLerp(tickDelta, entity.yRotO, entity.getYRot());

        float ageInTicks = entity.tickCount + tickDelta;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        applyRotations1(entity, matrices, ageInTicks, f, tickDelta);

        if (!isReRender)
        {
            float headPitch = Mth.lerp(tickDelta, entity.xRotO, entity.getXRot());
            float motionThreshold = getMotionAnimThreshold(entity);
            Vec3 velocity = entity.getDeltaMovement();
            float avgVelocity = (float) (Math.abs(velocity.x) + Math.abs(velocity.z) / 2f);
            AnimationState<AirtruckEntity> animationState = new AnimationState<>(entity, limbSwing, limbSwingAmount, tickDelta, avgVelocity >= motionThreshold);
            long instanceId = getInstanceId(entity);

            animationState.setData(DataTickets.TICK, entity.getTick(entity));
            animationState.setData(DataTickets.ENTITY, entity);
            animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(false, false, -0, -headPitch));
            this.model.addAdditionalStateData(entity, instanceId, animationState::setData);
            this.model.handleAnimations(entity, instanceId, animationState);
        }

        matrices.translate(0, 0.01f, 0);

        this.modelRenderTranslations = new Matrix4f(matrices.last().pose());

        float prop = entity.forwardsVelocity / entity.maxSpeed;
        var parser = MolangParser.INSTANCE;
        parser.setValue("r_lf", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
        parser.setValue("r_rf", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
        parser.setValue("r_lb", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
        parser.setValue("r_rb", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);

        if (!entity.isInvisibleTo(Minecraft.getInstance().player))
            actuallyRender1(matrices, entity, model, renderType, bufferSource, vcp, isReRender, tickDelta,
                    packedLight, packedOverlay, red, green, blue, alpha);

        matrices.popPose();
    }

    void actuallyRender1(PoseStack poseStack, AirtruckEntity animatable, BakedGeoModel model, RenderType renderType,
                                 MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                 int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        updateAnimatedTextureFrame(animatable);

        for (GeoBone group : model.topLevelBones())
        {
            renderRecursively(poseStack, animatable, group, renderType, bufferSource, buffer, isReRender, partialTick, packedLight,
                    packedOverlay, red, green, blue, alpha);
        }
    }

    //    @Override
//    public void render(T entity, float entityYaw, float partialTicks, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn)
//    {
//        stack.push();
//        EntityModelData entityModelData = new EntityModelData();
//
//        float f = MathHelper.lerpAngleDegrees(partialTicks, entity.prevYaw, entity.getYaw());
//
//        float f7 = this.handleRotationFloat(entity, partialTicks);
//        this.applyRotations(entity, stack, f7, f, partialTicks);
//
//        float prop = entity.forwardsVelocity / entity.maxSpeed;
//        var parser = MolangParser.INSTANCE;
//        parser.setValue("r_lf", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
//        parser.setValue("r_rf", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
//        parser.setValue("r_lb", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
//        parser.setValue("r_rb", () -> Math.signum(prop) * -ease(Math.abs(prop))  * 20);
//
////        AnimationEvent<T> predicate = new AnimationEvent<T>(entity, 0, 0, partialTicks,
////                false, Collections.singletonList(entityModelData));
////        GeoModel model = modelProvider.getModel(modelProvider.getModelResource(entity));
////        if (modelProvider instanceof IAnimatableModel)
////        {
////            ((IAnimatableModel<T>) modelProvider).setCustomAnimations(entity, this.getInstanceId(entity));
////        }
//
//        stack.translate(0, 0.01f, 0);
//        MinecraftClient.getInstance().getTextureManager().bindTexture(getTexture(entity));
//        Color renderColor = getRenderColor(entity, partialTicks, stack, bufferIn, null, packedLightIn);
//        RenderLayer renderType = getRenderType(entity, partialTicks, stack, bufferIn, null, packedLightIn, getTexture(entity));
//        boolean invis = entity.isInvisibleTo(MinecraftClient.getInstance().player);
//
//        render(model, entity, partialTicks, renderType, stack, bufferIn, null, packedLightIn,
//                getPackedOverlay(entity, 0), (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
//                (float) renderColor.getBlue() / 255f, invis ? 0.0F : (float) renderColor.getAlpha() / 255);
//
//        stack.pop();
//        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
//    }
//
//    @Override
//    public int getInstanceId(T animatable)
//    {
//        return animatable.getUuid().hashCode();
//    }
//
//    @Override
//    public void renderEarly(T animatable, MatrixStack stackIn, float ticks, VertexConsumerProvider renderTypeBuffer,
//                            VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue,
//                            float partialTicks)
//    {
//        this.rtb = renderTypeBuffer;
//        this.whTexture = this.getTextureLocation(animatable);
//        IGeoRenderer.super.renderEarly(animatable, stackIn, ticks, renderTypeBuffer, vertexBuilder, packedLightIn,
//                packedOverlayIn, red, green, blue, partialTicks);
//    }
//
    protected void applyRotations1(AirtruckEntity entity, PoseStack matrices, float ageInTicks, float rotationYaw, float tickDelta)
    {
        matrices.mulPose(Axis.YP.rotationDegrees(180 - rotationYaw));
        matrices.mulPose(Axis.ZN.rotationDegrees(entity.getRoll(tickDelta)));
        matrices.mulPose(Axis.XN.rotationDegrees(entity.getViewXRot(tickDelta)));
    }
//
//    public static int getPackedOverlay(Entity livingEntityIn, float uIn)
//    {
//        return OverlayTexture.getUv(OverlayTexture.getU(uIn), false);
//    }
//
//    protected float handleRotationFloat(T livingBase, float partialTicks)
//    {
//        return (float) livingBase.age + partialTicks;
//    }
//
//    public final boolean addLayer(GeoLayerRenderer<T> layer)
//    {
//        return this.layerRenderers.add(layer);
//    }
}
