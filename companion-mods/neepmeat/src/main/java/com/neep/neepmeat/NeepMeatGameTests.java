/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat;

import com.neep.neepspeak.parser.RuleParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class NeepMeatGameTests {
    @GameTest(template = "empty")
    public static void polishedMetalPlaces(GameTestHelper helper) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "polished_metal");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(id), "polished_metal is not registered");
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElseThrow();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block);
        helper.assertBlockPresent(block, pos);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void polishedMetalRecipeLoads(GameTestHelper helper) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "polished_metal");
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(id).isPresent(), "polished_metal recipe missing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void neepSpeakParserLoads(GameTestHelper helper) {
        var matches = RuleParser.parsePattern("1[abc]<wom>[cde]");
        helper.assertTrue(matches.size() == 3, "NEEPSpeak pattern parse");
        helper.succeed();
    }
}
