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

import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.RecipeRegistry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipe;
import com.neep.neepmeat.api.processing.ball_mill.DistinctBallMillRecipe;
import com.neep.neepmeat.machine.mixer.MixingRecipe;
import com.neep.neepmeat.recipe.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class NMrecipeTypes
{
    public static void init()
    {

    }

    public static final MeatRecipeSerialiser<MixingRecipe> MIXING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "mixing", new MixingRecipe.MixerSerializer());
    public static final MeatRecipeType<MixingRecipe> MIXING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "mixing");

    public static final MeatRecipeSerialiser<CrushingRecipe> GRINDING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "grinding", new CrushingRecipe.Serializer<>(CrushingRecipe::new, CrushingRecipe.DestroyRecipe::new, 60));
    public static final MeatRecipeType<CrushingRecipe> GRINDING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "grinding");

    public static final MeatRecipeSerialiser<AdvancedCrushingRecipe> ADVANCED_CRUSHING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "advanced_crushing", new AdvancedCrushingRecipe.Serializer<>(AdvancedCrushingRecipe::new, AdvancedCrushingRecipe.DestroyRecipe::new, 60));
    public static final MeatRecipeType<AdvancedCrushingRecipe> ADVANCED_CRUSHING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "advanced_crushing");

    public static final MeatRecipeSerialiser<BlockCrushingRecipe> BLOCK_CRUSHING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "block_crushing", new BlockCrushingRecipe.Serialiser<>(BlockCrushingRecipe::new));
    public static final MeatRecipeType<BlockCrushingRecipe> BLOCK_CRUSHING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "block_crushing");
    public static final MeatRecipeSerialiser<AdvancedBlockCrushingRecipe> ADVANCED_BLOCK_CRUSHING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "advanced_block_crushing", new BlockCrushingRecipe.Serialiser<>(AdvancedBlockCrushingRecipe::new));
    public static final MeatRecipeType<AdvancedBlockCrushingRecipe> ADVANCED_BLOCK_CRUSHING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "advanced_block_crushing");

    public static final MeatRecipeSerialiser<AlloyKilnRecipe> ALLOY_KILN_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "alloy_smelting", new AlloyKilnRecipe.Serializer(AlloyKilnRecipe::new, 60));
    public static final MeatRecipeType<AlloyKilnRecipe> ALLOY_SMELTING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "alloy_smelting");

    public static final MeatRecipeSerialiser<RenderingRecipe> RENDERING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "rendering", new RenderingRecipe.Serializer(RenderingRecipe::new));
    public static final MeatRecipeType<RenderingRecipe> RENDERING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "rendering");

    public static final MeatRecipeSerialiser<OreFatRenderingRecipe> ORE_FAT_RENDERING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "ore_fat_rendering", new OreFatRenderingRecipe.Serializer(OreFatRenderingRecipe::new));
    public static final MeatRecipeType<OreFatRenderingRecipe> ORE_FAT_RENDERING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "ore_fat_rendering");

    public static final MeatRecipeSerialiser<FatPressingRecipe> FAT_PRESSING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "fat_pressing", new FatPressingRecipe.Serializer());
    public static final MeatRecipeType<FatPressingRecipe> FAT_PRESSING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "fat_pressing");

    public static final MeatRecipeSerialiser<PressingRecipe> PRESSING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "pressing", new PressingRecipe.Serializer());
    public static final MeatRecipeType<PressingRecipe> PRESSING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "pressing");

    public static final MeatRecipeSerialiser<MobSqueezingRecipe> MOB_SQUEEZING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "mob_squeezing", new MobSqueezingRecipe.Serializer());
    public static final MeatRecipeType<MobSqueezingRecipe> MOB_SQUEEZING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "mob_squeezing");

    public static final MeatRecipeSerialiser<EnlighteningRecipe> ENLIGHTENING_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "enlightening", new EnlighteningRecipe.Serializer());
    public static final MeatRecipeType<EnlighteningRecipe> ENLIGHTENING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "enlightening");

    public static final MeatRecipeSerialiser<NormalTrommelRecipe> TROMMEL_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "trommel", new NormalTrommelRecipe.Serializer(NormalTrommelRecipe::new));
    public static final MeatRecipeType<NormalTrommelRecipe> TROMMEL = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "trommel");

    public static final MeatRecipeSerialiser<FatTrommelRecipe> FAT_TROMMEL_SERIALIZER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "fat_trommel", new FatTrommelRecipe.Serializer());
    public static final MeatRecipeType<FatTrommelRecipe> FAT_TROMMEL = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "fat_trommel");

    public static final MeatRecipeSerialiser<FluidHeatingRecipe> HEATING_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "heating", new FluidHeatingRecipe.Serializer(60));
    public static final MeatRecipeType<FluidHeatingRecipe> HEATING = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "heating");

    public static final MeatRecipeSerialiser<VivisectionRecipe> VIVISECTION_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "vivisection", new VivisectionRecipe.Serializer());
    public static final MeatRecipeType<VivisectionRecipe> VIVISECTION = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "vivisection");

    public static final MeatRecipeSerialiser<BioreactorRecipe> BIOREACTOR_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "bioreactor", new BioreactorRecipe.Serialiser());
    public static final MeatRecipeType<BioreactorRecipe> BIOREACTOR = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "bioreactor");

    public static final RecipeSerializer<OreDustSmeltingRecipe> ORE_DUST_SMELTING_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "ore_dust_smelting", new OreDustSmeltingRecipe.Serialisier<>(OreDustSmeltingRecipe::new, 200));
    public static final RecipeType<OreDustSmeltingRecipe> ORE_DUST_SMELTING = RecipeRegistry.registerType(NeepMeat.NAMESPACE, "ore_dust_smelting");
    public static final RecipeSerializer<OreDustBlastingRecipe> ORE_DUST_BLASTING_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "ore_dust_blasting", new OreDustBlastingRecipe.Serialisier<>(OreDustBlastingRecipe::new, 100));
    public static final RecipeType<OreDustBlastingRecipe> ORE_DUST_BLASTING = RecipeRegistry.registerType(NeepMeat.NAMESPACE, "ore_dust_blasting");

    public static final MeatRecipeType<BallMillRecipe> BALL_MILL = RecipeRegistry.registerMeatlibType(NeepMeat.NAMESPACE, "ball_mill");
    public static final MeatRecipeSerialiser<DistinctBallMillRecipe> BALL_MILL_SERIALISER = RecipeRegistry.registerMeatlibSerializer(NeepMeat.NAMESPACE, "ball_mill", new DistinctBallMillRecipe.Serialiser());

    public static Component ofMinMax(long min, long max)
    {
        if (min != max)
            return Component.literal("Amount: " + min + "-" + max).withStyle(ChatFormatting.GOLD);
        else
            return Component.literal("Amount: " + min).withStyle(ChatFormatting.GOLD);
    }
}