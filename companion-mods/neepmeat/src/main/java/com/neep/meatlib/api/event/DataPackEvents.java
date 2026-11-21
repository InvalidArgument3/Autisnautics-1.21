package com.neep.meatlib.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;

public interface DataPackEvents
{
    Event<DataPackEvents> IMMEDIATELY_AFTER_RELOAD = EventFactory.createArrayBacked(DataPackEvents.class, listeners -> (server, registryManager) ->
    {
        for (var listener : listeners)
            listener.apply(server, registryManager);
    });

    void apply(ReloadableServerResources dataPack, RegistryAccess registryManager);
}
