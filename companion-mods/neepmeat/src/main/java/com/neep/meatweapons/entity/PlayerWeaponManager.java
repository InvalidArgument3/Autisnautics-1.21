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

package com.neep.meatweapons.entity;

import com.neep.meatweapons.item.GunItem;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerWeaponManager
{
    protected boolean[] mainStatus = new boolean[2];
    protected boolean[] offStatus = new boolean[2];

    protected final Player player;

    public PlayerWeaponManager(Player playerEntity)
    {
        this.player = playerEntity;
    }

    public void tick()
    {
        GunItem mainItem = GunItem.getGun(player.getMainHandItem());
        GunItem offItem = GunItem.getGun(player.getOffhandItem());

        double pitch = Math.toRadians(player.getViewXRot(1));
        double yaw = Math.toRadians(player.getViewYRot(1));

        ItemStack stack;
        if (mainItem != null)
        {
            stack = player.getMainHandItem();
            if (mainStatus[0]) mainItem.tickTrigger(player.level(), player, stack, MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.MAIN, InteractionHand.MAIN_HAND);
            if (mainStatus[1]) mainItem.tickTrigger(player.level(), player, stack, MWAttackC2SPacket.TRIGGER_SECONDARY, pitch, yaw, MWAttackC2SPacket.HandType.MAIN, InteractionHand.MAIN_HAND);
        }

        if (offItem != null)
        {
            stack = player.getOffhandItem();
            if (offStatus[0]) offItem.tickTrigger(player.level(), player, stack, MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.OFF, InteractionHand.OFF_HAND);
            if (offStatus[1]) offItem.tickTrigger(player.level(), player, stack, MWAttackC2SPacket.TRIGGER_SECONDARY, pitch, yaw, MWAttackC2SPacket.HandType.OFF, InteractionHand.OFF_HAND);
        }
    }

    public void updateStatus(MWAttackC2SPacket.HandType handType, int triggerId, MWAttackC2SPacket.ActionType actionType)
    {

        if (handType.mainHand())
        {
            mainStatus[0] = triggerId == MWAttackC2SPacket.TRIGGER_PRIMARY ? actionType.pressed() : mainStatus[0];
            mainStatus[1] = triggerId == MWAttackC2SPacket.TRIGGER_SECONDARY ? actionType.pressed() : mainStatus[1];
        }

        if (handType.offHand())
        {
            offStatus[0] = triggerId == MWAttackC2SPacket.TRIGGER_PRIMARY ? actionType.pressed() : offStatus[0];
            offStatus[1] = triggerId == MWAttackC2SPacket.TRIGGER_SECONDARY ? actionType.pressed() : offStatus[1];
        }
    }
}
