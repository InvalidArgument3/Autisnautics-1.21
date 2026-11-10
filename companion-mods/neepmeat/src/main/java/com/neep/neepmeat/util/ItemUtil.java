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

package com.neep.neepmeat.util;

import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;

public final class ItemUtil {
    private ItemUtil() {}

    /** A held fluid pipe passes the click through so the pipe can connect. */
    public static boolean playerHoldingPipe(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof FluidPipeBlock;
    }
}
