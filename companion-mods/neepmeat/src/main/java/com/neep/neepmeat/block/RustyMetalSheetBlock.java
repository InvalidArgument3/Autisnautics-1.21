package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseBuildingBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlocks;
import java.util.function.Consumer;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;

public class RustyMetalSheetBlock extends BaseBuildingBlock
{
    public RustyMetalSheetBlock(RegistrationContext ctx, boolean makeWall, MeatlibBlockSettings settings)
    {
        super(ctx, makeWall, settings);
    }

    @Override
    public void generateRecipes(RecipeOutput exporter)
    {
        super.generateRecipes(exporter);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.RUSTY_VENT, this);

        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.RUSTY_METAL_BLOCK, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, ((com.neep.meatlib.block.BaseBuildingBlock) NMBlocks.RUSTY_METAL_BLOCK).stairs, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, ((com.neep.meatlib.block.BaseBuildingBlock) NMBlocks.RUSTY_METAL_BLOCK).slab, this, 2);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.RUSTY_METAL_BLOCK.wall, this);
    }
}
