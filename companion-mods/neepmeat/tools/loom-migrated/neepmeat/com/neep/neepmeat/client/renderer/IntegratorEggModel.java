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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.model.GeoModel;

@Environment(value = EnvType.CLIENT)
public class IntegratorEggModel<I extends BlockEntity> extends GeoModel<IntegratorBlockEntity>
{
    @Override
    public ResourceLocation getModelResource(IntegratorBlockEntity object)
    {
        return new ResourceLocation(NeepMeat.NAMESPACE, "geo/integrator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IntegratorBlockEntity object)
    {
        return new ResourceLocation(NeepMeat.NAMESPACE, "textures/entity/integrator_basic.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IntegratorBlockEntity animatable)
    {
        return new ResourceLocation(NeepMeat.NAMESPACE, "animations/integrator.animation.json");
    }
}
