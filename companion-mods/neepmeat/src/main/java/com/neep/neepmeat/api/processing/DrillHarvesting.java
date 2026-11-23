package com.neep.neepmeat.api.processing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.api.event.DataPackEvents;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMLootConditions;
import com.neep.neepmeat.meatgun.ModuleLootCondition;
import com.neep.neepmeat.mixin.loot.CombinedEntryAccessor;
import com.neep.neepmeat.mixin.loot.ItemEntryAccessor;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/// Manages loot table modification so that ore blocks can be dropped when mined with a rotary drill.
public class DrillHarvesting
{
    private static String FIELD_NAME;
//    private static String FIELD_NAME_SRG = "f_79109_";

    public static void init()
    {
        // LootTable: class_52
        // LootPool: class_55
        FIELD_NAME = "pools";

        DataPackEvents.IMMEDIATELY_AFTER_RELOAD.register((server, registryManager) ->
        {
            var lookup = registryManager.asGetterLookup().lookupOrThrow(Registries.BLOCK);
            var loot = server.getLootData();

            lookup.get(NMTags.DROPS_WITH_DRILL).ifPresent(o -> o.forEach(e ->
            {
                enableAbsoluteDrillHarvesting(e.value(), loot.getLootTable(e.value().getLootTable()));
            }));

            lookup.get(NMTags.BLOCK_CRUSHING_INPUTS).ifPresent(o -> o.forEach(e ->
            {
                // Prevent issues
                if (e.is(NMTags.DROPS_WITH_DRILL))
                    return;

                enableRawOreDrillHarvesting(e.value(), loot.getLootTable(e.value().getLootTable()));
            }));
        });
    }

    private static void enableAbsoluteDrillHarvesting(Block block, LootTable lootTable)
    {
        // Since we can't test for a raw ore here, we can't just swap the item out in the loot function.
        Item item = block.asItem();
        appendPool(
                lootTable, LootPool.lootPool()
                        .conditionally(new ModuleLootCondition(NMTags.ORE_BLOCK_DROPPING))
                        .add(LootItem.lootTableItem(item))
                        .build());

        // Add a loot function to erase all potential extra drops to prevent duping
        appendFunction(lootTable, new DrillHarvestingLootFunction(item, item));

        printLootPools(lootTable, block);
    }

    private static void printLootPools(LootTable lootTable, Block block)
    {
//        System.out.println();
//        System.out.println(" --- PROCESSED LOOT TABLE FOR %s".formatted(block));
//        System.out.println("Pools:");
//        for (LootPool lootPool : getPools(lootTable).first())
//        {
//            System.out.println(" conditions: " + List.of(lootPool.conditions));
//            System.out.println(" entries: " + List.of(lootPool.entries));
//        }
//
//        System.out.println("Functions");
//        System.out.println(Arrays.toString(lootTable.functions));
//        System.out.println();
    }

    private static void enableRawOreDrillHarvesting(Block block, LootTable lootTable)
    {
        // Traverse the loot table to find a raw ore
        for (var pool : getPools(lootTable).first())
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

                            // BLOCK_CRUSHING_OUTPUTS should cover all raw ores
                            if (outputItem.builtInRegistryHolder().is(NMTags.BLOCK_CRUSHING_OUTPUTS))
                            {
                                appendFunction(lootTable, new DrillHarvestingLootFunction(outputItem, block.asItem()));
                                printLootPools(lootTable, block);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private static <T> T[] insert(T[] array, T t)
    {
        var newOnes = (T[]) Array.newInstance(array[0].getClass(), array.length + 1);
        newOnes[0] = t;
        System.arraycopy(array, 0, newOnes, 1, newOnes.length - 1);

        return newOnes;
    }

    private static <T> T[] append(T[] array, T t)
    {
        var newOnes = Arrays.copyOf(array, array.length + 1);
        newOnes[newOnes.length - 1] = t;

        return newOnes;
    }

    private static void appendPool(LootTable lootTable, LootPool pool)
    {
        var pools = getPools(lootTable);
        pools.second().accept(pool);

//        try
//        {
//            MethodHandles.publicLookup().findSetter(LootTable.class, FIELD_NAME, LootPool[].class).invokeExact(lootTable, newPools);
//        }
//        catch (Throwable ignored)
//        {
//            try
//            {
//                MethodHandles.publicLookup().findSetter(LootTable.class, FIELD_NAME, List.class).invoke(lootTable, Lists.newArrayList(newPools));
//            }
//            catch (Throwable e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
    }

    private static void appendFunction(LootTable lootTable, LootItemFunction function)
    {
        lootTable.functions = append(lootTable.functions, function);
        lootTable.compositeFunction = LootItemFunctions.compose(lootTable.functions);
    }

    private static Pair<Iterable<LootPool>, Consumer<LootPool>> getPools(LootTable lootTable)
    {
//        System.out.println(" current namespace: " + FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
//        System.out.println(" field name: " + fieldName);

        List<LootPool> pools;
        Consumer<LootPool> append;
        try
        {
            var field = lootTable.getClass().getField(FIELD_NAME);
            if (field.getType().equals(List.class))
            {
                pools = ((List<LootPool>) field.get(lootTable));
                append = pool ->
                {
                    pools.add(pool);
                };
            }
            else if (field.getType().equals(LootPool[].class))
            {
                pools = List.of((LootPool[]) field.get(lootTable));
                append = pool -> {
                    try
                    {
                        LootPool[] array = (LootPool[]) field.get(lootTable);
                        field.set(lootTable, append(array, pool));
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                };
            }
            else
                throw new NoSuchElementException("field 'pools' in class %s is not an array or a list".formatted(lootTable.getClass()));
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

        return Pair.of(pools, append);

//        try
//        {
//            pools = new ObjectArrayList<>((LootPool[]) MethodHandles.publicLookup().findGetter(LootTable.class, FIELD_NAME, LootPool[].class).invokeExact(lootTable));
//        }
//        catch (Throwable ignored)
//        {
//            try
//            {
//                pools = new ObjectArrayList<>((List<LootPool>) MethodHandles.publicLookup().findGetter(LootTable.class, FIELD_NAME, List.class).invokeExact(lootTable));
//            }
//            catch (Throwable e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
//        return pools;
    }

    public record DrillHarvestingLootFunction(Item toRemove, Item swapWith) implements LootItemFunction
    {
        public static final Codec<DrillHarvestingLootFunction> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("target").forGetter(DrillHarvestingLootFunction::toRemove),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("swap_with").forGetter(DrillHarvestingLootFunction::swapWith)
                ).apply(instance, DrillHarvestingLootFunction::new));

        @Override
        public LootItemFunctionType getType()
        {
            return NMLootConditions.DRILL_HARVEST;
        }

        @Override
        public ItemStack apply(ItemStack stack, LootContext lootContext)
        {
//            System.out.println(" -- tool: " + lootContext.get(LootContextParameters.TOOL));
            if (ModuleLootCondition.test(lootContext.getParamOrNull(LootContextParams.TOOL), NMTags.ORE_BLOCK_DROPPING))
            {
//                System.out.println(" -- MODULE LOOT CONDITION TRUE");
                if (stack.is(toRemove))
                {
                    return new ItemStack(swapWith, 1);
                }

                // Prevent possible duping of extra drops
                return ItemStack.EMPTY;
            }
            else
            {
//                System.out.println(" -- MODULE LOOT CONDITION FALSE");
            }

            return stack;
        }
    }
}
