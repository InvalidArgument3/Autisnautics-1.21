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

package com.neep.neepmeat.client.meatgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.RotationAxis;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunModuleRenderer;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.item.DrillChassisItem;
import com.neep.neepmeat.meatgun.module.RotaryDrillHeadModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RotaryDrillHeadRenderer implements MeatgunModuleRenderer<RotaryDrillHeadModule>
{
    private final ItemRenderer itemRenderer;
    private final Minecraft client;
    private final RotaryDrillHeadModel model;

    public RotaryDrillHeadRenderer(Minecraft client, RotaryDrillHeadModel model)
    {
        this.itemRenderer = client.getItemRenderer();
        this.client = client;
        this.model = model;
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, RotaryDrillHeadModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, long time, float tickDelta, int light, int overlay)
    {
        boolean using = MWComponents.MEATGUN.get(stack).getCore().canRun(client.player, 1) &&
                DrillChassisItem.Client.using(stack, mode.firstPerson());

        BakedModel staticPart = getModel(client, NMExtraModels.ROTARY_DRILL_HEAD);
        BakedModel part1 = getModel(client, model.head1Id);
        BakedModel part2 = getModel(client, model.head2Id);
        BakedModel part3 = getModel(client, model.head3Id);
        BakedModel part4 = getModel(client, model.head4Id);

        renderItem(stack, mode, false, matrices, vcp, light, overlay, staticPart);

        float currentSpeed = module.currentSpeed;
        float angle = module.angle;

//        float delta = client.getTickDelta();
        float delta = using ? 0.1f : 0.05f;
        float targetSpeed = using ? 16 : 0;
        currentSpeed = (Mth.lerp(delta, currentSpeed, targetSpeed));
        angle = Mth.wrapDegrees(angle + currentSpeed * Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));

        module.angle = angle;
        module.currentSpeed = currentSpeed;

        // head transform
        matrices.translate(0, -8 / 16f, 0 / 16f);

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(RotationAxis.POSITIVE_Z.rotationDegrees(module.angle));
        matrices.translate(-0.5, -0.5, -0.5);
        renderItem(stack, mode, false, matrices, vcp, light, overlay, part1);
        renderItem(stack, mode, false, matrices, vcp, light, overlay, part3);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(RotationAxis.POSITIVE_Z.rotationDegrees(-module.angle));
        matrices.translate(-0.5, -0.5, -0.5);
        renderItem(stack, mode, false, matrices, vcp, light, overlay, part2);
        renderItem(stack, mode, false, matrices, vcp, light, overlay, part4);
        matrices.popPose();
    }
}
