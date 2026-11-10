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

import com.neep.meatweapons.item.BaseGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BaseGunModel<T extends BaseGunItem> extends GeoModel<T>
{
    private final ResourceLocation modelPath;
    private final ResourceLocation texturePath;
    private final ResourceLocation animationPath;

    public BaseGunModel(ResourceLocation modelPath, ResourceLocation texturePath, ResourceLocation animationPath)
    {
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.animationPath = animationPath;
    }


    public ResourceLocation getModelResource(T object)
    {
        return modelPath;
    }

    public ResourceLocation getTextureResource(T object)
    {
        return texturePath;
    }

    public ResourceLocation getAnimationResource(T animatable)
    {
        return animationPath;
    }
}
