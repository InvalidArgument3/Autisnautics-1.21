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

package com.neep.neepmeat.client.renderer;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class IntegratorEggModel<I extends BlockEntity> extends GeoModel<IntegratorBlockEntity>
{
    public ResourceLocation getModelResource(IntegratorBlockEntity object)
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "geo/integrator.geo.json");
    }

    public ResourceLocation getTextureResource(IntegratorBlockEntity object)
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/integrator_basic.png");
    }

    public ResourceLocation getAnimationResource(IntegratorBlockEntity animatable)
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "animations/integrator.animation.json");
    }
}
