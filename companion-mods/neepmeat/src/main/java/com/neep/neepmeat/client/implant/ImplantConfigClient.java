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

package com.neep.neepmeat.client.implant;

import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class ImplantConfigClient
{
    public static final Map<ResourceLocation, Factory<?>> FACTORIES = new HashMap<>();

    public static void register(ResourceLocation id, Factory<?> factory)
    {
        FACTORIES.put(id, factory);
    }

    public interface Factory<T extends ImplantConfigPanel>
    {
        T create(ImplantManagerScreenHandler handler, ImplantManagerScreen screen);
    }
}
