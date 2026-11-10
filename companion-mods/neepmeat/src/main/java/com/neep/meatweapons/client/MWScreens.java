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

package com.neep.meatweapons.client;

import com.neep.meatweapons.client.screen.meatgun.TinkerTableScreen;
import com.neep.meatweapons.init.MWScreenHandlers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.screens.MenuScreens;

@OnlyIn(Dist.CLIENT)
public class MWScreens
{
    public static void init()
    {
        net.fabricmc.fabric.api.client.screenhandler.v1.HandledScreens.register(MWScreenHandlers.MEATGUN, TinkerTableScreen::new);
    }
}
