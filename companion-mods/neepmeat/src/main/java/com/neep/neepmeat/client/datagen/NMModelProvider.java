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

package com.neep.neepmeat.client.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class NMModelProvider extends FabricModelProvider
{
    public NMModelProvider(FabricDataOutput output)
    {
        super(output);
    }

    public String getName()
    {
        return super.getName() + "(neepmeat)";
    }

    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator)
    {
//        Identifier modelId = new Identifier(NeepMeat.NAMESPACE, "block/painted_corrugated_asbestos");
        for (var block : NMBlocks.PAINTED_CORRUGATED_ASBESTOS.entries)
        {
            blockStateModelGenerator.createAxisAlignedPillarBlock(block, TexturedModel.CUBE);
//            blockStateModelGenerator.blockStateCollector.accept(new SimpleBlockStateSupplier(block, modelId));
//            Identifier modelId = blockStateModelGenerator.
//            blockStateModelGenerator.blockStateCollector.accept(new SimpleBlockStateSupplier(block, ));
//            blockStateModelGenerator.item(block);
        }

        blockStateModelGenerator.createTrivialBlock(LivingMachines.SKIN_MACHINE_BLOCK,
                TextureMapping.cube(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/living_machine/skin_machine_block")),
                TexturedModel.CUBE.get(LivingMachines.SKIN_MACHINE_BLOCK).getTemplate());

//        BlockStateModelGenerator.createStairsBlockState(NMBlocks.GREEN_ROUGH_CONCRETE.stairs
//        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createStairsBlockState(

//        blockStateModelGenerator.registerSingleton(NMBlocks.ACTIVE_WASTE,
//                TextureMap.all(new Identifier(NeepMeat.NAMESPACE, "block/reactor/active_waste_1")),
//                TexturedModel.CUBE_ALL.get(NMBlocks.ACTIVE_WASTE).getModel());

        for (var entry : BigBlock2Structure.INSTANCES)
        {
            blockStateModelGenerator.createNonTemplateModelBlock(entry, Blocks.AIR);
        }
    }

    public void generateItemModels(ItemModelGenerators itemModelGenerator)
    {
    }

    public static class SimpleBlockStateSupplier implements BlockStateGenerator
    {
        private final Block block;
        private final ResourceLocation model;

        public SimpleBlockStateSupplier(Block block, ResourceLocation model)
        {
            this.block = block;
            this.model = model;
        }

        @Override
        public Block getBlock()
        {
            return block;
        }

        @Override
        public JsonElement get()
        {
            JsonObject root = new JsonObject();
            JsonObject variant = new JsonObject();
            JsonObject model = new JsonObject();

            model.addProperty("model", this.model.toString());
            variant.add("", model);
            root.add("variants", variant);
            return root;
        }

    }
}
