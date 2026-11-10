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

import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.network.PacketBufUtil;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.meatgun.ModuleLootCondition;
import com.neep.neepmeat.mixin.loot.*;
import com.neep.neepmeat.recipe.AdvancedBlockCrushingRecipe;
import com.neep.neepmeat.recipe.BlockCrushingRecipe;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.transfer.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class BlockCrushingRegistry
{
    private static final GlobalChannelManager<Consumer<SyncData>> SYNC = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block_crushing_sync"),
            ChannelFormat.builder(ParamCodecs.<Consumer<SyncData>>as(Consumer.class)).param(SyncData.PARAM_CODEC).build());

    public static final BlockCrushingRegistry INSTANCE = new BlockCrushingRegistry();

    private final Map<ItemVariant, Entry> basicInputToEntry = new HashMap<>();
    private final Map<ItemVariant, Entry> advancedInputToEntry = new HashMap<>();

    @Nullable private BlockCrushingRecipe blockCrushingRecipe;
    @Nullable private AdvancedBlockCrushingRecipe advancedBlockCrushingRecipe;

    private BlockCrushingRegistry() { }

    private void read(RecipeManager manager, SyncData buf)
    {
        this.blockCrushingRecipe = BlockCrushingRecipe.INSTANCE;
        this.advancedBlockCrushingRecipe = AdvancedBlockCrushingRecipe.INSTANCE;

        basicInputToEntry.clear();
        advancedInputToEntry.clear();
        basicInputToEntry.putAll(buf.basicInputToEntry);
        advancedInputToEntry.putAll(buf.advancedInputToEntry);
    }

    public static void init()
    {
        DataPackPostProcess.AFTER_DATA_PACK_LOAD.register(DataPackPostProcess.ORE_FAT, BlockCrushingRegistry.INSTANCE::reload);
        DataPackPostProcess.SYNC.register(BlockCrushingRegistry::sync);
    }

    private void reload(MinecraftServer server)
    {
//        RecipeManager recipeManager = server.getRecipeManager();
        LootDataManager lootManager = server.getLootData();
        this.blockCrushingRecipe = BlockCrushingRecipe.INSTANCE;
        this.advancedBlockCrushingRecipe = AdvancedBlockCrushingRecipe.INSTANCE;

        searchForLootTables(lootManager);
    }

    @Nullable
    public Entry getFromInputBasic(ItemVariant input)
    {
        return basicInputToEntry.get(input);
    }

    @Nullable
    public Entry getFromInputAdvanced(ItemVariant input)
    {
        return advancedInputToEntry.get(input);
    }

    private void searchForLootTables(LootDataManager lootManager)
    {
        NeepMeat.LOGGER.info("Searching for block crushing loot tables");

        TagKey<Block> inputs = NMTags.BLOCK_CRUSHING_INPUTS;

        BuiltInRegistries.ITEM.stream()
                .filter(i -> i instanceof BlockItem)
                .map(i -> (BlockItem) i)
                .filter(i -> i.getBlock().builtInRegistryHolder().is(inputs))
                .forEach(i ->
                        inspectLootTable(i, List.of(lootManager.getLootTable(i.getBlock().getLootTable()).pools)));
        ;
    }

    /// Changes the contents of an ore block's loot table.
    /// If the block is in `neepmeat:drops_with_drill`
    /// - All drops will be replaced with the original block.
    /// If @param includeAll and the block is in `neepmeat:block_crushing_inputs`
    /// - The above behaviour will happen.
    /// If an item in the drop list is in `neepmeat:block_crushing_outputs`
    /// - That stack will be replaced with the original block.
    /// - This will only happen for one stack. Further matching stacks will be ignored.
    public List<ItemStack> modifyDroppedStacks(BlockState state, List<ItemStack> stacks, boolean includeAll)
    {
        // Can't guarantee that the list will be mutable. It's an ObjectArrayList in 1.20, but this might change.
        List<ItemStack> newStacks = new ObjectArrayList<>();

        boolean oreDropped = false;
        if (state.is(NMTags.DROPS_WITH_DRILL))
        {
            newStacks.add(new ItemStack(state.getBlock().asItem()));
        }
        else
        {
            for (ItemStack stack : stacks)
            {
                // Replace raw ores with the original block
                if (stack.is(NMTags.BLOCK_CRUSHING_OUTPUTS) || (includeAll && state.is(NMTags.BLOCK_CRUSHING_INPUTS)))
                {
                    if (!oreDropped)
                    {
                        newStacks.add(new ItemStack(state.getBlock().asItem()));

                        // Prevent duplicate drops if the loot table includes multiple separate raw ore stacks
                        oreDropped = true;
                    }
                }
                else
                {
                    newStacks.add(stack);
                }
            }
        }

        return newStacks;
    }

    /// Replaces raw ores with a single ore block if the block is in neepmeat:block_crushing_inputs.
    public List<ItemStack> modifyDroppedStacks(BlockState state, LootParams.Builder builder, List<ItemStack> stacks)
    {
        if (state.is(NMTags.BLOCK_CRUSHING_INPUTS))
        {
            if (ModuleLootCondition.test(builder.getParameter(LootContextParams.TOOL), NMTags.ORE_BLOCK_DROPPING))
            {
                return modifyDroppedStacks(state, stacks, false);
            }
        }

        return stacks;
    }

    /**
     * Incredibly cursed traversal that looks for a structure that roughly resembles a vanilla ore.
     * Loot table injection ignores excluded outputs but not excluded inputs.
     * @return Loot table amendment (raw ore to replace, item to replace raw ore with). Null if unrecognised.
     */
    @Nullable
    public InspectedLootTable inspectLootTable(Item blockItem, List<LootPool> pools)
    {
        if (advancedBlockCrushingRecipe == null && blockCrushingRecipe == null)
            return null;

        TagKey<Item> outputs = NMTags.BLOCK_CRUSHING_OUTPUTS;
        TagKey<Item> exclude = NMTags.BLOCK_CRUSHING_OUTPUTS_EXCLUDE;

        for (LootPool pool : pools)
        {
            for (LootPoolEntryContainer entry : pool.entries)
            {
                if (entry instanceof AlternativesEntry alternativeEntry)
                {
                    for (LootPoolEntryContainer child : ((CombinedEntryAccessor) alternativeEntry).getChildren())
                    {
                        if (child instanceof LootItem itemEntry)
                        {
                            Item outputItem = ((ItemEntryAccessor) itemEntry).getItem();
                            boolean isRawOre = outputItem.builtInRegistryHolder().is(outputs);
                            if (isRawOre && !outputItem.builtInRegistryHolder().is(exclude))
                            {
                                int min = 1;
                                int max = 1;

                                LootItemFunction[] lootFunctions = ((LeafEntryAccessor) itemEntry).getFunctions();

                                for (var function : lootFunctions)
                                {
                                    if (function instanceof SetItemCountFunction setCount)
                                    {
                                        NumberProvider number = ((SetCountLootFunctionAccessor) setCount).getCountRange();
                                        if (number instanceof UniformGenerator uniform)
                                        {
                                            if (((UniformLootNumberProviderAccessor) uniform).getMin() instanceof ConstantValue constant)
                                            {
                                                // Trick the compiler as the class is final
                                                min = (int) ((ConstantLootNumberProviderAccessor) (Object) constant).getValue();
                                            }
                                            if (((UniformLootNumberProviderAccessor) uniform).getMax() instanceof ConstantValue constant)
                                            {
                                                // Trick the compiler as the class is final
                                                max = (int) ((ConstantLootNumberProviderAccessor) (Object) constant).getValue();
                                            }
                                        }
                                    }
                                }

                                if (max < min)
                                {
                                    max = min;
                                }

                                register(blockItem, outputItem, min, max);
                                return new InspectedLootTable(blockItem, outputItem);
                            }
                            else if (isRawOre)
                            {
                                // Do not register if excluded
                                return new InspectedLootTable(blockItem, outputItem);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void register(Item inputVariant, Item outputItem, int min, int max)
    {
        if (blockCrushingRecipe != null)
        {
            basicInputToEntry.put(ItemVariant.of(inputVariant), create(inputVariant, outputItem, blockCrushingRecipe, min, max));
        }

        if (advancedBlockCrushingRecipe != null)
        {
            advancedInputToEntry.put(ItemVariant.of(inputVariant), create(inputVariant, outputItem, advancedBlockCrushingRecipe, min, max));
        }

        // Convert crushed ore into dust
        // Crushed ores are identified by their raw ore
        var crushed = OreDustRegistry.CRUSHED_ORE.get(outputItem);
        var dust = OreDustRegistry.ORE_DUST.get(outputItem);

        BallMillRecipeGenerator.INSTANCE.add(
//                "ore_dust" + "_from_" + inputVariant.getRegistryEntry().registryKey().getValue().getPath(),
                outputItem.builtInRegistryHolder().key().location().toString().replace(':', '_') + "_ore_dust",
                RecipeInputs.of(crushed.variant(), 1),
                dust.variant());
    }

    private Entry create(Item inputItem, Item outputItem, BlockCrushingRecipe recipe, int min, int max)
    {
        // Increase the amounts if the recipe is set to provide higher yields than the loot table.
        // Relevant for advanced crushing.
        min = (int) Math.max(recipe.getBaseAmount(), min);
        max = (int) Math.max(recipe.getBaseAmount(), max);

        int extraAmount = (int) recipe.getExtraAmount();

        ItemVariant outputVariant = OreDustRegistry.CRUSHED_ORE.get(outputItem).variant();
        RegistryRecipeInput<Item> input = RecipeInputs.of(inputItem, 1);
        RecipeOutput<Item> output = new RecipeOutputImpl<>(outputVariant.getItem(), min, max, 1, Ingredients.ITEM, outputVariant.getNbt());
        RecipeOutput<Item> extra = new RecipeOutputImpl<>(outputVariant.getItem(), extraAmount, extraAmount, recipe.getChance(), Ingredients.ITEM, outputVariant.getNbt());

        return new Entry(input, output, extra);
    }

    public Collection<Entry> getBasicEntries()
    {
        return basicInputToEntry.values();
    }

    public Collection<Entry> getAdvancedEntries()
    {
        return advancedInputToEntry.values();
    }

    private static void sync(MinecraftServer server, @NotNull Set<ServerPlayer> players)
    {
        for (var player : players)
        {
            SYNC.emitter(player).accept(new SyncData(INSTANCE.basicInputToEntry, INSTANCE.advancedInputToEntry));
        }
    }

    public record InspectedLootTable(Item oreBlockItem, Item rawOre) { }

    public record Entry(RegistryRecipeInput<Item> input, RecipeOutput<Item> output, RecipeOutput<Item> extra)
    {
        public static Entry read(FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Item> input = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutput<Item> output = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);
            RecipeOutput<Item> extra = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            return new Entry(input, output, extra);
        }

        public void write(FriendlyByteBuf buf)
        {
            input.write(buf);
            output.write(buf);
            extra.write(buf);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {
            SYNC.receiverHandler(Dist.CLIENT, (player, buf, responseSender) ->
                    data -> INSTANCE.read(Minecraft.getInstance().level.getRecipeManager(), data));
        }
    }

    private record SyncData(Map<ItemVariant, Entry> basicInputToEntry, Map<ItemVariant, Entry> advancedInputToEntry)
    {
        public static final ParamCodec<SyncData> PARAM_CODEC = ParamCodec.of(SyncData.class, SyncData::write, SyncData::read);

        public static SyncData read(FriendlyByteBuf buf)
        {
            SyncData data = new SyncData(new HashMap<>(), new HashMap<>());
            PacketBufUtil.readMap(buf, data.basicInputToEntry::put, ItemVariant::fromPacket, Entry::read);
            PacketBufUtil.readMap(buf, data.advancedInputToEntry::put, ItemVariant::fromPacket, Entry::read);
            return data;
        }

        private void write(FriendlyByteBuf buf)
        {
            PacketBufUtil.writeMap(buf, basicInputToEntry, TransferVariant::toPacket, Entry::write);
            PacketBufUtil.writeMap(buf, advancedInputToEntry, TransferVariant::toPacket, Entry::write);
        }
    }
}
