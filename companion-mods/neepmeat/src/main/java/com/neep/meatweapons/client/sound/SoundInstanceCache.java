package com.neep.meatweapons.client.sound;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/// For associating instances with entity IDs.
/// One instance per entity.
/// Clears automatically when the client disconnects.
public class SoundInstanceCache<T>
{
    private final Int2ObjectMap<T> map = new Int2ObjectOpenHashMap<>();

    public SoundInstanceCache()
    {
        ClientPlayConnectionEvents.DISCONNECT.register((clientPlayNetworkHandler, minecraftClient) ->
        {
            clear();
        });
    }

    public void clear()
    {
        map.clear();
    }

    public T get(int id)
    {
        return map.get(id);
    }

    public T add(int id, T t)
    {
        map.put(id, t);
        return t;
    }

    public T remove(int id)
    {
        return map.remove(id);
    }
}
