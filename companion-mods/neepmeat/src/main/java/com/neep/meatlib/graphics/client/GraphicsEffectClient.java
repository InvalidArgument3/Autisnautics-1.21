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

package com.neep.meatlib.graphics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.graphics.GraphicsEffect;
import com.neep.meatlib.graphics.GraphicsEffectType;
import com.neep.meatlib.graphics.GraphicsEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GraphicsEffectClient
{
    public static List<GraphicsEffect> EFFECTS = new ArrayList<>();
    private static final Map<GraphicsEffectType, GraphicsEffectFactory> FACTORIES = new IdentityHashMap<>();

    public static void addEffect(GraphicsEffect effect)
    {
        EFFECTS.add(effect);
    }

    public static void init()
    {
        ClientTickEvents.END_WORLD_TICK.register(ctx ->
        {
            ClientLevel world = Minecraft.getInstance().level;
            EFFECTS.removeIf(effect -> effect.isRemoved() || effect.getWorld() != world);
            EFFECTS.forEach(GraphicsEffect::tick);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ctx ->
        {
            PoseStack matrices = ctx.matrixStack();
            MultiBufferSource consumers = ctx.consumers();

            EFFECTS.forEach(((effect) -> effect.render(ctx.camera(), matrices, consumers, ctx.tickDelta())));
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx ->
        {
            PoseStack matrices = ctx.matrixStack();
            MultiBufferSource consumers = ctx.consumers();

            EFFECTS.forEach(((effect) -> effect.renderAfter(ctx.camera(), matrices, consumers, ctx.tickDelta())));
        });

        GraphicsEffects.CHANNEL.receiverHandler(Dist.CLIENT, (player, buf, responseSender) ->
        (worldKey, type, start, end, velocity, scale, maxTime, byteBuf) ->
            {
                ClientLevel world = Minecraft.getInstance().level;
                if (world == null)
                    throw new IllegalStateException("Tried to spawn effect in a null world!");

                if (world.dimension().equals(worldKey))
                {
//                    MeatLib.LOGGER.debug("Spawning graphics effect with uuid " + uuid + " in world " + worldId + ".");
                    GraphicsEffect effect = FACTORIES.get(type).create(world, start, end, velocity, scale, maxTime, byteBuf);
                    GraphicsEffectClient.addEffect(effect);
                }
            });
    }

    public static GraphicsEffectFactory registerEffect(GraphicsEffectType type, GraphicsEffectFactory factory)
    {
        FACTORIES.put(type, factory);
        return factory;
    }
}
