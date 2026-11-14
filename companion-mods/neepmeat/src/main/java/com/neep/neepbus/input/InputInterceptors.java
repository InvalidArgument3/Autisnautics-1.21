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

package com.neep.neepbus.input;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.neepbus.NeepBus;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class InputInterceptors
{
    public static final GlobalChannelManager<ApplyEvent> EVENT = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepBus.REGISTRY_NAMESPACE, "neepbus/input_event"), ChannelFormat.builder(ApplyEvent.class).param(ParamCodec.BOOLEAN).param(ParamCodec.INT).param(InputInterceptor.Movement.PARAM_CODEC).build());
    public static final GlobalChannelManager<Activate> ACTIVATE = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepBus.REGISTRY_NAMESPACE, "neepbus/activate_input_interceptor"), ChannelFormat.builder(Activate.class).param(ParamCodec.BOOLEAN).param(ParamCodec.ofNullable(ParamCodecs.IDENTIFIER)).build());

    public static void activate(Player player, InputInterceptor interceptor)
    {
        InputInterceptorPlayer player1 = (InputInterceptorPlayer) player;
        InputInterceptor prev = player1.neepmeat$getInterceptor();

        if (prev != null)
            prev.close(player);

        player1.neepmeat$etInterceptor(interceptor);
        interceptor.open(player);
        InputInterceptors.ACTIVATE.emitter(player).apply(true, interceptor.handlerId());
    }

    public static void deactivate(Player player)
    {
        InputInterceptorPlayer player1 = (InputInterceptorPlayer) player;
        InputInterceptor prev = player1.neepmeat$getInterceptor();

        if (prev != null)
            prev.close(player);

        player1.neepmeat$etInterceptor(null);
        InputInterceptors.ACTIVATE.emitter(player).apply(false, null);
    }

    public static void init()
    {
        EVENT.receiverHandler(Dist.DEDICATED_SERVER, (player, buf, responseSender) -> (action, name, movement) ->
        {
            var interceptor = ((InputInterceptorPlayer) player).neepmeat$getInterceptor();
            if (interceptor != null)
            {
                if (movement != null)
                {
                    interceptor.movementInput(player, action, movement);
                }

                interceptor.input(player, action, name);
            }
        });

        // Allow the client to deactivate the interceptor
        ACTIVATE.receiverHandler(Dist.DEDICATED_SERVER, ((player, buf, responseSender) -> (active, id) ->
        {
            if (!active)
                deactivate(player);
        }));
    }

    public static boolean isActive(Player player)
    {
        return ((InputInterceptorPlayer) player).neepmeat$getInterceptor() != null;
    }

    public interface ApplyEvent
    {
        void apply(boolean action, int key, @Nullable InputInterceptor.Movement movement);
    }

    public interface Activate
    {
        void apply(boolean active, @Nullable ResourceLocation id);
    }
}
