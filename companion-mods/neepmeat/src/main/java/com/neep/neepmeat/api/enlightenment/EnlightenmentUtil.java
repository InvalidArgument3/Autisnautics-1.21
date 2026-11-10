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

package com.neep.neepmeat.api.enlightenment;

import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.init.NMComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class EnlightenmentUtil
{
    public static final float THRESHOLD_EXTREME = 20000;
    public static final float THRESHOLD_HIGH = 10000;
    public static final float THRESHOLD_MEDIUM = 5000;
    public static final float THRESHOLD_LOW = 2500;
    public static final float THRESHOLD_NEGLIGIBLE = 1000;

    public static void init()
    {
    }

    public static float getEventProbability(Player player)
    {
        float enlightenment = getEnlightenment(player);

        float p1 = Mth.clamp(enlightenment / getMaxEnlightenment(player), 0, 1);

        return p1 * 0.1f;
    }

    public static float getEnlightenment(Player player)
    {
        return NMComponents.ENLIGHTENMENT_MANAGER.get(player).getTotal();
//        return player.getStatHandler().getStat(ENLIGHTENMENT);
    }

    public static void applyDose(ServerPlayer player, float baseAmount, double sqDistance, double split)
    {
        EnlightenmentManager manager = NMComponents.ENLIGHTENMENT_MANAGER.get(player);
        manager.exposeDose(baseAmount, sqDistance, split);
    }

    public static int getMaxEnlightenment(Player player)
    {
        return (int) (THRESHOLD_EXTREME);
    }
}
