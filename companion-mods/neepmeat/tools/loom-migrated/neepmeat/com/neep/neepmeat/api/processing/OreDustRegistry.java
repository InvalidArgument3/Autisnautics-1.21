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

package com.neep.neepmeat.api.processing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.util.StringTokenView;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OreDustRegistry implements SimpleSynchronousResourceReloadListener
{
    public static final OreDustRegistry ORE_DUST = new OreDustRegistry(new ResourceLocation(NeepMeat.NAMESPACE, "ore_dust"), () -> NMItems.ORE_DUST);
    public static final OreDustRegistry CRUSHED_ORE = new OreDustRegistry(new ResourceLocation(NeepMeat.NAMESPACE, "crushed_ore"), () -> NMItems.CRUSHED_ORE);

    // Entries are lazily created as requested
    private final Map<Item, Entry> dustMap = new IdentityHashMap<>();
    private final ResourceLocation name;
    private final Supplier<Item> item;

    // For registering a true item in place of an automatically generated dust
    private final Map<Item, Entry> persistentOverrides = new IdentityHashMap<>();

    private final GlobalChannelManager<Consumer<Map<Item, Entry>>> syncChannel;

    public static void init()
    {
    }

    private OreDustRegistry(ResourceLocation name, Supplier<Item> item)
    {
        this.name = name;
        this.item = item;

        DataPackPostProcess.SYNC.register(this::sync);

        syncChannel = GlobalChannelManager.create(
                new ResourceLocation(name.getNamespace(), "sync_" + name.getPath()),
                ChannelFormat.builder(ParamCodecs.<Consumer<Map<Item, Entry>>>as(Consumer.class))
                        .param(ParamCodecs.map(ParamCodecs.as(Map.class), ParamCodecs.registryValue(Item.class, BuiltInRegistries.ITEM), Entry.PARAM_CODEC))
                        .build());
        syncChannel.receiver(EnvType.CLIENT, itemEntryMap ->
        {
//            dustMap.clear();
//            dustMap.putAll(itemEntryMap);
        });
    }

    // Doesn't work consistently
    public void addToItemGroup(CreativeModeTab.ItemDisplayParameters displayContext, CreativeModeTab.Output entries)
    {
        dustMap.forEach((i, e) -> entries.accept(e.variant.toStack()));
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return name;
    }

    public Entry get(Item rawOre)
    {
        return dustMap.computeIfAbsent(rawOre, item1 -> new Entry(createItemVariant(item1), -1));
    }

    @Nullable
    public Entry getClient(Item rawOre)
    {
        return dustMap.get(rawOre);
    }

    private ItemVariant createItemVariant(Item rawOre)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("ore_item", BuiltInRegistries.ITEM.getKey(rawOre).toString());
        return ItemVariant.of(item.get(), nbt);
    }

    /// Responsible for reading overrides from the data pack.
    /// Further entries are created as requested during {@link DataPackPostProcess#AFTER_DATA_PACK_LOAD}.
    @Override
    public void onResourceManagerReload(ResourceManager manager)
    {
        dustMap.clear();

        readOverrides(manager);
    }

    private void sync(MinecraftServer server, Set<ServerPlayer> players)
    {
        players.forEach(p -> syncChannel.emitter(p).accept(dustMap));
    }

    private void readOverrides(ResourceManager manager)
    {
        dustMap.clear();

        for (ResourceLocation id : manager.listResources("ore_dust/overrides", path -> path.getPath().endsWith(".json")).keySet())
        {
            if (manager.getResource(id).isPresent())
            {
                try (InputStream stream = manager.getResource(id).get().open())
                {
                    Reader reader = new InputStreamReader(stream);
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    JsonObject rootObject = (JsonObject) rootElement;

                    String resourceName = GsonHelper.getAsString(rootObject, "output");
                    Item result = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(resourceName));
                    ItemVariant itemVariant = createItemVariant(result);

                    int tint = -1;
                    if (rootObject.has("tint"))
                    {
                        var tintElement = rootObject.get("tint");
                        tint = StringTokenView.parseInteger(tintElement.getAsString());
                    }

                    Entry entry = new Entry(itemVariant, tint);
                    dustMap.put(result, entry);
                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error while reading ore dust override {}", id.toString(), e);
                }
            }
        }

        dustMap.putAll(persistentOverrides);
    }

    public Map<Item, Entry> getValues()
    {
        return dustMap;
    }

    public void registerPersistent(Item key, Entry value)
    {
        persistentOverrides.put(key, value);
    }

    public record Entry(ItemVariant variant, int tint)
    {
        public static final ParamCodec<Entry> PARAM_CODEC = RecordParamCodec.builder(Entry.class)
                .param(ParamCodecs.ITEM_VARIANT)
                .param(ParamCodec.INT)
                .build();
    }
}
