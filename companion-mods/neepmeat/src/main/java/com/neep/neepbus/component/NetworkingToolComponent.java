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

package com.neep.neepbus.component;

import com.neep.neepbus.client.item.NetworkingToolClient;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

public class NetworkingToolComponent extends ItemComponent
{
    public NetworkingToolComponent(ItemStack stack)
    {
        super(stack);
    }

    public NetworkingToolClient.Mode getMode()
    {
        return NetworkingToolClient.Mode.values()[getInt("mode")];
    }
}
