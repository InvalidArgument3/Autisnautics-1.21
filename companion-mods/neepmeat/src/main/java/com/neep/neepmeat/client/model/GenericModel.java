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

package com.neep.neepmeat.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class GenericModel<T extends GeoAnimatable> extends GeoModel<T>
{
    public final String namespace;
    public final String modelPath;
    public final String texturePath;
    public final String animPath;

    public GenericModel(String namespace, String modelPath, String texturePath, String animPath)
    {
        this.namespace = namespace;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.animPath = animPath;
    }

    @Override
    public ResourceLocation getModelResource(GeoAnimatable object)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, modelPath);
    }

    @Override
    public ResourceLocation getTextureResource(GeoAnimatable object)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, texturePath);
    }

    @Override
    public ResourceLocation getAnimationResource(GeoAnimatable animatable)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, animPath);
    }
}
