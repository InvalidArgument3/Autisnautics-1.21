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

package com.neep.meatweapons.init;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.block.TinkerTableBlock;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.init.NMBlocks;
import net.minecraft.world.level.block.Block;

@RegisterMe(MeatWeapons.NAMESPACE)
public class MWBlocks
{
    public static final RegistrationContext C = new RegistrationContext(MeatWeapons.NAMESPACE, () -> MeatWeapons.WEAPONS);

    public static final Block TINKER_TABLE = new TinkerTableBlock(C ,ItemSettings.block(), MeatlibBlockSettings.copyOf(NMBlocks.RUSTY_METAL_BLOCK).properties());

    public static void init()
    {

    }
}
