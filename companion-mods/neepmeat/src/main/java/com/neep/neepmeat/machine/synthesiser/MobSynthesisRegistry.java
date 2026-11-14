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

package com.neep.neepmeat.machine.synthesiser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.resource.FabricResources.SimpleSynchronousResourceReloadListener;
import com.neep.meatlib.transfer.FluidConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public class MobSynthesisRegistry implements SimpleSynchronousResourceReloadListener
{
    private static final MobSynthesisRegistry INSTANCE = new MobSynthesisRegistry();

    private final Map<EntityType<?>, Entry> entries = Maps.newHashMap();
    private final Set<EntityType<?>> denyTypes = Sets.newHashSet();

    @Nullable
    public static Entry get(Entity entity)
    {
        return get(entity.getType());
    }

    @Nullable
    public static Entry get(EntityType<?> type)
    {
        return getInstance().getEntry(type);
    }

    @Nullable
    public Entry getEntry(EntityType<?> type)
    {
        if (denyTypes.contains(type))
            return null;

        return entries.get(type);
    }

    public static long meatFromSize(Entity entity)
    {
        return (long) Math.floor(entity.getBbHeight() * entity.getBbWidth() * entity.getBbWidth() * FluidConstants.BUCKET);
    }

    public static long meatFromSize(EntityType<?> entity)
    {
        return (long) Math.floor(entity.getHeight() * entity.getWidth() * entity.getWidth() * FluidConstants.BUCKET);
    }

    public static MobSynthesisRegistry getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager)
    {
        entries.clear();
        denyTypes.clear();

        initDefaults();

        for (ResourceLocation id : manager.listResources("cloning", path -> path.getPath().endsWith(".json")).keySet())
        {
            if (manager.getResource(id).isPresent())
            {
                try (InputStream stream = manager.getResource(id).get().open())
                {
                    Reader reader = new InputStreamReader(stream);
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    JsonObject rootObject = (JsonObject) rootElement;

                    JsonArray allowJson = GsonHelper.getAsJsonArray(rootObject, "extra");
                    allowJson.forEach(jsonElement ->
                    {
                        if (jsonElement instanceof JsonObject jsonObject)
                        {
                            if (jsonObject.size() != 1)
                                throw new JsonSyntaxException("Expected entity ID string or object containing entity ID and meat amount");

                            jsonObject.entrySet().forEach(entry ->
                            {
                                String entityIdStr = entry.getKey();
                                ResourceLocation entityId = ResourceLocation.parse(entityIdStr);
                                int meat = entry.getValue().getAsInt();

                                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

                                entries.put(entityType, new Entry(entityType, meat, 60));
                            });
                        }
                        else
                        {
                            String entityIdStr = jsonElement.getAsString();
                            ResourceLocation entityId = ResourceLocation.parse(entityIdStr);
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                            long meat = meatFromSize(entityType);

                            entries.put(entityType, new Entry(entityType, meat, 60));
                        }
                    });

                    JsonArray denyJson = GsonHelper.getAsJsonArray(rootObject, "deny");
                    denyJson.forEach(jsonElement ->
                    {
                        GsonHelper.convertToString(jsonElement, jsonElement.toString());
                        String entityIdStr = jsonElement.getAsString();
                        ResourceLocation entityId = ResourceLocation.parse(entityIdStr);
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                        denyTypes.add(entityType);
                    });
                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error occurred while loading cloning json " + id.toString(), e);
                }
            }

        }
    }

    private void initDefaults()
    {
        // Generate entries for all spawn eggs based on bounding box size
        SpawnEggItem.eggs().forEach(eggItem ->
        {
            EntityType<?> type = eggItem.getType(null);
            if (type == null) // Some mods (Ultracraft) might add spawn eggs with null entity type
                return;
            this.entries.put(type, new Entry(type, meatFromSize(type), 60));
        });
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "cloning");
    }

    public record Entry(EntityType<?> type, long meat, int time)
    {
    }
}
