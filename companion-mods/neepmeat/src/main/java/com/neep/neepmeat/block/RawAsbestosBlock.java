package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseBuildingBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlocks;
import java.util.function.Consumer;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;

public class RawAsbestosBlock extends BaseBuildingBlock
{
    public RawAsbestosBlock(RegistrationContext ctx, boolean makeWall, MeatlibBlockSettings settings)
    {
        super(ctx, makeWall, settings);
    }

    @Override
    public void generateRecipes(RecipeOutput exporter)
    {
        super.generateRecipes(exporter);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.ASBESTOS_TILE, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, ((com.neep.meatlib.block.BaseBuildingBlock) NMBlocks.ASBESTOS_TILE).stairs, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, ((com.neep.meatlib.block.BaseBuildingBlock) NMBlocks.ASBESTOS_TILE).slab, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.ASBESTOS_TILE.wall, this);

        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, NMBlocks.CORRUGATED_ASBESTOS, this);
    }
}
