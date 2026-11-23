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

import com.neep.neepmeat.item.GogglesItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@OnlyIn(Dist.CLIENT)
public class GogglesArmourRenderer extends GeoArmorRenderer<GogglesItem>
{
    public GogglesArmourRenderer(GeoModel<GogglesItem> modelProvider)
    {
        super(modelProvider);

        this.head = modelProvider.getBone("armorHead").orElse(null);
        this.body = modelProvider.getBone("armorBody").orElse(null);
        this.rightArm = modelProvider.getBone("armorRightArm").orElse(null);
        this.leftArm = modelProvider.getBone("armorLeftArm").orElse(null);
        this.rightLeg = modelProvider.getBone("armorRightLeg").orElse(null);
        this.leftLeg = modelProvider.getBone("armorLeftLeg").orElse(null);
        this.rightBoot = modelProvider.getBone("armorRightBoot").orElse(null);
        this.leftBoot = modelProvider.getBone("armorLeftBoot").orElse(null);
    }
}
