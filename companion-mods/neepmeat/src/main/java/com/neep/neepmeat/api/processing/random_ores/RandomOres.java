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

package com.neep.neepmeat.api.processing.random_ores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.mixin.feature.CountPlacementModifierAccessor;
import com.neep.meatlib.resource.FabricResources.ResourceReloadListenerKeys;
import com.neep.meatlib.resource.FabricResources.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public class RandomOres implements SimpleSynchronousResourceReloadListener
{
    public static final RandomOres INSTANCE = new RandomOres();

    private final Map<BlockState, MutableEntry> stateToTempEntry = new HashMap<>();
    private final List<Entry> entries = new ArrayList<>();

    public static void init()
    {
        DataPackPostProcess.AFTER_DATA_PACK_LOAD.register(INSTANCE::postProcess);

        Registry.register(WeightModifier.REGISTRY, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "biome_list"), BiomeListWeightModifier.CODEC);
        Registry.register(WeightModifier.REGISTRY, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "biome_tag"), BiomeTagWeightModifier.CODEC);
    }

    public RandomOreProvider makeProvider(Level world, BlockPos pos, RandomSource random)
    {
        return new RandomOreProvider(world, pos, random, entries);
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies()
    {
        return List.of(
                ResourceReloadListenerKeys.TAGS
        );
    }

    private void postProcess(MinecraftServer server)
    {
        List<MutableEntry> entryList = new ArrayList<>();

        Registry<PlacedFeature> placed = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);

        placed.forEach(placedFeature ->
        {
            // Only proceed if the attached feature is an ore feature.
            ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();
            if (configuredFeature.config() instanceof OreConfiguration oreFeatureConfig)
            {
                for (var placement : placedFeature.placement())
                {
                    // Only support CountPlacementModifier since most 'ore' ores use it.
                    if (placement instanceof CountPlacement count)
                    {
                        IntProvider intProvider = ((CountPlacementModifierAccessor) count).getCount();
                        int maxCount = intProvider.getMaxValue();

                        oreFeatureConfig.targetStates.forEach(t ->
                        {
                            if (t.state.is(NMTags.CHARNEL_PUMP_OUTPUT_ORES))
                            {
                                // Multiply count by size to hopefully get the expected number of ore blocks per chunk.
                                entryList.add(new MutableEntry(t.state, maxCount * oreFeatureConfig.size));
                            }
                        });
                    }
                }
            }
        });

        // Combine identical BlockStates
        for (var entry : entryList)
        {
            MutableEntry inMap = stateToTempEntry.computeIfAbsent(entry.state, b -> new MutableEntry(b, 0));
            inMap.count += entry.count;
        }

        entries.addAll(stateToTempEntry.values().stream().map(
                e -> new Entry(e.state, e.count, e.modifiers)).toList());
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "random_ores");
    }

    @Override
    public void reload(ResourceManager manager)
    {
        stateToTempEntry.clear();
        entries.clear();

        for (ResourceLocation id : manager.listResources("random_ores", path -> path.getPath().endsWith(".json")).keySet())
        {
            if (manager.getResource(id).isPresent())
            {
                try (InputStream stream = manager.getResource(id).get().open())
                {
                    Reader reader = new InputStreamReader(stream);
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    JsonObject rootObject = (JsonObject) rootElement;

                    // Read target BlockState, count and an optional list of modifiers.
                    BlockState target = BlockState.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(rootObject, "state"))
                            .resultOrPartial(NeepMeat.LOGGER::error).orElseThrow();

                    int count = GsonHelper.getAsInt(rootObject, "count");

                    List<WeightModifier> modifiers = parseModifiers(rootObject);

                    MutableEntry entry = new MutableEntry(target, count, modifiers);
                    stateToTempEntry.put(target, entry);
                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error while reading random ores " + id.toString(), e);
                }
            }
        }
    }

    private List<WeightModifier> parseModifiers(JsonObject root)
    {
        List<WeightModifier> list = new ArrayList<>();
        if (root.has("modifiers"))
        {
            JsonArray modifiers = GsonHelper.getAsJsonArray(root, "modifiers");
            for (int i = 0; i < modifiers.size(); ++i)
            {
                JsonObject modifierObject = modifiers.get(i).getAsJsonObject();

                Codec<WeightModifier> codec = WeightModifier.CODEC.dispatch("type", WeightModifier::getCodec, Function.identity());
                WeightModifier weightModifier = codec.parse(JsonOps.INSTANCE, modifierObject)
                        .resultOrPartial(NeepMeat.LOGGER::error).orElseThrow();

                list.add(weightModifier);
            }
        }

        return list;
    }

    public record Entry(BlockState state, float weight, List<WeightModifier> modifiers)
    {
        public float modifiedWeight(Level world, BlockPos pos)
        {
            float weight = weight();
            for (var modifier : modifiers)
            {
                weight = modifier.apply(weight, world, pos);
            }
            return weight;
        }
    }

    private static class MutableEntry
    {
        final List<WeightModifier> modifiers;
        final BlockState state;
        int count;

        public MutableEntry(BlockState state, int count)
        {
            this(state, count, new ArrayList<>());
        }

        public MutableEntry(BlockState target, int count, List<WeightModifier> modifiers)
        {
            this.state = target;
            this.count = count;
            this.modifiers = modifiers;
        }
    }

}
