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
import com.neep.meatweapons.item.HandCannonItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HandCannonItemModel extends GeoModel<HandCannonItem>
{
    public ResourceLocation getModelResource(HandCannonItem object)
    {
        // Apparently, the model folder MUST be called 'geo'.
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "geo/hand_cannon-thin.geo.json");
    }

    public ResourceLocation getTextureResource(HandCannonItem object)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/general/hand_cannon-thin.png");
    }

    public ResourceLocation getAnimationResource(HandCannonItem animatable)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "animations/hand_cannon.animation.json");
    }
}
