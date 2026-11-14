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

import com.google.common.collect.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.mixin.RecipeManagerAccessor;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.meatlib.util.StringTokenView;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.fluid.ore_fat.OreFatFluidFactory;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.item.AbstractOreDustItem;
import com.neep.meatlib.resource.FabricResources.SimpleSynchronousResourceReloadListener;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class OreFatRegistry implements SimpleSynchronousResourceReloadListener
{
    public static final OreFatRegistry INSTANCE = new OreFatRegistry();
    public static final GlobalChannelManager<OreFatSyncData.SyncOreFats> SYNC = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "ore_fat"),
            ChannelFormat.builder(OreFatSyncData.SyncOreFats.class).param(OreFatSyncData.PARAM_CODEC).build());

    private final Set<TagKey<Item>> generateForTags = Sets.newHashSet();
    private final Set<ResourceLocation> generateForItems = Sets.newHashSet();
    private final Set<ResourceLocation> excludeItems = Sets.newHashSet();

    private final Map<Item, Entry> outputToEntry = Maps.newHashMap();
    private final Map<Item, Entry> inputToEntry = Maps.newHashMap();
    private final Map<ResourceLocation, Entry> nbtToEntry = Maps.newHashMap();

    private final Multimap<Entry, Item> collectedInputs = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);

    public static void init()
    {
        DataPackPostProcess.AFTER_DATA_PACK_LOAD.register(DataPackPostProcess.ORE_FAT, INSTANCE::generate);
        DataPackPostProcess.SYNC.register(INSTANCE::sync);
    }

    @Nullable
    public static Entry get(CompoundTag nbt)
    {
        return INSTANCE.nbtToEntry.get(getItemId(nbt));
    }

    public static Entry get(ResourceLocation entryId)
    {
        return INSTANCE.nbtToEntry.get(entryId);
    }

    public static ResourceLocation getItemId(CompoundTag nbt)
    {
        return ResourceLocation.tryParse(nbt.getString("item"));
    }

    @Nullable
    public static Entry getFromInput(Item item)
    {
        return INSTANCE.inputToEntry.get(item);
    }

    ///  Takes into account dusts
    @Nullable
    public static Entry getFromRecipeInput(ItemVariant variant)
    {
        if (variant.getItem() instanceof AbstractOreDustItem dustItem)
        {
            return getFromInput(dustItem.getOreItem(variant.toStack()));
        }

        return getFromInput(variant.getItem());
    }

    @Nullable
    public static Entry getFromVariant(FluidVariant variant)
    {
        if (variant.getObject() instanceof OreFatFluidFactory.Main)
        {
            CompoundTag nbt = variant.getNbt();
            if (nbt != null)
                return INSTANCE.nbtToEntry.get(getItemId(nbt));
        }
        return null;
    }

    private void addTag(ResourceLocation id)
    {
        generateForTags.add(TagKey.create(BuiltInRegistries.ITEM.key(), id));
    }

    private void generate(MinecraftServer server)
    {
        BallMillRecipeGenerator.INSTANCE.clear();

        RecipeManagerAccessor manager = (RecipeManagerAccessor) server.getRecipeManager();
        Map<ResourceLocation, SmeltingRecipe> smeltingRecipes = manager.callGetAllOfType(RecipeType.SMELTING);
        Map<ResourceLocation, BlastingRecipe> blastingRecipes = manager.callGetAllOfType(RecipeType.BLASTING);
        Streams.concat(smeltingRecipes.values().stream(), blastingRecipes.values().stream()).forEach(recipe ->
        {
            List<Ingredient> ingredients = recipe.getIngredients();
            ItemStack output = recipe.getResultItem(server.registryAccess());
            testIngredient(ingredients, output);
        });

        NeepMeat.LOGGER.info("Generated {} ore fat routes", inputToEntry.size());
    }

    private void sync(MinecraftServer server, Set<ServerPlayer> players)
    {
        OreFatSyncData packet = new OreFatSyncData(nbtToEntry);
        for (ServerPlayer player : players)
        {
            SYNC.emitter(player).apply(packet);
        }
    }

    public void onPacket(OreFatSyncData t)
    {
        nbtToEntry.clear();
        nbtToEntry.putAll(t.nbtToEntry());
    }

    /**
     * Runs through the inputs and checks if they are raw ores.
     * If this is the case, it is probably safe to generate an ore fat path from input to output.
     */
    private void testIngredient(Collection<Ingredient> input, ItemStack output)
    {
        if (output.isEmpty())
            return;

        if (input.isEmpty())
            return;

        for (Ingredient ingredient : input)
        {
            if (ingredient.isEmpty())
                continue;

            for (ItemStack stack : ingredient.getItems())
            {
                ResourceLocation itemId = stack.getItem().builtInRegistryHolder().key().location();
                if (!excludeItems.contains(itemId) && (stack.getTags().anyMatch(generateForTags::contains) || generateForItems.contains(itemId)))
                {
                    registerDefaultEntry(stack.getItem(), ItemVariant.of(output));
                }
            }
        }
    }

    private void registerDefaultEntry(Item input, ItemVariant output)
    {
        CompoundTag nbt = createNbt(output.getItem());
        ResourceLocation id = createId(output.getItem());

        Entry entry = outputToEntry.computeIfAbsent(output.getItem(), i -> new Entry(
                createName(output.getItem(), NMFluids.DIRTY_ORE_FAT),
                createName(output.getItem(), NMFluids.CLEAN_ORE_FAT),
                output, nbt, NeepMeat.CONFIG.oreFat.defaultRenderingYield_1.getRealValue(), NeepMeat.CONFIG.oreFat.defaultTrommelYield_1.getRealValue(), -1));

        nbtToEntry.put(id, entry);
        addInput(entry, input);
    }

    public static RegistryRecipeInput<Item> createRecipeInput(ItemVariant itemVariant)
    {
        return RecipeInputs.of(itemVariant, 1);
    }

    /**
     * @param outputItem Output item
     * @return The NBT compound that held by all dirty and clean FluidVariants corresponding to the item.
     */
    private CompoundTag createNbt(Item outputItem)
    {
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putString("item", createId(outputItem).toString());
        return nbtCompound;
    }

    public ResourceLocation createId(Item outputItem)
    {
        return BuiltInRegistries.ITEM.getKey(outputItem);
    }

    private Component createName(Item outputItem, Block fluid)
    {
        return Component.translatable(fluid.getDescriptionId(), outputItem.getDescription());
    }

    public Item getItem(FluidVariant variant)
    {
        CompoundTag nbt = variant.getNbt();
        if (variant.getObject() instanceof OreFatFluidFactory.Main && nbt != null)
        {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse((nbt.getString("item"))));
        }
        return null;
    }

    public FluidVariant getDirty(Item ore)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("item", BuiltInRegistries.ITEM.getKey(ore).toString());
        return FluidVariant.of(NMFluids.STILL_DIRTY_ORE_FAT, nbt);
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "ore_fat");
    }

    /**
     * This works in a very convoluted way.
     * Fats have one output and multiple inputs. Therefore, fats can be identified by their output item.
     * First the main fat file is processed. This determines which tags will be used to generate fat fluids from
     * smelting recipes.
     * Overrides are then parsed. These pre-populate the maps with arbitrary entries that do not depend on recipes.
     * The generation step occurs after data packs have been reloaded. All smelting recipes are checked for inputs
     * and outputs that match the tags in fat.json.
     * Entries that already exist for a given output will not be replaced by generated entries.
     */
    @Override
    public void reload(ResourceManager manager)
    {
        inputToEntry.clear();
        nbtToEntry.clear();
        outputToEntry.clear();
        collectedInputs.clear();

        for (ResourceLocation id : manager.listResources("ore_fat", path -> path.getPath().endsWith("/fat.json")).keySet())
        {
            if (manager.getResource(id).isPresent())
            {
                try (InputStream stream = manager.getResource(id).get().open())
                {
                    Reader reader = new InputStreamReader(stream);
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    JsonObject rootObject = (JsonObject) rootElement;

                    if (rootObject.has("replace") && GsonHelper.getAsBoolean(rootObject, "replace"))
                    {
                        // Last file replaces previous ones
                        generateForTags.clear();
                        generateForItems.clear();
                        excludeItems.clear();
                    }

                    // Generate routes for smelting recipes whose inputs are in these tags
                    JsonArray generateForTags = GsonHelper.getAsJsonArray(rootObject, "generate_for_tags");
                    generateForTags.forEach(e ->
                    {
                        ResourceLocation tagId = ResourceLocation.tryParse(e.getAsString());
                        addTag(tagId);
                    });

                    JsonArray generateForItems = GsonHelper.getAsJsonArray(rootObject, "generate_for_items");
                    generateForItems.forEach(e ->
                    {
                        ResourceLocation itemId = ResourceLocation.tryParse(e.getAsString());

                        this.generateForItems.add(itemId);
                    });

                    JsonArray excludeItems = GsonHelper.getAsJsonArray(rootObject, "exclude_items", new JsonArray());
                    excludeItems.forEach(e ->
                    {
                        ResourceLocation itemId = ResourceLocation.tryParse(e.getAsString());

                        this.excludeItems.add(itemId);
                    });
                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error while reading ore fat " + id.toString(), e);
                }
            }
        }

        readOverrides(manager);
    }

    private void readOverrides(ResourceManager manager)
    {
        for (ResourceLocation id : manager.listResources("ore_fat/overrides", path -> path.getPath().endsWith(".json")).keySet())
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

                    Component dirtyFatName = createName(result, NMFluids.DIRTY_ORE_FAT);
                    if (rootObject.has("dirty_fat_name"))
                        dirtyFatName = Component.translatable(GsonHelper.getAsString(rootObject, "dirty_fat_name"));

                    Component cleanFatName = createName(result, NMFluids.CLEAN_ORE_FAT);
                    if (rootObject.has("clean_fat_name"))
                        cleanFatName = Component.translatable(GsonHelper.getAsString(rootObject, "clean_fat_name"));

                    float renderingYield = NeepMeat.CONFIG.oreFat.defaultRenderingYield_1.getRealValue();
                    if (rootObject.has("rendering_yield"))
                        renderingYield = GsonHelper.getAsFloat(rootObject, "rendering_yield");

                    float trommelYield = NeepMeat.CONFIG.oreFat.defaultTrommelYield_1.getRealValue();
                    if (rootObject.has("trommel_yield"))
                        trommelYield = GsonHelper.getAsFloat(rootObject, "trommel_yield");

                    int tint = -1;
                    if (rootObject.has("tint"))
                    {
                        var tintElement = rootObject.get("tint");
                        tint = StringTokenView.parseInteger(tintElement.getAsString());
                    }

                    Entry entry = new Entry(dirtyFatName, cleanFatName, ItemVariant.of(result), createNbt(result), renderingYield, trommelYield, tint);

                    outputToEntry.put(result, entry);
                    nbtToEntry.put(createId(result), entry);

                    if (rootObject.has("inputs"))
                    {
                        JsonArray inputs = GsonHelper.getAsJsonArray(rootObject, "inputs");
                        for (var inputObject : inputs)
                        {
                            String inputName = inputObject.getAsString();
                            Item input = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(inputName));
                            addInput(entry, input);
                        }
                    }
                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error while reading ore fat override " + id.toString(), e);
                }
            }
        }
    }

    private void addInput(Entry entry, Item input)
    {
        collectedInputs.put(entry, input);
        inputToEntry.put(input, entry);
    }

    public Map<Entry, Set<ItemVariant>> getCollectedInputs()
    {
        // Some horrible jank to inject ore dusts. They're lazily generated, so there's no definitive list of them.
        Map<Entry, Set<ItemVariant>> result = new HashMap<>();

        collectedInputs.asMap().forEach((e, c) ->
        {
            Set<ItemVariant> set = new HashSet<>();
            for (Item item : c)
            {
                if (item.builtInRegistryHolder().is(NMTags.BLOCK_CRUSHING_OUTPUTS))
                {
                    set.add(OreDustRegistry.CRUSHED_ORE.get(item).variant());
                    set.add(OreDustRegistry.ORE_DUST.get(item).variant());
                }

                set.add(ItemVariant.of(item));
            }
            result.put(e, set);
        });

        return result;
    }

    public record Entry(Component dirtyFatName, Component cleanFatName, ItemVariant result, CompoundTag nbt, float renderingYield,
                        float trommelYield, int tint)
    {
//        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
//                instance.group(
//                        Codecs.TEXT.fieldOf("dirty_fat_name").forGetter(Entry::dirtyFatName),
//                        Codecs.TEXT.fieldOf("clean_fat_name").forGetter(Entry::cleanFatName),
//                        MeatlibStorageUtil.ITEM_VARIANT_CODEC.fieldOf("result").forGetter(Entry::result),
//                        NbtCompound.CODEC.fieldOf("nbt").forGetter(Entry::nbt),
//                        Codec.FLOAT.fieldOf("rendering_yield").forGetter(Entry::renderingYield),
//                        Codec.FLOAT.fieldOf("trommel_yield").forGetter(Entry::trommelYield)
//                ).apply(instance, Entry::new));

        public static Entry read(FriendlyByteBuf buf)
        {
            return new Entry(
                    Component.Serializer.fromJson(buf.readUtf()),
                    Component.Serializer.fromJson(buf.readUtf()),
                    ItemVariant.fromPacket(buf),
                    buf.readNbt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt()
            );
        }

        public void write(FriendlyByteBuf buf)
        {
            buf.writeUtf(Component.Serializer.toJson(dirtyFatName));
            buf.writeUtf(Component.Serializer.toJson(cleanFatName));
            result.toPacket(buf);
            buf.writeNbt(nbt);
            buf.writeFloat(renderingYield);
            buf.writeFloat(trommelYield);
            buf.writeVarInt(tint);
        }

        public FluidVariant getDirty()
        {
            return FluidVariant.of(NMFluids.STILL_DIRTY_ORE_FAT, nbt);
        }

        public FluidVariant getClean()
        {
            return FluidVariant.of(NMFluids.STILL_CLEAN_ORE_FAT, nbt);
        }
    }
}
