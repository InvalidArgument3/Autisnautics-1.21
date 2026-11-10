/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.MaterialItems;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class ManufactureGameTests {
    @GameTest(template = "empty")
    public static void boneBecomesContractileActuator(GameTestHelper helper) {
        ItemStack bone = new ItemStack(Items.BONE);
        ItemStack result = ManufactureRecipe.apply(bone,
                new ManufactureRecipe.ManufactureStepSpec("neepmeat:combine", "neepmeat:internal_components"),
                helper.getLevel().getRecipeManager());
        helper.assertTrue(result.is(MaterialItems.CONTRACTILE_ACTUATOR.get()), "result " + result);
        helper.assertTrue(helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath("neepmeat", "ingredients/control_unit")).isPresent(),
                "control unit recipe missing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chestSlotMutatesInPlace(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = helper.getBlockEntity(new BlockPos(1, 0, 1));
        chest.setItem(0, new ItemStack(Items.BONE));
        MutateInPlace<ItemStack> place = MutateInPlace.ITEM.find(helper.getLevel(), helper.absolutePos(new BlockPos(1, 0, 1)), null);
        helper.assertTrue(place != null, "no mutate target");
        ItemStack result = place.combine(new ItemStack(MaterialItems.INTERNAL_COMPONENTS.get()), helper.getLevel().getRecipeManager());
        helper.assertTrue(result.is(MaterialItems.CONTRACTILE_ACTUATOR.get()), "mutated " + result);
        helper.assertTrue(chest.getItem(0).is(MaterialItems.CONTRACTILE_ACTUATOR.get()), "chest slot");
        helper.succeed();
    }
}
