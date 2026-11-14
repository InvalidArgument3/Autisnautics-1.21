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

import com.neep.meatlib.mixin.client.MinecraftClientAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

@OnlyIn(Dist.CLIENT)
public interface UseAttackCallback
{
    Event<UseAttackCallback> DO_USE = EventFactory.createArrayBacked(UseAttackCallback.class,
            (listeners) -> (client, player) ->
            {
                for (UseAttackCallback listener : listeners)
                {
                    boolean result = listener.context(client, player);
                    if (!result)
                        return false;
                }
                return true;
            });

    Event<UseAttackCallback> DO_ATTACK = EventFactory.createArrayBacked(UseAttackCallback.class,
            (listeners) -> (client, player) ->
            {
                for (UseAttackCallback listener : listeners)
                {
                    boolean result = listener.context(client, player);
                    if (!result) return false;
                }
                return true;
            });

    static void swingHand(Minecraft client, InteractionHand hand)
    {
        client.player.swing(hand);
        ((MinecraftClientAccessor) client).setUseItemCooldown(4);
    }

    boolean context(Minecraft client, LocalPlayer player);
}
