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

package com.neep.meatweapons.item;

import com.neep.meatweapons.network.MWAttackC2SPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface TriggerReceiver
{
    default void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand) {}
    default void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand) {}
    default void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand) {}
}
