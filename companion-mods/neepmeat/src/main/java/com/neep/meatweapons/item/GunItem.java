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

import com.neep.meatweapons.meatgun.MeatgunModuleCapability;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface GunItem extends BeamEffectProvider, TriggerReceiver
{
    static GunItem getGun(ItemStack stack)
    {
        return stack.getItem() instanceof GunItem gun ? gun : null;
    }

    Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack);

    default void triggerClient(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType)
    {
    }

    default void releaseClient(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType)
    {
    }

    default void playSound(Level world, LivingEntity entity, GunSounds sound) {}

    @Override
    default void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full) {}

    @Deprecated
    default void syncAnimation(Level world, LivingEntity player, ItemStack stack, String animation, boolean broadcast) {}

    @Deprecated
    default int getShots(ItemStack stack, int trigger)
    {
        return 0;
    }

    // Removes ammunition from inventory. Returns null if none present.
    static ItemStack removeStack(Item type, Player player)
    {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i)
        {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem().equals(type))
            {
                return stack;
            }
        }
        return null;
    }

    default boolean redirectClicks() { return true; };

    default boolean redirectTrigger(ItemStack stack, MeatgunModuleCapability trigger) { return true; }

    enum GunSounds
    {
        FIRE_PRIMARY,
        FIRE_SECONDARY,
        RELOAD,
        EMPTY,
    }
}
