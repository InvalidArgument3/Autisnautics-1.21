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

package com.neep.neepmeat.client;

import com.neep.meatlib.client.event.AppendPlayerFeatureRenderers;
import com.neep.neepmeat.client.renderer.entity.CatEarsFeatureRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class NMFeatureRenderers
{
    public static void init()
    {
        AppendPlayerFeatureRenderers.EVENT.register((context, renderer, appender) ->
        {
            appender.accept(new CatEarsFeatureRenderer(context, renderer));
        });

        EntityModelLayerRegistry.registerModelLayer(CatEarsFeatureRenderer.MODEL_LAYER, CatEarsFeatureRenderer::getTexturedModelData);
    }
}
