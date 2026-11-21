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

package com.neep.neepmeat.init;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.enlightenment.EnlightenmentEventManager;
import com.neep.neepmeat.enlightenment.event.PlaceWasteEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.event.RandomClockEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.event.ReminaEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.event.ScareVillagersEnlightenmentEvent;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class NMEnlightenmentEvents
{
    public static final EnlightenmentEvent.Factory RANDOM_CLOCK = new EnlightenmentEvent.SimpleFactory(RandomClockEnlightenmentEvent::new);
    public static final EnlightenmentEvent.Factory SCARE_VILLAGERS = new EnlightenmentEvent.SimpleFactory(ScareVillagersEnlightenmentEvent::new);
    public static final EnlightenmentEvent.Factory OBJECT_ABOVE = new EnlightenmentEvent.SimpleFactory(ReminaEnlightenmentEvent::new, ReminaEnlightenmentEvent::canSpawn);
    public static final EnlightenmentEvent.Factory PLACE_ACTIVE_WASTE = new EnlightenmentEvent.SimpleFactory((world, player) -> new PlaceWasteEnlightenmentEvent(world, player, IntrusionReactor.ACTIVE_WASTE.defaultBlockState()), PlaceWasteEnlightenmentEvent::canSpawn);
    public static final EnlightenmentEvent.Factory PLACE_RUBBLE = new EnlightenmentEvent.SimpleFactory((world, player) -> new PlaceWasteEnlightenmentEvent(world, player, NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState()), PlaceWasteEnlightenmentEvent::canSpawn);

    public static void init()
    {
        register(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "random_clock"), RANDOM_CLOCK);
        register(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "scare_villagers"), SCARE_VILLAGERS);
        register(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "above"), OBJECT_ABOVE);
        register(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "place_active_waste"), PLACE_ACTIVE_WASTE);
        register(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "place_rubble"), PLACE_RUBBLE);
    }

    public static <T extends EnlightenmentEvent.Factory> T register(ResourceLocation id, T t)
    {
        return Registry.register(EnlightenmentEventManager.EVENTS, id, t);
    }
}
