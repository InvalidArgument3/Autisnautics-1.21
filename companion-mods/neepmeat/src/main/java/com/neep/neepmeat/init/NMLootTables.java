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

import com.neep.meatlib.block.BaseCropBlock;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class NMLootTables
{
    private static final ResourceLocation ZOMBIE = EntityType.ZOMBIE.getDefaultLootTable();
    private static final ResourceLocation HUSK = EntityType.HUSK.getDefaultLootTable();
    private static final ResourceLocation DROWNED = EntityType.DROWNED.getDefaultLootTable();
    private static final ResourceLocation ELDER_GUARDIAN = EntityType.ELDER_GUARDIAN.getDefaultLootTable();
    private static final ResourceLocation DUNGEON = BuiltInLootTables.SIMPLE_DUNGEON;
    private static final ResourceLocation DESERT_TEMPLE = BuiltInLootTables.DESERT_PYRAMID;
    private static final ResourceLocation JUNGLE_TEMPLE = BuiltInLootTables.JUNGLE_TEMPLE;
    private static final ResourceLocation ARMORER_CHEST = BuiltInLootTables.VILLAGE_ARMORER;
    private static final ResourceLocation BUTCHER_CHEST = BuiltInLootTables.VILLAGE_BUTCHER;
    private static final ResourceLocation GRASS = Blocks.GRASS.getLootTable();
    private static final ResourceLocation TALL_GRASS = Blocks.TALL_GRASS.getLootTable();
    private static final ResourceLocation POTATOES = Blocks.POTATOES.getLootTable();

    public static void init()
    {

    }

    private static void appendBrain(LootTable.Builder tableBuilder)
    {
        LootPool.Builder poolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(NMItems.ROUGH_BRAIN)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0f))))
                        .apply(LootingEnchantFunction.lootingMultiplier(ConstantValue.exactly(0.0f)))
                ).when(LootItemRandomChanceCondition.randomChance(0.25f));
//                        .conditionally(KilledByPlayerLootCondition.builder());

        tableBuilder.withPool(poolBuilder);
    }

    static
    {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
        {
            if (ZOMBIE.equals(id) || HUSK.equals(id) || DROWNED.equals(id))
            {
                appendBrain(tableBuilder);
            }
            else if (ELDER_GUARDIAN.equals(id))
            {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(((LootPoolSingletonContainer.Builder<?>)LootItem.lootTableItem(NMBlocks.STATUE.asItem())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0f, 1.0f))))
                                .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0f, 1.0f))))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer());

                tableBuilder.withPool(poolBuilder);
            }
            else if (DUNGEON.equals(id) || DESERT_TEMPLE.equals(id) || ARMORER_CHEST.equals(id) || BUTCHER_CHEST.equals(id) || JUNGLE_TEMPLE.equals(id))
            {
                if (NeepMeat.CONFIG.lootTables.injectIntegratorEggs.getRealValue())
                {
                    LootPool.Builder builder = LootPool.lootPool()
                            .setRolls(UniformGenerator.between(0.0f, 1.0f))
                            .add(LootItem.lootTableItem(NMBlocks.INTEGRATOR_EGG.asItem()).setWeight(10));
                    tableBuilder.withPool(builder);
                }
            }
            else if (POTATOES.equals(id))
            {
                LootPool.Builder builder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.04f))
                        .add(LootItem.lootTableItem(((BaseCropBlock) NMBlocks.FLESH_POTATO).getBaseSeedId()).setWeight(1));

                tableBuilder.withPool(builder);
            }
        });
    }
}
