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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.api.item.ScrollableItem;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.util.NMMaths;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemPortBlockItem extends BaseBlockItem implements ScrollableItem
{
    private final List<Item> instances;

    public ItemPortBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings, List<Item> instances)
    {
        super(block, ctx, itemSettings.tooltip((stack, tooltip) ->
                tooltip.add(NeepMeat.translationKey("item", "port.scrollable"))));
        this.instances = instances;
        instances.add(this);
    }

    public static MeatlibBlock.ItemFactory forList(List<Item> itemPorts)
    {
        return (block, ctx, settings) -> new ItemPortBlockItem(block, ctx, settings, itemPorts);
    }

    @Override
    public void onScroll(Player player, ItemStack stack, InteractionHand hand, double amount)
    {
        int idx = instances.indexOf(this);
        int newIdx = NMMaths.wrapIndex(idx + ((int) amount), instances.size());

        ScrollableItem.swapStack(player, hand, stack, instances.get(newIdx));
    }
}
