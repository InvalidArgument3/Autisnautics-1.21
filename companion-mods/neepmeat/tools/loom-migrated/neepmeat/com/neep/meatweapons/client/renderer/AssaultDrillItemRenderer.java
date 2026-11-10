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

import com.neep.meatlib.client.RotationAxis;
import com.neep.neepmeat.client.renderer.RendererInstance;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.component.AssaultDrillComponent;
import com.neep.meatweapons.init.MWComponents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static com.neep.neepmeat.client.renderer.item.DrillChassisItemRenderer.renderItem;

import com.mojang.blaze3d.vertex.PoseStack;

public class AssaultDrillItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer
{
    public AssaultDrillItemRenderer()
    {
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        AssaultDrillRendererInstance instance = RendererInstance.getNullable(stack);
        if (instance == null)
            return;

        Minecraft client = Minecraft.getInstance();
        BakedModel main = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(MWExtraModels.ASSAULT_DRILL_HANDLE);
        BakedModel part1 = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(MWExtraModels.ASSAULT_DRILL_PART_1);
        BakedModel part2 = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(MWExtraModels.ASSAULT_DRILL_PART_2);
        BakedModel part3 = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(MWExtraModels.ASSAULT_DRILL_PART_3);
        BakedModel part4 = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(MWExtraModels.ASSAULT_DRILL_PART_4);

        float timeDegrees = client.level.getGameTime() % 360 + client.getFrameTime();

        AssaultDrillComponent component = MWComponents.ASSAULT_DRILL.getNullable(stack);

        if (component != null)
        {
            boolean using = component.using() || component.attacking();

            float thrust = instance.thrust;
            float targetThrust = using ? 0.07f : 0;

            float delta = using ? 0.1f : 0.05f;
            float angle = instance.angle;
            float currentSpeed = instance.currentSpeed;
            float targetSpeed = using ? 10 : 0;

            float shake = (float) (0.01f * Math.sin(Math.toRadians(timeDegrees) * 140));
            if (!using)
                shake = 0;

            thrust = Mth.lerp(0.1f, thrust, targetThrust);
            currentSpeed = (Mth.lerp(delta, currentSpeed, targetSpeed));
            angle = Mth.wrapDegrees(angle + currentSpeed * Minecraft.getInstance().getDeltaFrameTime());

            instance.thrust = thrust;
            instance.currentSpeed = currentSpeed;
            instance.angle = angle;

            matrices.translate(-thrust / 2, shake, -thrust);

            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, true);

            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(RotationAxis.POSITIVE_Z.rotationDegrees(instance.angle));
            matrices.translate(-0.5, -0.5, -0.5);
            renderItem(stack, mode, matrices, vcp, light, overlay, part1, false, true);
            renderItem(stack, mode, matrices, vcp, light, overlay, part3, false, true);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(RotationAxis.POSITIVE_Z.rotationDegrees(-instance.angle));
            matrices.translate(-0.5, -0.5, -0.5);
            renderItem(stack, mode, matrices, vcp, light, overlay, part2, false, true);
            renderItem(stack, mode, matrices, vcp, light, overlay, part4, false, true);
            matrices.popPose();
        }

    }

    public static class AssaultDrillRendererInstance implements RendererInstance
    {
        public AssaultDrillRendererInstance()
        {

        }

        public boolean using;
        public float angle;
        public float currentSpeed;
        public float thrust;
    }

    static
    {
        RendererInstance.register(MWItems.ASSAULT_DRILL, AssaultDrillRendererInstance::new);
    }
}