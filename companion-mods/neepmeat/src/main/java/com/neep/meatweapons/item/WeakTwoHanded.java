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

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface WeakTwoHanded
{
    default boolean displayArmFirstPerson(ItemStack stack, InteractionHand hand)
    {
        return true;
    }

    default ArmPose getThirdPersonArmPose(AbstractClientPlayer player, ItemStack stackInHand, InteractionHand hand)
    {
        return ArmPose.BOW_AND_ARROW;
    }

    enum ArmPose
    {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false),
        TOOT_HORN(false),
        BRUSH(false);

        private final boolean twoHanded;

        ArmPose(boolean twoHanded)
        {
            this.twoHanded = twoHanded;
        }

        public boolean isTwoHanded()
        {
            return this.twoHanded;
        }
    }
}
