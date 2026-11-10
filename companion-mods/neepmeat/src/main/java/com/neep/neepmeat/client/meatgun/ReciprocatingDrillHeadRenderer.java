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
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunModuleRenderer;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.item.DrillChassisItem;
import com.neep.neepmeat.meatgun.module.ReciprocatingDrillHeadModule;
import com.neep.neepmeat.util.NMMaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ReciprocatingDrillHeadRenderer implements MeatgunModuleRenderer<ReciprocatingDrillHeadModule>
{
    private final ItemRenderer itemRenderer;
    private final Minecraft client;

    public ReciprocatingDrillHeadRenderer(Minecraft client)
    {
        this.itemRenderer = client.getItemRenderer();
        this.client = client;
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, ReciprocatingDrillHeadModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay)
    {
        BakedModel base = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(NMExtraModels.ROCK_DRILL_HEAD));
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, base);

        boolean using = module.canRun() && DrillChassisItem.Client.using(stack, mode.firstPerson());
        BakedModel rod = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(NMExtraModels.ROCK_DRILL_ROD));

        // Drill head model is too far forwards as it wasn't always a meatgun part
        matrices.translate(0, 0, 18 / 16f);
        if (using)
            matrices.translate(0, 0, 3 / 16f * (1 + NMMaths.sin(client.level.getGameTime(), client.getDeltaTracker().getRealtimeDeltaTicks(), 10)));

        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, rod);
    }
}
