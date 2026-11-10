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

package com.neep.neepmeat.world;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMFeatures extends FabricDynamicRegistryProvider
{
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_ASBESTOS = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "ore_asbestos"));
    public static final ResourceKey<PlacedFeature> ORE_ASBESTOS_UPPER = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "ore_asbestos_upper"));
    public static final ResourceKey<PlacedFeature> ORE_ASBESTOS_LOWER = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "ore_asbestos_lower"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> BROME_PATCH = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "brome_patch"));
    public static final ResourceKey<PlacedFeature> SINGLE_PIECE_OF_BROME_BONEMEAL = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "single_piece_of_brome"));
    public static final ResourceKey<PlacedFeature> PLACED_BROME_PATCH = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "brome_patch"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_BROWNE = of("ore_browne");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_BROWNE_SMALL = of("ore_browne_small");
//    public static final RegistryKey<PlacedFeature> P_ORE_BROWNE_UPPER = pof("ore_browne_upper");
    public static final ResourceKey<PlacedFeature> P_ORE_BROWNE_MIDDLE = pof("ore_browne_middle");
    public static final ResourceKey<PlacedFeature> P_ORE_BROWNE_SMALL = pof("ore_browne_small");

    public NMFeatures(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    public static void init()
    {
        var asbestosSelector = BiomeSelectors.tag(NMTags.HAS_ORE_ASBESTOS);
        BiomeModifications.addFeature(asbestosSelector, GenerationStep.Decoration.UNDERGROUND_ORES, ORE_ASBESTOS_UPPER);
        BiomeModifications.addFeature(asbestosSelector, GenerationStep.Decoration.UNDERGROUND_ORES, ORE_ASBESTOS_LOWER);

        var browneSelector = BiomeSelectors.tag(NMTags.HAS_ORE_BROWNE);
        BiomeModifications.addFeature(browneSelector, GenerationStep.Decoration.UNDERGROUND_ORES, P_ORE_BROWNE_MIDDLE);
        BiomeModifications.addFeature(browneSelector, GenerationStep.Decoration.UNDERGROUND_ORES, P_ORE_BROWNE_SMALL);

        BiomeModifications.addFeature(BiomeSelectors.tag(NMTags.HAS_GRAVE_BROME), GenerationStep.Decoration.VEGETAL_DECORATION, PLACED_BROME_PATCH);
    }

    private static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier)
    {
        return List.of(countModifier, InSquarePlacement.spread(), heightModifier, BiomeFilter.biome());
    }

    private static List<PlacementModifier> modifiersWithCount(int count, PlacementModifier heightModifier)
    {
        return modifiers(CountPlacement.of(count), heightModifier);
    }

    private static List<PlacementModifier> modifiersWithRarity(int chance, PlacementModifier heightModifier)
    {
        return modifiers(RarityFilter.onAverageOnceEvery(chance), heightModifier);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries)
    {
//        final RegistryWrapper.Impl<Biome> biomeRegistry = registries.getWrapperOrThrow(RegistryKeys.BIOME);
//        entries.add(ExampleBiomes.MODDED_BIOME_KEY, biomeRegistry.getOrThrow(ExampleBiomes.MODDED_BIOME_KEY).value());

        var thing = new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(NMBlocks.BLOOD_BUBBLE_LOG),
//                new StraightTrunkPlacer(5, 2, 0),
                new BendingTrunkPlacer(5, 2, 0, 3, UniformInt.of(1, 2)),
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(NMBlocks.BLOOD_BUBBLE_LEAVES.defaultBlockState(), 3)
                        .add(NMBlocks.BLOOD_BUBBLE_LEAVES_FLOWERING.defaultBlockState(), 1)),
                new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50),
                new TwoLayersFeatureSize(1, 0, 1)).dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt().build();
        entries.add(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "blood_bubble_tree")), new ConfiguredFeature<>(Feature.TREE, thing));

        RuleTest ruleTest = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
        entries.add(ORE_ASBESTOS, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(ruleTest, NMBlocks.ASBESTOS.defaultBlockState(), 64)));
        placedFeature(ORE_ASBESTOS_UPPER, ORE_ASBESTOS, entries, modifiersWithRarity(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        placedFeature(ORE_ASBESTOS_LOWER, ORE_ASBESTOS, entries, modifiersWithCount(1, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));

        entries.add(BROME_PATCH, new ConfiguredFeature<>(Feature.FLOWER,
                createRandomPatchFeatureConfig(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(NMBlocks.GRAVE_BROME.defaultBlockState(), 2)
                                .add(NMBlocks.GRAVE_BROME.withAge(1, false), 1)),
                        2))
        );
//        placedFeature(SINGLE_PIECE_OF_BROME_BONEMEAL, BROME_PATCH, entries, List.of(PlacedFeatures.isAir()));
        placedFeature(
                PLACED_BROME_PATCH, BROME_PATCH, entries, List.of(
                RarityFilter.onAverageOnceEvery(4),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3))
                ));

        // Browne
        RuleTest stoneTest = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateTest = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        List<OreConfiguration.TargetBlockState> browneList = List.of(
                OreConfiguration.target(stoneTest, NMBlocks.BROWNE_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateTest, NMBlocks.DEEPSLATE_BROWNE_ORE.defaultBlockState())
        );

        entries.add(ORE_BROWNE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(browneList, 9)));
        entries.add(ORE_BROWNE_SMALL, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(browneList, 4)));

        // Copying iron generation for now
//        placedFeature(P_ORE_BROWNE_UPPER, ORE_BROWNE, entries, modifiersWithCount(90,
//                HeightRangePlacementModifier.trapezoid(YOffset.fixed(80), YOffset.fixed(384))));
        placedFeature(P_ORE_BROWNE_MIDDLE, ORE_BROWNE, entries, modifiersWithCount(10,
                HeightRangePlacement.triangle(VerticalAnchor.bottom(), VerticalAnchor.absolute(46))));
        placedFeature(P_ORE_BROWNE_SMALL, ORE_BROWNE_SMALL, entries, modifiersWithCount(10,
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));

    }

    private static ResourceKey<ConfiguredFeature<?, ?>> of(String path)
    {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, path));
    }

    private static ResourceKey<PlacedFeature> pof(String path)
    {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, path));
    }

    private static void placedFeature(ResourceKey<PlacedFeature> registryKey, ResourceKey<ConfiguredFeature<?, ?>> configured, Entries entries, List<PlacementModifier> modifiers)
    {
        entries.add(registryKey, new PlacedFeature(entries.ref(configured), modifiers));
    }

    private static RandomPatchConfiguration createRandomPatchFeatureConfig(BlockStateProvider block, int tries)
    {
        return FeatureUtils.simpleRandomPatchConfiguration(tries, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(block)));
    }

    @Override
    public String getName()
    {
        return NeepMeat.NAMESPACE + " features";
    }
}
