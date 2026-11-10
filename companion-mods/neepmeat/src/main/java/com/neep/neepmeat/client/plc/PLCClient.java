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

package com.neep.neepmeat.client.plc;

import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.recipe.ItemWorkpiece;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@OnlyIn(Dist.CLIENT)
public class PLCClient
{
    public static void init()
    {
        ItemTooltipCallback.EVENT.register((stack, context, lines) ->
        {
            if (ItemWorkpiece.has(stack))
            {
                NMComponents.WORKPIECE.maybeGet(stack).ifPresent(workpiece ->
                {
                    var steps = workpiece.getSteps();
                    if (!steps.isEmpty())
                    {
                        lines.add(Component.translatable("message.neepmeat.workpiece.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                        for (var entry : steps)
                        {
                            entry.appendText(lines);
                        }
                    }
                });
            }
        });
    }
}
