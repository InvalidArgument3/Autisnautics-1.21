// priority: 4
// Dimension-based progression gates for Autisnautics 1.21:
// - Layer 3 (dimension_of_caves:cave): AE2 + Alex's Caves Neodymium
// - Layer 6 (undergarden:undergarden): Ars Nouveau & Iron's Spells starter gates
// - Layer 7 (infernalcross:dimension): Pre-Infernal NuclearCraft radiation protection
// - Space (stellaris): Shadow Steel rocket body & Refined Radiance nose cone

ServerEvents.recipes(event => {
    // =========================================================================
    // 1. Stellaris Rocket Gate — Shadow Steel body + Refined Radiance nose cone
    // =========================================================================
    if (Platform.isLoaded("stellaris")) {
        event.remove({ id: "stellaris:rocket" })
        event.custom({
            type: "stellaris:rocket_station",
            ingredients: [
                { item: "stellaris:rocket_nose_cone" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "stellaris:rocket_fin" },
                { item: "create:shadow_steel" },
                { item: "create:shadow_steel" },
                { item: "stellaris:rocket_fin" },
                { item: "stellaris:rocket_fin" },
                { item: "stellaris:rocket_engine" },
                { item: "stellaris:rocket_fin" }
            ],
            output: {
                id: "stellaris:rocket",
                count: 1
            }
        }).id("kubejs:stellaris_rocket_shadow_steel")

        event.remove({ id: "stellaris:misc/rocket_nose_cone" })
        event.shaped("stellaris:rocket_nose_cone", [
            " R ",
            " # ",
            "###"
        ], {
            R: "create:refined_radiance",
            "#": "create:shadow_steel"
        }).id("kubejs:stellaris_nose_cone_refined_radiance")
    }

    // =========================================================================
    // 2. AE2 + Alex's Caves Neodymium Gate (dimension_of_caves:cave)
    // =========================================================================
    if (Platform.isLoaded("ae2") && Platform.isLoaded("alexscaves")) {
        event.remove({ id: "ae2:network/blocks/inscribers" })
        event.shaped("ae2:inscriber", [
            "SPS",
            "A F",
            "SPS"
        ], {
            S: "alexscaves:scarlet_neodymium_ingot",
            A: "alexscaves:azure_neodymium_ingot",
            P: "minecraft:sticky_piston",
            F: "#c:ingots/copper"
        }).id("kubejs:ae2_inscriber_neodymium")

        event.remove({ id: "ae2:network/blocks/crystal_processing_charger" })
        event.shaped("ae2:charger", [
            "ACA",
            "I  ",
            "ACA"
        ], {
            A: "alexscaves:azure_neodymium_ingot",
            C: "#c:ingots/copper",
            I: "#c:ingots/iron"
        }).id("kubejs:ae2_charger_neodymium")

        event.remove({ id: "ae2:network/blocks/controller" })
        event.shaped("ae2:controller", [
            "SFS",
            "ETE",
            "SFS"
        ], {
            S: "ae2:smooth_sky_stone_block",
            F: "ae2:fluix_crystal",
            E: "ae2:engineering_processor",
            T: "alexscaves:tesla_bulb"
        }).id("kubejs:ae2_controller_tesla")

        // Craftable AE2 presses gated by Cave-dim Neodymium + Certus Quartz
        event.shaped("ae2:calculation_processor_press", ["SNS", "NCN", "SNS"], {
            S: "alexscaves:scarlet_neodymium_ingot",
            N: "#c:ingots/iron",
            C: "ae2:certus_quartz_crystal"
        }).id("kubejs:ae2_press_calculation_neodymium")
        event.shaped("ae2:engineering_processor_press", ["SNS", "NDN", "SNS"], {
            S: "alexscaves:azure_neodymium_ingot",
            N: "#c:ingots/iron",
            D: "#c:gems/diamond"
        }).id("kubejs:ae2_press_engineering_neodymium")
        event.shaped("ae2:logic_processor_press", ["SNS", "NGN", "SNS"], {
            S: "alexscaves:scarlet_neodymium_ingot",
            N: "#c:ingots/iron",
            G: "#c:ingots/gold"
        }).id("kubejs:ae2_press_logic_neodymium")
        event.shaped("ae2:silicon_press", ["ANA", "NQN", "ANA"], {
            A: "alexscaves:azure_neodymium_ingot",
            N: "#c:ingots/iron",
            Q: "ae2:silicon"
        }).id("kubejs:ae2_press_silicon_neodymium")
    }

    // =========================================================================
    // 3. Undergarden Starter Gates — Ars Nouveau & Iron's Spells (independent)
    // =========================================================================
    if (Platform.isLoaded("ars_nouveau") && Platform.isLoaded("undergarden")) {
        event.remove({ id: "ars_nouveau:novice_spell_book" })
        event.shapeless("ars_nouveau:novice_spell_book", [
            "minecraft:book",
            "undergarden:cloggrum_ingot",
            "undergarden:utherium_crystal",
            "minecraft:iron_shovel",
            "minecraft:iron_pickaxe",
            "minecraft:iron_axe",
            "minecraft:iron_sword"
        ]).id("kubejs:ars_novice_spell_book_undergarden")

        event.remove({ id: "ars_nouveau:imbuement_chamber" })
        event.shaped("ars_nouveau:imbuement_chamber", [
            "CGC",
            "AUA",
            "CGC"
        ], {
            C: "undergarden:cloggrum_ingot",
            G: "#c:ingots/gold",
            A: "#ars_nouveau:archwood_planks",
            U: "undergarden:utherium_crystal"
        }).id("kubejs:ars_imbuement_chamber_undergarden")
    }

    if (Platform.isLoaded("irons_spellbooks") && Platform.isLoaded("undergarden")) {
        event.remove({ id: "irons_spellbooks:scroll_forge" })
        event.shaped("irons_spellbooks:scroll_forge", [
            "DDD",
            " U ",
            "CCC"
        ], {
            D: "minecraft:polished_deepslate",
            U: "undergarden:utherium_crystal",
            C: "undergarden:cloggrum_ingot"
        }).id("kubejs:irons_scroll_forge_undergarden")

        event.remove({ id: "irons_spellbooks:arcane_anvil" })
        event.shaped("irons_spellbooks:arcane_anvil", [
            "FFF",
            " A ",
            "DRD"
        ], {
            F: "undergarden:froststeel_ingot",
            A: "minecraft:amethyst_block",
            D: "minecraft:polished_deepslate",
            R: "undergarden:regalium_crystal"
        }).id("kubejs:irons_arcane_anvil_undergarden")
    }

    // =========================================================================
    // 4. Pre-Infernal Cross NuclearCraft Radiation Protection
    //    Craftable before entering radioactive infernalcross:dimension
    // =========================================================================
    if (Platform.isLoaded("nuclearcraftneohaul")) {
        const rubber = Item.exists("kubejs:rubber") ? "kubejs:rubber" : "minecraft:leather"
        const lead = "#c:ingots/brass"

        event.shaped("nuclearcraftneohaul:hazmat_helmet", [
            "RRR",
            "RGR",
            "L L"
        ], {
            R: rubber,
            G: "#c:glass_blocks",
            L: lead
        }).id("kubejs:nc_pre_infernal_hazmat_helmet")

        event.shaped("nuclearcraftneohaul:hazmat_chestplate", [
            "R R",
            "RLR",
            "RRR"
        ], {
            R: rubber,
            L: lead
        }).id("kubejs:nc_pre_infernal_hazmat_chestplate")

        event.shaped("nuclearcraftneohaul:hazmat_leggings", [
            "RRR",
            "L L",
            "R R"
        ], {
            R: rubber,
            L: lead
        }).id("kubejs:nc_pre_infernal_hazmat_leggings")

        event.shaped("nuclearcraftneohaul:hazmat_boots", [
            "L L",
            "R R"
        ], {
            R: rubber,
            L: lead
        }).id("kubejs:nc_pre_infernal_hazmat_boots")

        event.shapeless("nuclearcraftneohaul:rad_x", [
            "minecraft:glass_bottle",
            "minecraft:golden_carrot",
            "minecraft:glowstone_dust",
            "minecraft:dried_kelp"
        ]).id("kubejs:nc_pre_infernal_rad_x")

        event.shapeless("nuclearcraftneohaul:radaway", [
            "minecraft:glass_bottle",
            "minecraft:golden_apple",
            "minecraft:redstone",
            "minecraft:sugar"
        ]).id("kubejs:nc_pre_infernal_radaway")
    }
})
