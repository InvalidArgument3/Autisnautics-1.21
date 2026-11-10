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

package com.neep.neepmeat.client;

import com.neep.meatweapons.client.renderer.meatgun.MeatgunModuleRenderers;
import com.neep.neepmeat.client.meatgun.ReciprocatingDrillHeadRenderer;
import com.neep.neepmeat.client.meatgun.RotaryDrillHeadRenderer;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NMMeatgunModuleRenderers
{
    public static void init()
    {
        MeatgunModuleRenderers.register(NMMeatgunModules.BASE_ROCK_DRILL, c -> null);
        MeatgunModuleRenderers.register(NMMeatgunModules.ROCK_DRILL_HEAD, ReciprocatingDrillHeadRenderer::new);
        MeatgunModuleRenderers.register(NMMeatgunModules.IRON_ROTARY_DRILL_HEAD, c -> new RotaryDrillHeadRenderer(c, NMExtraModels.IRON_ROTARY_DRILL_HEAD));
        MeatgunModuleRenderers.register(NMMeatgunModules.MEAT_STEEL_ROTARY_DRILL_HEAD, c -> new RotaryDrillHeadRenderer(c, NMExtraModels.MEAT_STEEL_ROTARY_DRILL_HEAD));
        MeatgunModuleRenderers.register(NMMeatgunModules.DIAMOND_ROTARY_DRILL_HEAD, c -> new RotaryDrillHeadRenderer(c, NMExtraModels.DIAMOND_ROTARY_DRILL_HEAD));
    }
}
