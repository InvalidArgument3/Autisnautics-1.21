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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.implant.entity.PinealEyeImplant;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.init.NMItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SightUtil
{
    public static final ResourceLocation BOG_ZOMBIE_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/bog_zombie.png");

    public static boolean canPlayerSee(Player player)
    {
        boolean goggles = player.getInventory().getArmor(3).is(NMItems.GOGGLES);

        boolean pinealEye = PlayerImplantManager.get(player).getImplant(PinealEyeImplant.ID) != null;

        return goggles || pinealEye;
    }

    public static boolean canPlayerSeeBog(Player player)
    {
        var manager = EnlightenmentManager.get(player);
        return manager.getTotal() > 400 || canPlayerSee(player);
    }

    public static ResourceLocation getZombieTexture(ResourceLocation original)
    {
        var player = Minecraft.getInstance().player;
        return player != null && SightUtil.canPlayerSeeBog(player) ? BOG_ZOMBIE_TEXTURE : original;
    }
}
