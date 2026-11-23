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

package com.neep.meatweapons.client.model;

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.LMGItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LMGItemModel extends GeoModel<LMGItem>
{
    @Override
    public ResourceLocation getModelResource(LMGItem object)
    {
        // Apparently, the model folder MUST be called 'geo'.
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "geo/light_machine_gun.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LMGItem object)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/general/light_machine_gun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LMGItem animatable)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "animations/light_machine_gun.animation.json");
    }
}
