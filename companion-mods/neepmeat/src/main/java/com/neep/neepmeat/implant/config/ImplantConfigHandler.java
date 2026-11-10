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

package com.neep.neepmeat.implant.config;

import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepmeat.implant.entity.EntityImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface ImplantConfigHandler extends WidgetHandler
{
    Map<ResourceLocation, Factory<?, ?>> FACTORIES = new HashMap<>();

    static <T extends ImplantConfigHandler, I extends EntityImplant> void register(ResourceLocation id, Factory<T, I> factory)
    {
        FACTORIES.put(id, factory);
    }

    @Nullable
    static Factory<?, ?> get(ResourceLocation id)
    {
        return FACTORIES.get(id);
    }

    @FunctionalInterface
    interface Factory<T extends ImplantConfigHandler, I extends EntityImplant>
    {
        T create(ImplantManagerScreenHandler handler, I implant, Player player);

        default T createCast(ImplantManagerScreenHandler handler, EntityImplant implant, Player player)
        {
            // I love incorrectly using the type system!
            return create(handler, (I) implant, player);
        }
    }
}
