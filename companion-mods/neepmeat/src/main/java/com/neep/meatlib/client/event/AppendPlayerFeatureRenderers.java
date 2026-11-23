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

package com.neep.meatlib.client.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public interface AppendPlayerFeatureRenderers
{
    Event<AppendPlayerFeatureRenderers> EVENT = EventFactory.createArrayBacked(AppendPlayerFeatureRenderers.class, listeners ->(context, renderer, appender) ->
    {
        for (var listener : listeners)
        {
            listener.apply(context, renderer, appender);
        }
    });

    void apply(EntityRendererProvider.Context context, LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, Consumer<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> appender);
}
