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

package com.neep.neepmeat.client.item;

import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.neepmeat.item.ClientBlockAttackListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class BlockAttackListenerThings
{
    public static void init()
    {
        KeyboardEvents.POST_INPUT.register((window, key, scancode, action, modifiers) ->
        {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && client.options != null)
            {
                ItemStack mainStack = client.player.getMainHandItem();

                if (mainStack.getItem() instanceof ClientBlockAttackListener listener &&
                        (client.options.keyAttack.matches(key, scancode)
                                || client.options.keyAttack.matchesMouse(key))
                )
                {
                    if (client.options.keyAttack.isDown())
                    {
                        listener.onAttackBlock(mainStack, client.player);
                    }
                    else
                    {
                        listener.onFinishAttackBlock(mainStack, client.player);
                    }
                }
            }

            return false;
        });
    }
}
