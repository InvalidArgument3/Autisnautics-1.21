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
import com.neep.meatweapons.item.Aimable;
import com.neep.meatweapons.item.BaseGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BaseGunRenderer<T extends BaseGunItem & GeoAnimatable> extends GeoItemRenderer<T>
{
    public Vector3f currentTransform = new Vector3f(0, 0, 0);

    public BaseGunRenderer(GeoModel<T> model)
    {
        super(model);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick)
    {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrices,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        this.animatable = (T) stack.getItem();
        this.currentItemStack = stack;
        this.renderPerspective = transformType;

        LocalPlayer player = Minecraft.getInstance().player;

        if (transformType == ItemDisplayContext.GUI)
        {
            renderInGui(transformType, matrices, bufferSource, packedLight, packedOverlay);
        }
        else
        {
            if (renderPerspective.firstPerson() && player != null)
            {
                boolean isAiming = player.isShiftKeyDown();
                Vector3f transform = stack.getItem() instanceof Aimable aimable ? aimable.getAimOffset() : new Vector3f(0, 0, 0);
                float delta = 0.2f;
                currentTransform.lerp(isAiming ? transform : new Vector3f(0, 0, 0), delta);

                boolean stackInMain = GeoItem.getId(player.getItemInHand(InteractionHand.MAIN_HAND)) == GeoItem.getId(stack);

                matrices.translate(
                        (stackInMain && player.getMainArm() == HumanoidArm.RIGHT) ? -currentTransform.x : currentTransform.x,
                        currentTransform.y,
                        currentTransform.z);
            }

            RenderType renderType = getRenderType(this.animatable, getTextureLocation(this.animatable), bufferSource, Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
            VertexConsumer buffer = ItemRenderer.getFoilBufferDirect(bufferSource, renderType, false, this.currentItemStack != null && this.currentItemStack.hasFoil());

            defaultRender(matrices, this.animatable, bufferSource, renderType, buffer,
                    0, Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks(), packedLight);
        }
    }


    //    public void render(ItemStack itemStack, ModelTransformationMode mode, MatrixStack matrices,
//                       VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn)
//    {
//    }

//    public void render(T animatable, MatrixStack matrices, VertexConsumerProvider bufferIn, int packedLightIn,
//                       ItemStack itemStack) {
//
//        super.render(animatable, matrices, bufferIn, packedLightIn, itemStack);

//        MinecraftClient client = MinecraftClient.getInstance();
//        this.currentItemStack = itemStack;
//        AnimationEvent<T> itemEvent = new AnimationEvent<>(animatable, 0, 0,
//                MinecraftClient.getInstance().getTickDelta(), false, Collections.singletonList(itemStack));
//        modelProvider.setLivingAnimations(animatable, this.getUniqueID(animatable), itemEvent);
//        matrices.push();
//        matrices.translate(0.5, 0.5, 0.5);
//
//        MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureLocation(animatable));
//        GeoModel model = modelProvider.getModel(modelProvider.getModelLocation(animatable));
//        Color renderColor = getRenderColor(animatable, 0, matrices, bufferIn, null, packedLightIn);
//        RenderLayer renderType = getRenderType(animatable, 0, matrices, bufferIn, null, packedLightIn,
//                getTextureLocation(animatable));
//        render(model, animatable, 0, renderType, matrices, bufferIn, null, packedLightIn, OverlayTexture.DEFAULT_UV,
//                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
//                (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
//        Optional<GeoBone> location = model.getBone("main");
////        Optional<GeoBone> location2 = model.getBone("");
//        if (location.isPresent())
//        {
//            GeoBone bone = location.get();
//            matrices.push();
//            RenderUtils.translate(bone, matrices);
//            RenderUtils.moveToPivot(bone, matrices);
//            RenderUtils.rotate(bone, matrices);
//            RenderUtils.scale(bone, matrices);
////            RenderUtils.moveBackFromPivot(bone, matrices);
////            matrices.translate(0, 1.0, 0);
//            matrices.scale(0.01f, -0.01f, 0.1f);
////            MinecraftClient.getInstance().textRenderer.draw(matrices, String.valueOf(1 - itemStack.getDamage() / itemStack.getItem().getMaxDamage()), 0.f, 0.f, 0x00bbbbbb);
//            MinecraftClient.getInstance().textRenderer.draw(matrices, "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww", 0.f, 0.f, 0x00bbbbbb);
//            matrices.pop();
//            matrices.pop();
//        }
//    }
}
