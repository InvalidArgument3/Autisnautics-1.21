ServerEvents.generateData("after_mods", event => {
    // Terralith 1.21 removed #terralith:mystical — use explicit biomes for lapis/sal ammoniac
    let mysticalTerralithBiomes = [
        "terralith:amethyst_canyon",
        "terralith:amethyst_rainforest",
        "terralith:lavender_forest",
        "terralith:lavender_valley",
        "terralith:mirage_isles",
        "terralith:moonlight_grove",
        "terralith:moonlight_valley",
        "terralith:orchid_swamp"
    ]

    // removeFeature(event, "minecraft:ore_redstone")
    // removeFeature(event, "minecraft:ore_redstone_lower")

    // removeFeature(event, "occultism:ore_silver")
    // removeFeature(event, "occultism:ore_silver_deepslate")
    // removeFeature(event, "occultism:ore_silver_deepslate")
    /* A2: comment out CABIN oregen for now
    // It's possible to disable these in the config, but using configs for oregen is outdated and the way Thermal addons' oregens work are very strange
    removeFeature(event, "thermal:tin_ore")
    removeFeature(event, "thermal:silver_ore")

    addOregenOverworld(event, "kubejs:ore_ruby", "thermal:ruby_ore", "minecraft:trapezoid", -144, 16, 4, 6, 0.5, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_sapphire", "thermal:sapphire_ore", "minecraft:trapezoid", -144, 16, 4, 6, 0.5, "#minecraft:is_overworld")

    // The defaults for these 2 aren't what we want.
    // We need Extra Cinnabar to turn into Redstone and Apatite doesn't even generate without Thermal Cultivation.
    removeFeature(event, "thermal:apatite_ore")
    removeFeature(event, "thermal:cinnabar_ore")

    addOregenOverworld(event, "kubejs:ore_apatite", "thermal:apatite_ore", "minecraft:trapezoid", -16, 96, 3, 9, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_cinnabar", "thermal:cinnabar_ore", "minecraft:trapezoid", -16, 48, 3, 6, 0, "#minecraft:is_overworld")
    */

    // //A2: remove all ore features
    let oreFeaturesArray = [
        "create:zinc_ore",
        // "embers:ore_lead", "embers:ore_silver",
        "galosphere:ore_silver_large", "galosphere:ore_silver_small",
        "immersiveengineering:bauxite", "immersiveengineering:nickel", "immersiveengineering:deep_nickel", "immersiveengineering:lead", "immersiveengineering:silver", "immersiveengineering:uranium",
        "minecraft:ore_coal", "minecraft:ore_coal_buried", "minecraft:ore_copper_large", "minecraft:ore_copper_small", "minecraft:ore_diamond_buried", "minecraft:ore_diamond_large", "minecraft:ore_diamond_medium", "minecraft:ore_diamond_small", "minecraft:ore_emerald", "minecraft:ore_gold", "minecraft:ore_gold_buried", "minecraft:ore_iron", "minecraft:ore_iron_small", "minecraft:ore_lapis", "minecraft:ore_lapis_buried", "minecraft:ore_redstone",
        // "mna:overworld_vinteum_ore",
        // nuclearcraft neohaul (configured_feature ids)
        "nuclearcraftneohaul:boron_small", "nuclearcraftneohaul:boron_large",
        "nuclearcraftneohaul:lead_small", "nuclearcraftneohaul:lead_large",
        "nuclearcraftneohaul:lithium_small", "nuclearcraftneohaul:lithium_large",
        "nuclearcraftneohaul:magnesium_small", "nuclearcraftneohaul:magnesium_large",
        "nuclearcraftneohaul:thorium_small", "nuclearcraftneohaul:thorium_large",
        "nuclearcraftneohaul:tin_small", "nuclearcraftneohaul:tin_large",
        "nuclearcraftneohaul:uranium_small", "nuclearcraftneohaul:uranium_large",
        // "projectred_exploration:electrotine_ore", "projectred_exploration:peridot_ore", "projectred_exploration:ruby_ore", "projectred_exploration:sapphire_ore", "projectred_exploration:silver_ore", "projectred_exploration:tin_ore",
        "regions_unexplored:ore_redstone_large",
        // "scguns:anthralite_ore", "scguns:sulfur_ore", "scguns:phosphorite",
        // "thermal:apatite_ore", "thermal:cinnabar_ore", "thermal:lead_ore", "thermal:nickel_ore", "thermal:niter_ore", "thermal:silver_ore", "thermal:sulfur_ore", "thermal:tin_ore",
        "theurgy:sal_ammoniac_ore",
        // "vs_clockwork:ore_wanderlite",
        // duplicate meme stone features
        "chisel:ore_diabase", "chisel:ore_limestone", "chisel:ore_marble",
        // 1.21
        // "oritech:ore_nickel", "oritech:ore_platinum", "oritech:uranium_patch", "oritech:resource_node_common", "oritech:resource_node_rare", "oritech:resource_node_other",
        "stellaris:steel_ore_key", "stellaris:steel_ore_deepslate_key"
    ]

    oreFeaturesArray.forEach(oreFeature => {
        removeFeature(event, oreFeature)
    })
    // //Custom Ore Features — shattered floating crust
    // addOreGenOverworld(event, featureName, blockName, heightType, heightMin, heightMax, veinCount, veinSize, discardChanceOnAirExposure, biomeTag)
    // Basics (coal/copper/iron/zinc/diamond): #minecraft:is_overworld — every island, altitude = former crust depth
    // Specialty/mod ores: #c:is_{hot,temperate,cold,dry,wet}/overworld — fly to climate bands (Natural Temperature)

    // aluminum — tropical bauxite, lower crust under hot islands
    addOregenOverworld(event, "kubejs:ore_aluminum_underground", "immersiveengineering:ore_aluminum", "minecraft:trapezoid", -74, -6, 12, 8, 0, "#c:is_hot/overworld")
    addOregenOverworld(event, "kubejs:ore_aluminum_underground_big", "immersiveengineering:ore_aluminum", "minecraft:trapezoid", -74, -6, 0.02, 64, 0, "#c:is_hot/overworld")
    // //tfmg:bauxite disabled
    // anthralite
    // apatite
    // cinnabar
    // addOregenOverworld(event, "kubejs:ore_cinnabar_underground", "thermal:cinnabar_ore", "minecraft:uniform", -64, 15, 2, 8, 0, "#minecraft:is_overworld")
    // addOregenOverworld(event, "kubejs:ore_cinnabar_underground_deep", "thermal:cinnabar_ore", "minecraft:trapezoid", -96, 32, 6, 8, 0, "#minecraft:is_overworld")
    // coal — universal high crust; swamp lignite & deep-ocean seabed are regional bonuses
    addOregenOverworld(event, "kubejs:ore_coal_islands", "minecraft:coal_ore", "minecraft:uniform", 92, 400, 25, 18, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_coal_islands_big", "minecraft:coal_ore", "minecraft:uniform", 92, 400, 0.075, 64, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_coal_seabed_postdiluvian", "minecraft:coal_ore", "minecraft:trapezoid", 32, 70, 15, 12, 0.5, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_coal_seabed_postdiluvian_deep", "minecraft:coal_ore", "minecraft:trapezoid", 22, 62, 15, 12, 0.5, "#minecraft:is_deep_ocean")
    addOregenOverworld(event, "kubejs:ore_coal_anthracite", "minecraft:coal_ore", "minecraft:trapezoid", -20, 25, 0.05, 64, 0.8, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_coal_islands_swamp_lignite", "minecraft:coal_ore", "minecraft:uniform", 92, 400, 5, 36, 0.5, "#c:is_swamp")
    addOregenOverworld(event, "kubejs:ore_coal_islands_swamp_lignite_big", "minecraft:coal_ore", "minecraft:uniform", 92, 400, 0.5, 52, 0.5, "#c:is_swamp")
    // //tfmg:lignite disabled
    // copper — universal island foundation (lowest floating crust layer)
    addOregenOverworld(event, "kubejs:ore_copper_islands_bottom", "minecraft:copper_ore", "minecraft:trapezoid", 80, 144, 16, 10, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_copper_islands_bottom_big", "minecraft:copper_ore", "minecraft:trapezoid", 80, 144, 0.005, 64, 0, "#minecraft:is_overworld")
    // diamond — universal deep shattered mantle
    addOregenOverworld(event, "kubejs:ore_diamond_underground_small", "minecraft:diamond_ore", "minecraft:trapezoid", -144, 16, 7, 4, 0.5, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_diamond_underground_buried", "minecraft:diamond_ore", "minecraft:trapezoid", -144, 16, 4, 8, 1, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_diamond_underground_big", "minecraft:diamond_ore", "minecraft:trapezoid", -144, 16, 0.11, 12, 0.7, "#minecraft:is_overworld")
    // electrotine
    // addOregenOverworld(event, "kubejs:ore_electrotine_underground_magnetic", "projectred_exploration:electrotine_ore", "minecraft:trapezoid", -168, 40, 40, 32, 0, "alexscaves:magnetic_caves")
    // addOregenOverworld(event, "kubejs:ore_electrotine_islands_old", "projectred_exploration:electrotine_ore", "minecraft:uniform", 50, 400, 15, 18, 0, "regions_unexplored:alpha_grove")
    // emerald — upper crust of cold highlands
    addOregenOverworld(event, "kubejs:ore_emerald_islands_cold_high", "minecraft:emerald_ore", "minecraft:trapezoid", 272, 500, 10, 4, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_emerald_islands_peaks", "minecraft:emerald_ore", "minecraft:trapezoid", 100, 620, 30, 4, 0, "terralith:emerald_peaks")
    // gold — island-surface placers by climate (fly south for hot/dry, north for cold)
    addOregenOverworld(event, "kubejs:ore_gold_islands_temperate", "minecraft:gold_ore", "minecraft:uniform", 92, 400, 40, 3, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_gold_islands_hot", "minecraft:gold_ore", "minecraft:uniform", 92, 400, 80, 3, 0, "#c:is_hot/overworld")
    addOregenOverworld(event, "kubejs:ore_gold_islands_dry", "minecraft:gold_ore", "minecraft:uniform", 92, 400, 25, 10, 0, "#c:is_dry/overworld")
    addOregenOverworld(event, "kubejs:ore_gold_islands_dry_big", "minecraft:gold_ore", "minecraft:uniform", 92, 400, 0.005, 64, 0, "#c:is_dry/overworld")
    addOregenOverworld(event, "kubejs:ore_gold_islands_cold", "minecraft:gold_ore", "minecraft:uniform", 92, 400, 6, 3, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_gold_underground_alluvial_delta", "minecraft:gold_ore", "minecraft:uniform", -64, 40, 25, 3, 0, "regions_unexplored:ancient_delta")
    // iron — universal mid/high crust
    addOregenOverworld(event, "kubejs:ore_iron_islands", "minecraft:iron_ore", "minecraft:uniform", 92, 400, 16, 9, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_iron_islands_big", "minecraft:iron_ore", "minecraft:uniform", 92, 400, 0.002, 64, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_iron_underground", "minecraft:iron_ore", "minecraft:uniform", -24, 56, 20, 9, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_iron_underground_big", "minecraft:iron_ore", "minecraft:uniform", -48, 56, 0.03, 64, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_iron_underground_magnetite", "minecraft:iron_ore", "minecraft:uniform", -64, 56, 10, 9, 0, "alexscaves:magnetic_caves")
    // lapis — landmark biomes (mystical / mauve hills), worth an airship detour
    addOregenOverworld(event, "kubejs:ore_lapis_islands_mystical", "minecraft:lapis_ore", "minecraft:uniform", 92, 400, 6, 10, 0, mysticalTerralithBiomes)
    addOregenOverworld(event, "kubejs:ore_lapis_islands_mystical_big", "minecraft:lapis_ore", "minecraft:uniform", 92, 400, 0.05, 64, 0, mysticalTerralithBiomes)
    addOregenOverworld(event, "kubejs:ore_lapis_islands_mauve", "minecraft:lapis_ore", "minecraft:uniform", 92, 400, 6, 10, 0, "regions_unexplored:mauve_hills")
    addOregenOverworld(event, "kubejs:ore_lapis_islands_mauve_big", "minecraft:lapis_ore", "minecraft:uniform", 92, 400, 0.05, 64, 0, "regions_unexplored:mauve_hills")
    // lead — galena in temperate mid-crust; deep lenses under cold islands
    addOregenOverworld(event, "kubejs:ore_lead_underground", "immersiveengineering:ore_lead", "minecraft:trapezoid", -60, 40, 6, 8, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_lead_underground_big", "immersiveengineering:ore_lead", "minecraft:trapezoid", -20, 0, 0.01, 64, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_lead_underground_bottom", "immersiveengineering:ore_lead", "minecraft:trapezoid", -104, -24, 6, 10, 0, "#c:is_cold/overworld")
    // //tfmg:galena disabled, alexscaves:galena exists
    // nuclearcraft neohaul — climate-gated fission materials (blocks: nuclearcraftneohaul:*_ore)
    // boron — temperate reactor moderator, mid crust
    addOregenOverworld(event, "kubejs:ore_boron_underground", "nuclearcraftneohaul:boron_ore", "minecraft:trapezoid", -42, 28, 6, 4, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_boron_underground_big", "nuclearcraftneohaul:boron_ore", "minecraft:trapezoid", -42, 28, 0.03, 8, 0, "#c:is_temperate/overworld")
    // lithium — hot/wet brines (unify prefers nuclearcraft)
    addOregenOverworld(event, "kubejs:ore_lithium_islands_wet", "nuclearcraftneohaul:lithium_ore", "minecraft:uniform", 92, 400, 4, 6, 0, "#c:is_wet/overworld")
    addOregenOverworld(event, "kubejs:ore_lithium_underground_hot", "nuclearcraftneohaul:lithium_ore", "minecraft:trapezoid", -52, 28, 5, 4, 0, "#c:is_hot/overworld")
    // magnesium — evaporite in dry climates
    addOregenOverworld(event, "kubejs:ore_magnesium_underground_dry", "nuclearcraftneohaul:magnesium_ore", "minecraft:trapezoid", -64, 24, 4, 4, 0, "#c:is_dry/overworld")
    addOregenOverworld(event, "kubejs:ore_magnesium_underground_dry_big", "nuclearcraftneohaul:magnesium_ore", "minecraft:trapezoid", -64, 24, 0.02, 10, 0, "#c:is_dry/overworld")
    // tin — temperate cassiterite in mid/high crust
    addOregenOverworld(event, "kubejs:ore_tin_underground", "nuclearcraftneohaul:tin_ore", "minecraft:trapezoid", -16, 50, 5, 4, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_tin_islands", "nuclearcraftneohaul:tin_ore", "minecraft:trapezoid", 224, 352, 2, 6, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_tin_islands_big", "nuclearcraftneohaul:tin_ore", "minecraft:trapezoid", 224, 352, 0.01, 12, 0, "#c:is_temperate/overworld")
    // thorium — cold deep crust
    addOregenOverworld(event, "kubejs:ore_thorium_underground_cold", "nuclearcraftneohaul:thorium_ore", "minecraft:trapezoid", -80, 16, 3, 4, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_thorium_underground_cold_big", "nuclearcraftneohaul:thorium_ore", "minecraft:trapezoid", -80, 16, 0.015, 8, 0, "#c:is_cold/overworld")
    // uranium — cold north + toxic caves (unify prefers nuclearcraft)
    addOregenOverworld(event, "kubejs:ore_uranium_underground_cold", "nuclearcraftneohaul:uranium_ore", "minecraft:trapezoid", -64, 32, 5, 3, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_uranium_underground_cold_big", "nuclearcraftneohaul:uranium_ore", "minecraft:trapezoid", -64, 32, 0.02, 8, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_uranium_underground_toxic", "nuclearcraftneohaul:uranium_ore", "minecraft:uniform", -48, 56, 8, 4, 0, "alexscaves:toxic_caves")
    // immersive engineering nickel — temperate/cold (no NC nickel)
    addOregenOverworld(event, "kubejs:ore_nickel_underground", "immersiveengineering:ore_nickel", "minecraft:trapezoid", -40, 40, 4, 8, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_nickel_underground_big", "immersiveengineering:ore_nickel", "minecraft:trapezoid", -40, 40, 0.002, 64, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_nickel_underground_bottom", "immersiveengineering:ore_nickel", "minecraft:trapezoid", -84, -44, 5, 10, 0, "#c:is_cold/overworld")
    // galosphere palladium (silver) — cave landmarks + cold crust supplement
    addOregenOverworld(event, "kubejs:ore_palladium_caves_lichen", "galosphere:palladium_ore", "minecraft:uniform", -16, 48, 6, 6, 0, "galosphere:lichen_caves")
    addOregenOverworld(event, "kubejs:ore_palladium_caves_pink_salt", "galosphere:palladium_ore", "minecraft:uniform", -16, 48, 4, 9, 0, "galosphere:pink_salt_caves")
    addOregenOverworld(event, "kubejs:ore_palladium_underground_cold", "galosphere:palladium_ore", "minecraft:uniform", -32, 48, 3, 6, 0, "#c:is_cold/overworld")
    // niter: ore not necessary, there are several sources including tfmg geothermal vents and natural substitutes
    // peridot
    // addOregenOverworld(event, "kubejs:ore_peridot_underground_olivine", "projectred_exploration:peridot_ore", "minecraft:uniform", -64, -57, 1, 4, 0, "#minecraft:is_overworld")
    // addOregenOverworld(event, "kubejs:ore_peridot_underground_olivine_mantle", "projectred_exploration:peridot_ore", "minecraft:trapezoid", -158, 30, 5, 4, 0, "terralith:cave/mantle_caves")
    // addOregenOverworld(event, "kubejs:ore_peridot_islands_olivine_volcanic", "projectred_exploration:peridot_ore", "minecraft:uniform", 92, 400, 5, 4, 0, "#terralith:volcanic")
    // phosphorite
    // addOregenOverworld(event, "kubejs:ore_phosphorite_underground_big", "scguns:phosphorite", "minecraft:uniform", 0, 36, 0.01, 64, 0, "#minecraft:is_overworld")
    // platinum - gated like nuclearcraft ores once were, this will be a high level tech material
    // rich_phosphorite - original feature not disabled because it only spawns inside phosphorite, which we want
    // addOregenOverworld(event, "kubejs:ore_rich_phosphorite_guano", "scguns:rich_phosphorite", "minecraft:uniform", 62, 85, 40, 12, 0, ["regions_unexplored:rocky_reef", "regions_unexplored:tropics"])
    // redstone - disabled: cinnabar crushes into redstone, redstone caves and alexscaves primordial caves can also be mined for redstone dust
    // ruby
    // addOregenOverworld(event, "kubejs:ore_ruby_islands_hot", "thermal:ruby_ore", "minecraft:trapezoid", 92, 680, 5, 4, 0, "#c:is_hot/overworld")
    // sal_ammoniac
    addOregenOverworld(event, "kubejs:ore_sal_ammoniac_islands_mystical", "theurgy:sal_ammoniac_ore", "minecraft:trapezoid", 92, 680, 7, 4, 0, mysticalTerralithBiomes)
    addOregenOverworld(event, "kubejs:ore_sal_ammoniac_islands_mauve", "theurgy:sal_ammoniac_ore", "minecraft:trapezoid", 92, 680, 7, 4, 0, "regions_unexplored:mauve_hills")
    // sapphire
    // addOregenOverworld(event, "kubejs:ore_sapphire_islands_cold", "thermal:sapphire_ore", "minecraft:trapezoid", 92, 680, 5, 4, 0, "#c:is_snowy")
    // silver: CABIN says the only source should be melting coins but a little bit is fine
    addOregenOverworld(event, "kubejs:ore_silver_islands_temperate", "immersiveengineering:ore_silver", "minecraft:uniform", 92, 400, 25, 3, 0, "#c:is_temperate/overworld")
    addOregenOverworld(event, "kubejs:ore_silver_islands_cold", "immersiveengineering:ore_silver", "minecraft:uniform", 92, 400, 50, 3, 0, "#c:is_cold/overworld")
    addOregenOverworld(event, "kubejs:ore_silver_islands_ice", "immersiveengineering:ore_silver", "minecraft:trapezoid", 112, 912, 1, 6, 0, ["minecraft:ice_spikes", "terralith:glacial_chasm"])
    addOregenOverworld(event, "kubejs:ore_silver_underground_frostfire", "immersiveengineering:ore_silver", "minecraft:trapezoid", -168, 40, 0.01, 20, 0.5, "terralith:cave/frostfire_caves")
    // striated "ores" (stones) from tfmg disabled
    // sulfur: spawns in the nether and toxic caves already
    // tin
    // addOregenOverworld(event, "kubejs:ore_tin_islands_mountain", "thermal:tin_ore", "minecraft:uniform", 92, 400, 6, 6, 0, "#minecraft:is_mountain")
    // addOregenOverworld(event, "kubejs:ore_tin_islands_alluvial", "thermal:tin_ore", "minecraft:uniform", 92, 400, 256, 3, 0, "#minecraft:is_river")
    // addOregenOverworld(event, "kubejs:ore_tin_seabed_alluvial", "thermal:tin_ore", "minecraft:trapezoid", 42, 50, 10, 6, 0.7, "#minecraft:is_overworld")
    // addOregenOverworld(event, "kubejs:ore_tin_seabed_alluvial_big", "thermal:tin_ore", "minecraft:trapezoid", 42, 50, 0.005, 64, 0, "#minecraft:is_overworld")
    // addOregenOverworld(event, "kubejs:ore_tin_seabed_alluvial_deep", "thermal:tin_ore", "minecraft:trapezoid", 32, 42, 10, 6, 0.7, "#minecraft:is_deep_ocean")
    // addOregenOverworld(event, "kubejs:ore_tin_seabed_alluvial_big_deep", "thermal:tin_ore", "minecraft:trapezoid", 32, 42, 0.005, 64, 0, "#minecraft:is_overworld")
    // uranium: see nuclearcraft neohaul section above
    // vinteum
    // addOregenOverworld(event, "kubejs:ore_vinteum_islands_mystical_bottom", "mna:vinteum_ore", "minecraft:trapezoid", 80, 144, 8, 12, 0, "#terralith:mystical")
    // addOregenOverworld(event, "kubejs:ore_vinteum_islands_mystical_bottom_big", "mna:vinteum_ore", "minecraft:trapezoid", 80, 144, 0.005, 48, 0, "#terralith:mystical")
    // addOregenOverworld(event, "kubejs:ore_vinteum_islands_mauve_bottom", "mna:vinteum_ore", "minecraft:trapezoid", 80, 144, 8, 12, 0, "regions_unexplored:mauve_hills")
    // addOregenOverworld(event, "kubejs:ore_vinteum_islands_mauve_bottom_big", "mna:vinteum_ore", "minecraft:trapezoid", 80, 144, 0.005, 48, 0, "regions_unexplored:mauve_hills")
    // wanderlite
    // zinc — universal upper crust (Create); high island layers only
    addOregenOverworld(event, "kubejs:ore_zinc_islands_5", "create:zinc_ore", "minecraft:trapezoid", 272, 400, 8, 12, 0, "#minecraft:is_overworld")
    addOregenOverworld(event, "kubejs:ore_zinc_islands_4", "create:zinc_ore", "minecraft:trapezoid", 224, 352, 2, 12, 0, "#minecraft:is_overworld")


})
