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

package com.neep.meatlib.item;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.item.ScrollableItem;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.GlobalChannelManager;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ScrollableItems
{
    public static GlobalChannelManager<Send> SEND_C2S = GlobalChannelManager.create(
            new ResourceLocation(MeatLib.NAMESPACE, "item/scrollable_item"),
            ChannelFormat.builder(Send.class).param(ParamCodec.DOUBLE).build());

    public static void init()
    {
        SEND_C2S.receiverHandler(EnvType.SERVER, (player, buf, responseSender) -> (amount) ->
        {
            ItemStack mainStack = player.getMainHandItem();
            if (mainStack.getItem() instanceof ScrollableItem scrollableItem)
            {
                scrollableItem.onScroll(player, mainStack, InteractionHand.MAIN_HAND, amount);
            }
        });
    }

    public interface Send
    {
        void onScroll(double amount);
    }
}
