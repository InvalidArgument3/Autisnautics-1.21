ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")

    // //create stuff & additions wrangling
    // replace create_sa clown mechanisms with a&b mechanisms
    // event.replaceInput({}, "create_sa:heat_engine", "kubejs:infernal_mechanism")// flamethrower and grapplin whisk
    // event.replaceInput({}, "create_sa:hydraulic_engine", "kubejs:sealed_mechanism")
    // event.replaceInput({}, "create_sa:steam_engine", "create:precision_mechanism")
    // // redundant create_sa fuel tanks
    // event.remove({ output: "create_sa:small_fueling_tank" })
    // event.remove({ output: "create_sa:large_fueling_tank" })
    // // make create_sa andesite exoskeleton andesite tier
    // event.remove({ output: "create_sa:andesite_exoskeleton_chestplate" })
    // event.shaped("create_sa:andesite_exoskeleton_chestplate", [
    //     "SBS",
    //     "AKA",
    //     "ZAZ"
    // ], {
    //     A: "create:andesite_alloy",
    //     B: "create:belt_connector",
    //     S: "create:shaft",
    //     K: "kubejs:kinetic_mechanism",
    //     Z: "create:zinc_block"
    // })

    // //sturdy sheet replacements
    // event.remove({ output: "create_sa:medium_fueling_tank" })
    // event.shaped("create_sa:medium_fueling_tank", [
    //     " P ",
    //     "PTP",
    //     " P "
    // ], {
    //     T: "create:fluid_tank",
    //     P: "create:iron_sheet"
    // })
    // sturdy sheet → gold sheet for aeronautics / aircraft parts (replaces old VS Clockwork / Railways)
    if (Item.exists("aeronautics:smart_propeller")) {
        event.replaceInput({ output: "aeronautics:smart_propeller" }, "create:sturdy_sheet", "create:golden_sheet")
    }
    if (Item.exists("immersive_aircraft:engine")) {
        event.replaceInput({ output: "immersive_aircraft:engine" }, "create:sturdy_sheet", "create:golden_sheet")
    }
    event.replaceInput({ output: "immersive_aircraft:quadrocopter" }, "create:sturdy_sheet", "create:golden_sheet")
    // event.replaceInput({ output: "create_sa:flamethrower" }, "create:sturdy_sheet", "minecraft:netherite_ingot")
    event.remove({ output: "immersive_aircraft:nether_engine" })
    event.remove({ output: "immersive_aircraft:eco_engine" })
    zincMachine(event, Item.of("immersive_aircraft:nether_engine", 1), "immersive_aircraft:engine")
    leadMachine(event, Item.of("immersive_aircraft:eco_engine", 1), "immersive_aircraft:engine")

    // //aircraft/jetpack tiers
    // andesite: gyrodyne, andesite jetpack
    event.replaceInput({ output: "immersive_aircraft:gyrodyne" }, "create:precision_mechanism", "kubejs:kinetic_mechanism")
    // event.remove({ output: "create_sa:andesite_jetpack_chestplate" })
    // event.shaped("create_sa:andesite_jetpack_chestplate", [
    //     "CBC",
    //     "AKA",
    //     "FAF"
    // ], {
    //     A: "create:andesite_alloy",
    //     B: "create:belt_connector",
    //     C: "create:cogwheel",
    //     K: "kubejs:kinetic_mechanism",
    //     F: "create:encased_fan"
    // })
    // copper: copper jetpack - mechanism already replaced
    // gold: airships, quadrocopter (aeronautics gold machines in chapters.js)
    event.replaceInput({ output: "immersive_aircraft:quadrocopter" }, "immersive_aircraft:boiler", "kubejs:gold_machine")
    event.remove({ output: "immersive_aircraft:airship" })
    if (Item.exists("immersive_aircraft:airship")) {
        event.shaped("immersive_aircraft:airship", [
            "SSS",
            "R R",
            "HGP"
        ], {
            S: "immersive_aircraft:sail",
            R: "simulated:rope_coupling",
            P: "create:propeller",
            H: "immersive_aircraft:hull",
            G: "kubejs:gold_machine"
        })
    }

    // brass: engine, warship, planes, brass jetpack
    // event.remove({ output: "immersive_aircraft:engine" })
    // brassMachine(event, Item.of("immersive_aircraft:engine", 1), "minecraft:blast_furnace")

    // //tfmg/IE integration
    // re-add blasting mixture recipe as metallurgy.js removes all input:#create:crushed_raw_materials
    // event.recipes.create.mixing("tfmg:blasting_mixture", [Item.of("thermal:iron_dust", 3), "tfmg:limesand"])// Item.of(#tag) doesnt work right for some reason
    // thermal rockwool and IE slag glass conflict: rockwool is now blasting only, slag glass is smelting only
    if (hasThermal) {
        event.remove({ type: "minecraft:smelting", output: "thermal:white_rockwool" })
    }
    event.remove({ type: "create:fan_blasting", output: "immersiveengineering:slag_glass" })

    // conflict with thermal:slag_block: slag brick is now stonecutting only
    event.remove({ type: "minecraft:crafting_shaped", output: "immersiveengineering:slag_brick" })

    // get all our slags in one place
    if (hasThermal) {
        event.replaceInput({}, "thermal:slag", "#forge:slag")
        event.replaceOutput({}, "immersiveengineering:slag", "thermal:slag")
        event.replaceInput({}, "thermal:sawdust", "#forge:dusts/wood")
        event.replaceOutput({}, "immersiveengineering:dust_wood", "thermal:sawdust")
    }
    event.replaceInput({}, "immersiveengineering:slag", "#forge:slag")

    event.remove({ output: "immersiveengineering:sawdust" })// sawdust floor conflicts with JAOPCA storage block
    event.shapeless("3x immersiveengineering:sawdust", ["immersiveengineering:dust_wood"])

    // unify sawdusts
    event.remove({ output: "jaopca:storage_blocks.wood" })
    event.remove({ input: "jaopca:storage_blocks.wood" })
    event.replaceInput({}, "immersiveengineering:dust_wood", "#forge:dusts/wood")
    if (hasThermal) {
        event.replaceInput({}, "thermal:sawdust", "#forge:dusts/wood")
        event.replaceOutput({}, "immersiveengineering:dust_wood", "thermal:sawdust")
    }

    // remove vanilla blast furnace -> steel (lol?)
    event.remove({ type: "minecraft:blasting", input: "minecraft:iron_ingot" })// done by input in case of steel unification changes
    // chainmail no longer meltable (it gave steel and chainmail can be crafted)
    if (Platform.isLoaded("tconstruct")) {
        event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_helmet" })
        event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_chestplate" })
        event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_leggings" })
        event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_boots" })
    }


    // Create Addition: disable rotational force/energy conversion because powergrid does it better
    event.remove({ output: "createaddition:electric_motor" })
    event.remove({ input: "createaddition:electric_motor" })
    event.remove({ output: "createaddition:alternator" })

    // //using thermal:device_fisher in place of the water strainer mod
    // use canvas in slot to get sand and/or clay
    if (hasThermal) {
        event.recipes.thermal.fisher_boost('farmersdelight:canvas', 2, 0.02, 'autisnautics2datapack:gameplay/straining')
    }
    // add a few more sources of straw for canvas
    event.custom({// wheat
        "type": "farmersdelight:cutting",
        "ingredients": [{ "item": "minecraft:wheat" }],
        "result": [
            { "item": { "id": "farmersdelight:straw" } },
            { "item": { "id": "minecraft:wheat_seeds" }, "chance": 0.25 }
        ],
        "tool": { "tag": "c:tools/knives" }
    })
    event.custom({// flax
        "type": "farmersdelight:cutting",
        "ingredients": [{ "item": "supplementaries:flax" }],
        "result": [
            { "item": { "id": "farmersdelight:straw" } },
            { "item": { "id": "minecraft:string" } }
        ],
        "tool": { "tag": "c:tools/knives" }
    })
    // modify aquatic entangler recipe so it's easier to get
    if (hasThermal) {
        event.replaceInput({ output: "thermal:device_fisher" }, "thermal:redstone_servo", "minecraft:barrel")
        // make junk net more expensive (it's unbreakable and surprisingly useful)
        event.replaceInput({ output: "thermal:junk_net" }, "minecraft:iron_nugget", "#forge:ingots/lead")
        // Rats not in pack yet — restore when mod updates
        // event.replaceInput({ output: "thermal:junk_net" }, "minecraft:stick", "rats:garbage_pile")
    }

    // tfmg/ie synthetic leathers and strings support
    event.replaceInput({}, "minecraft:leather", "#forge:leathers")
    // event.replaceInput({}, "minecraft:string", "#forge:string")
    // fix synthetic leather recipe collision with immersiveengineering:plate_duroplast
    // event.remove({ output: "tfmg:synthetic_leather" })
    // event.recipes.create.deploying("tfmg:synthetic_leather", ["#forge:ingots/plastic", "minecraft:paper"])

    // make tome of alkahestry an endgame item
    if (Item.exists("enigmaticlegacy:withered_tome")) {
        event.remove({ type: "minecraft:crafting_shapeless", input: "reliquary:witch_hat", output: "reliquary:alkahestry_tome" })
        event.recipes.create.mechanical_crafting("reliquary:alkahestry_tome", [
            " HTE ",
            "  A  ",
            "SXADW",
            "PGMRP",
            "OOCOO"
        ], {
            T: "enigmaticlegacy:withered_tome",
            E: "reliquary:eye_of_the_storm",
            G: "kubejs:accelerator_glowstone",
            R: "kubejs:accelerator_redstone",
            M: "kubejs:missingno",
            C: itemOr("botania:conjuration_catalyst", "minecraft:nether_star"),
            H: "dungeonnowloading:chaotic_hexahedron",
            O: "minecraft:crying_obsidian",
            P: Item.exists("embers:alchemy_pedestal") ? "embers:alchemy_pedestal" : "minecraft:obsidian",
            X: "enigmaticlegacy:forbidden_axe",
            S: "minecraft:player_head",
            D: "reliquary:phoenix_down",
            A: "reliquary:alkahestry_altar",
            W: "minecraft:wither_skeleton_skull",
        })
    }

    // //chapter fixes
    // unfuck wood plank cutting
    event.remove({ output: "cuisinedelight:plate" })
    // event.remove({ output: "tfmg:formwork_block" })
    let handrailTypes = ["oak", "birch", "spruce", "jungle", "dark_oak", "acacia", "crimson", "warped", "mangrove", "cherry", "bamboo"]
    let removeHandrails = (wood) => {
        let originalHandrail = "youkaishomecoming:" + wood + "_handrail"
        if (Item.exists(originalHandrail)) {
            event.remove({ output: originalHandrail })
        }
    }
    handrailTypes.forEach(removeHandrails)

    // wooden slab → plates lives in chapter_1.js (createCuttingTag — EMI half-item icon)
    // formwork blocks are stonecut from hollow logs
    // event.stonecutting(Item.of("tfmg:formwork_block", 8), "#quark:hollow_logs")
    // handrails get a shaped recipe
    let addHandrails = (wood) => {
        let resultingHandrail = "youkaishomecoming:" + wood + "_handrail"
        let woodItem = "minecraft:" + wood + "_slab"
        if (Item.exists(resultingHandrail)) {
            event.shaped(Item.of(resultingHandrail, 4), [
                "SSS",
                "T T"
            ], {
                S: woodItem,
                T: "minecraft:stick"
            })
        }
    }
    handrailTypes.forEach(addHandrails)

    // replace create big cannons mould recipes which are all the exact same recipe because the modder is a twisted psychopath
    let mouldTypes = ["very_small", "small", "medium", "large", "very_large", "cannon_end", "sliding_breech", "screw_breech", "autocannon_breech", "autocannon_recoil_spring", "autocannon_barrel"]
    let removeMould = (type) => {
        let originalMould = "createbigcannons:" + type + "_cast_mould"
        if (Item.exists(originalMould)) {
            event.remove({ output: originalMould })
        }
    }
    mouldTypes.forEach(removeMould)
    // all moulds now start from the very large cast mould, by deploying a saw on any log
    deployTool(event, "createbigcannons:very_large_cast_mould", "#minecraft:logs", "#kubejs:saws")
    deployTool(event, "createbigcannons:large_cast_mould", "createbigcannons:very_large_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:medium_cast_mould", "createbigcannons:large_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:small_cast_mould", "createbigcannons:medium_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:very_small_cast_mould", "createbigcannons:small_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:autocannon_breech_cast_mould", "createbigcannons:very_small_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:autocannon_recoil_spring_cast_mould", "createbigcannons:autocannon_breech_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:autocannon_barrel_cast_mould", "createbigcannons:autocannon_recoil_spring_cast_mould", "#kubejs:saws")
    deployTool(event, "createbigcannons:sliding_breech_cast_mould", "createbigcannons:large_cast_mould", "#forge:chisels")
    deployTool(event, "createbigcannons:cannon_end_cast_mould", "createbigcannons:medium_cast_mould", "#forge:chisels")
    deployTool(event, "createbigcannons:screw_breech_cast_mould", "createbigcannons:cannon_end_cast_mould", "#kubejs:saws")

    // might as well fix this createbigcannons shit while i'm at it
    event.remove({ id: "createbigcannons/cutting/autocannon_cartridge_sheet_copper" })// duplicate
    event.replaceInput({ output: "createbigcannons:spring_wire" }, "#forge:plates/iron", "#c:plates/lead")// another ingredient+type with multiple outputs

    // //Chapter 2B: Lead Machines replacement
    // replacing the chassis with the lead machine
    event.remove({ output: "nuclearcraftneohaul:machine_chassis" })
    event.replaceInput({ output: "nuclearcraftneohaul:turbine_casing" }, "nuclearcraftneohaul:machine_chassis", "kubejs:lead_casing")// exception
    event.replaceInput({}, "nuclearcraftneohaul:machine_chassis", "kubejs:lead_machine")
    // gate manufactory and alloy furnace behind lead machine as well
    event.replaceInput({ output: "nuclearcraftneohaul:manufactory" }, "minecraft:piston", "kubejs:lead_machine")
    event.replaceInput({ output: "nuclearcraftneohaul:alloy_furnace" }, "minecraft:blast_furnace", "kubejs:lead_machine")

    // //atomic mechanisms — logistic mechanism lives in chapter_2b.js (pulp + haste potion)
    // Radaway and related NC recipes below
    event.recipes.create.mixing([Fluid.of("nuclearcraftneohaul:radaway", 250)], [Item.of("nuclearcraftneohaul:glowing_mushroom", 3), Fluid.of("minecraft:milk", 250)]).heated()
    // original NC recipe: replace Ethanol with Milk
    event.remove({ output: Fluid.of("nuclearcraftneohaul:radaway") })
    event.remove({ output: Fluid.of("nuclearcraftneohaul:radaway_slow") })
    // remove NC ethanol while we're at it, other mods do it better
    event.remove({ output: Fluid.of("nuclearcraftneohaul:ethanol") })
    event.remove({ output: Fluid.of("nuclearcraftneohaul:redstone_ethanol") })
    event.remove({ output: "nuclearcraftneohaul:ethanol_bucket" })
    event.remove({ output: "nuclearcraftneohaul:redstone_ethanol_bucket" })

    event.custom(ncEnricherRecipe(
        [{ count: 3, item: "nuclearcraftneohaul:glowing_mushroom" }],
        [{ amount: 250, tag: "kubejs:milk" }],
        [{ amount: 250, fluid: "nuclearcraftneohaul:radaway" }],
        { radiation: 1.0 }
    ))
    event.custom(ncEnricherRecipe(
        [{ count: 1, item: "minecraft:redstone" }],
        [{ amount: 250, tag: "forge:radaway" }],
        [{ amount: 250, fluid: "nuclearcraftneohaul:radaway_slow" }],
        { radiation: 1.0 }
    ))
    // synthetic glowing mushroom recipe: 50mb of Potion of Glowing on a brown mushroom
    event.recipes.create.filling("nuclearcraftneohaul:glowing_mushroom", [
        "minecraft:brown_mushroom",
        Fluid.sizedIngredientOf(Fluid.ingredientOf("create:potion", {
            "create:potion_fluid_bottle_type": "regular",
            "minecraft:potion_contents": { potion: "alexscaves:glowing" }
        }), 50)
    ])
    // potion of glowing recipe in startup_scripts


    // //just one word: PLASTICS
    // tfmg-less edition: end product is now nuclearcraft:bioplastic

    // Rats not in pack yet — restore when mod updates
    // event.remove({ output: "rats:raw_plastic" })
    // if (Item.exists("rats:plastic_waste")) {
    //     event.remove({ input: "rats:plastic_waste" })
    // }
    // if (Item.exists("rats:raw_plastic")) {
    //     event.replaceInput({}, "rats:raw_plastic", "#forge:ingots/plastic")
    // }
    //
    // // converting plastic waste to liquid plastic by "recycling", now with kubejs:liquid_plastic
    // if (Item.exists("rats:plastic_waste")) {
    //     event.recipes.create.mixing([Fluid.of("kubejs:liquid_plastic", 10), "quark:dirty_shard"], [Item.of("rats:plastic_waste", 9), Fluid.of("minecraft:water", 1000)]).heated()
    //     event.custom(ncMelterRecipe(
    //         [{ count: 1, item: "rats:plastic_waste" }],
    //         [{ amount: 10, tag: "kubejs:liquid_plastic" }],
    //         { radiation: 1.0 }
    //     ))
    // }
    // melting bioplastic to liquid for whatever reason
    event.custom(ncMelterRecipe([{ tag: "forge:ingots/plastic" }], [{ amount: 90, fluid: "kubejs:liquid_plastic" }], { radiation: 1.0 }))
    // // converting raw_plastic (loot only) to liquid_plastic
    // if (Item.exists("rats:raw_plastic")) {
    //     event.recipes.create.mixing([Fluid.of("kubejs:liquid_plastic", 90)], [Item.of("rats:raw_plastic", 1), Fluid.of("minecraft:water", 250)]).heated()
    //     event.custom(ncMelterRecipe(
    //         [{ count: 1, item: "rats:raw_plastic" }],
    //         [{ amount: 90, fluid: "kubejs:liquid_plastic" }],
    //         { radiation: 1.0 }
    //     ))
    // }

    // forming bioplastic from liquid plastic — blazinghot sheet mold (tag fluid for EMI icon)
    if (Platform.isLoaded("blazinghot")) {
        ;["porcelain", "sturdy"].forEach(moldMat => {
            event.custom({
                "type": "create:casting",
                "cooling_duration": 100,
                "ingredients": [
                    { "item": `blazinghot:${moldMat}_sheet_mold` },
                    {
                        "type": "neoforge:tag",
                        "amount": 90,
                        "tag": "forge:liquid_plastic"
                    }
                ],
                "keep_mold": true,
                "processing_time": 100,
                "results": [{ "id": "nuclearcraftneohaul:bioplastic" }]
            }).id(`kubejs:casting/bioplastic_${moldMat}_sheet`)
        })
    }
    event.custom(ncIngotFormerRecipe([{ amount: 90, tag: "forge:liquid_plastic" }], [{ item: "nuclearcraftneohaul:bioplastic" }], { radiation: 1.0 }))

    // replace sugarcane for bioplastic with biomass
    event.remove({ output: "nuclearcraftneohaul:bioplastic" })
    event.custom(ncManufactoryRecipe([{ count: 2, item: "createaddition:biomass" }], [{ item: "nuclearcraftneohaul:bioplastic" }], { radiation: 1.0 }))


    // fix broken radaway item recipes
    event.remove({ output: Item.of("nuclearcraftneohaul:radaway") })
    event.remove({ output: Item.of("nuclearcraftneohaul:radaway_slow") })
    createFillingJson(event, "nuclearcraftneohaul:radaway", "nuclearcraftneohaul:bioplastic", "nuclearcraftneohaul:radaway", 250, "kubejs:filling/radaway")
    createFillingJson(event, "nuclearcraftneohaul:radaway_slow", "nuclearcraftneohaul:bioplastic", "nuclearcraftneohaul:radaway_slow", 250, "kubejs:filling/radaway_slow")
    event.custom(ncInfuserRecipe(
        [{ amount: 250, tag: "forge:radaway" }],
        [{ tag: "forge:ingots/plastic" }],
        [{ item: "nuclearcraftneohaul:radaway" }],
        { radiation: 1.0 }
    ))
    event.custom(ncInfuserRecipe(
        [{ amount: 250, tag: "forge:radaway_slow" }],
        [{ tag: "forge:ingots/plastic" }],
        [{ item: "nuclearcraftneohaul:radaway_slow" }],
        { radiation: 1.0 }
    ))

    // remove redundant "rose quartz" for polished rose quartz
    event.replaceOutput({}, "create:rose_quartz", "create:polished_rose_quartz")// jaopca only
    // event.replaceInput({}, "create:rose_quartz", "create:polished_rose_quartz")// jaopca, cosmetic stonecutting block, rock candy

    // remove bugged melting recipe for "raw tungsten" which doesn't exist
    if (Platform.isLoaded("tconstruct")) {
        event.remove({ id: "tconstruct:smeltery/melting/metal/tungsten/raw" })
        event.remove({ id: "tconstruct:smeltery/melting/metal/tungsten/raw_block" })
    }

    // tfmg meme stones
    // event.remove({ input: /.*tfmg.*galena.*/ })
    // event.remove({ input: /.*tfmg.*bauxite.*/ })
    // event.remove({ input: "tfmg:lignite" })
    // event.remove({ output: /.*tfmg.*galena.*/ })
    // event.remove({ output: /.*tfmg.*bauxite.*/ })
    // event.remove({ output: "tfmg:lignite" })

    // alexscaves limestone integration
    event.replaceInput({}, "alexscaves:limestone", "#forge:stone/limestone")
    event.replaceInput({}, "create:limestone", "#forge:stone/limestone")

    // alexscaves galena to lead
    event.recipes.create.crushing([
        CreateItem.of("immersiveengineering:dust_lead", 0.05),
        CreateItem.of("create:crushed_raw_lead", 0.01),
        CreateItem.of("nuclearcraftneohaul:lead_raw", 0.001)
    ], "alexscaves:galena").processingTime(500)

    // alexscaves scrap metal gacha (Create 1.21 crushing: max 7 outputs per recipe)
    let scrapMetalOutputs = [
        CreateItem.of("minecraft:iron_nugget", 0.5),
        CreateItem.of("create:copper_nugget", 0.03),
        CreateItem.of("create:zinc_nugget", 0.03),
        CreateItem.of("nuclearcraftneohaul:tin_nugget", 0.03),
        CreateItem.of("nuclearcraftneohaul:lead_nugget", 0.03),
        CreateItem.of("createbigcannons:cast_iron_nugget", 0.025),
        CreateItem.of("createdeco:industrial_iron_nugget", 0.025),
    ]
    event.recipes.create.crushing(scrapMetalOutputs, "alexscaves:scrap_metal").processingTime(1)

    // synthesize alexscaves neodymium using nuclearcraft neodymium dust which is otherwise useless
    event.custom(ncAssemblerRecipe([{ tag: "forge:dusts/neodymium" }, { item: "kubejs:substrate_cinnabar" }], [{ item: "alexscaves:raw_scarlet_neodymium" }], { radiation: 1.0 }))
    event.custom(ncAssemblerRecipe([{ tag: "forge:dusts/neodymium" }, { tag: "forge:gems/lapis" }], [{ item: "alexscaves:raw_azure_neodymium" }], { radiation: 1.0 }))

    // globes for the wasteland
    event.remove({ output: "supplementaries:globe" })
    event.remove({ output: "supplementaries:globe_sepia" })
    // sepia globe: globe in NC nuclear furnace
    event.custom(ncAlloyFurnaceRecipe([{ item: "supplementaries:globe" }, { item: "minecraft:clock" }], [{ item: "supplementaries:globe_sepia" }], { radiation: 1.0, time: 30.0 }))
    // globe
    event.shaped("supplementaries:globe", [
        "RM ",
        "MBM",
        " MR"
    ], {
        R: "createaddition:brass_rod",
        M: "minecraft:map",
        B: "#c:slimeballs"
    })
    // sodium chloride is salt
    if (Item.exists("jaopca:storage_blocks.sodium_chloride")) {
        event.remove({ input: "jaopca:storage_blocks.sodium_chloride" })
        event.remove({ output: "jaopca:storage_blocks.sodium_chloride" })
    }

    // //createdeco
    // zinc sheets unification
    event.replaceInput({}, "createdeco:zinc_sheet", "#forge:plates/zinc")
    // remove useless netherite sheet
    if (Item.exists("createdeco:netherite_sheet")) {
        event.remove({ output: "createdeco:netherite_sheet" })
    }
    // no counterfeiting
    event.remove({ input: /^createdeco:.*coin(stack)?$/ })
    event.remove({ output: /^createdeco:.*coin(stack)?$/ })

    // forcing the use of ae2 ender dust
    if (Item.exists("thermal:ender_pearl_dust")) {
        event.replaceOutput({}, "thermal:ender_pearl_dust", "ae2:ender_dust")
    }

    // adding electrotine alloy recipes (where did the original one go?)
    if (Item.exists("projectred_core:electrotine_dust") && Item.exists("projectred_core:electrotine_ingot")) {
        event.shapeless("projectred_core:electrotine_ingot", [Item.of("projectred_core:electrotine_dust", 8), "minecraft:iron_ingot"])
    }

    // //fluid unification
    if (Platform.isLoaded("embers")) {
        event.remove({ id: "embers:mixing/molten_invar" })// progression meme
    }
    // the rest is in A2 datapack (kubejs incompatible)
    // nuclearcraft:molten_.*
    let unifiedNCFluids = ["bronze", "cobalt", "electrum", "lead", "platinum", "silver", "tin", "uranium", "zinc"]
    unifiedNCFluids.forEach(fluid => {
        let moltenId = null
        if (Fluid.exists("blazinghot:molten_" + fluid)) {
            moltenId = "blazinghot:molten_" + fluid
        } else if (Fluid.exists("nuclearcraftneohaul:molten_" + fluid)) {
            moltenId = "nuclearcraftneohaul:molten_" + fluid
        } else if (Platform.isLoaded("tconstruct") && Fluid.exists("tconstruct:molten_" + fluid)) {
            moltenId = "tconstruct:molten_" + fluid
        }
        if (moltenId) {
            let ncMolten = "nuclearcraftneohaul:molten_" + fluid
            if (Fluid.exists(ncMolten)) {
                event.replaceOutput({ type: "nuclearcraftneohaul:melter_recipe" }, ncMolten, moltenId)
            }
        }
    })

    // remove jaopca "molten coal" for uselessness and possible dupe exploit
    event.remove({ id: /^jaopca:.*molten.*coal$/ })

    // fixing weird coal coke storage block recipes
    if (hasThermal) {
        event.remove({ id: "thermal:storage/coal_coke_block" })
    }
    event.remove({ id: "immersiveengineering:crafting/coal_coke_to_coke" })
    // event.remove({ id: "tfmg:crafting/coal_coke_block" })
    //
    // event.remove({ id: "tfmg:crafting/coal_coke_from_block" })
    event.remove({ id: "immersiveengineering:crafting/coke_to_coal_coke" })
    // event.remove({ output: "tfmg:coal_coke_block" })
    if (hasThermal) {
        event.remove({ id: "thermal:storage/coal_coke_block" })
        event.shapeless("thermal:coal_coke_block", ["9x #forge:coal_coke"])
    }

    // //creosote unification - have to use IE because it's hardcoded
    // tfmg hardened wood block = treated wood block + 125mb more creosote
    // event.remove({ id: "tfmg:filling/hardened_wood_creosote" })
    // event.recipes.create.filling("tfmg:hardened_planks", ["#forge:treated_wood", Fluid.of("immersiveengineering:creosote", 125)])

    // //powergrid integration
    // replace conductive casing recipes with zinc machine
    // event.replaceInput({}, "powergrid:conductive_casing", "kubejs:zinc_machine")
    // it's now a cosmetic block so make it cheaper
    // event.remove({ output: "powergrid:conductive_casing" })
    // event.shapeless(Item.of("powergrid:conductive_casing", 8), ["#forge:ingots/zinc", "create:andesite_casing"])
    // various junctions, connectors, etc. are now from stonecutting zinc machine 8:1
    let powergridWidgets = ["cord_junction", "lv_switch", "lv_button", "light_fixture", "device_connector", "socket", "fuse_holder"]
    powergridWidgets.forEach(widget => {
        event.remove({ output: "powergrid:" + widget })
        zincMachine(event, Item.of("powergrid:" + widget, 8))
    })
    // machinify some more recipes
    event.recipes.create.filling("powergrid:battery", ["kubejs:zinc_machine", Fluid.of("powergrid:acid", 250)])
    event.remove({ output: "powergrid:mv_switch" })
    zincMachine(event, Item.of("powergrid:mv_switch", 4))
    event.remove({ output: "powergrid:spark_gap" })
    zincMachine(event, Item.of("powergrid:spark_gap", 4))
    event.remove({ id: "powergrid:crafting/generator_housing" })
    zincMachine(event, Item.of("powergrid:generator_housing", 2))
    event.remove({ output: "powergrid:hv_switch" })
    zincMachine(event, Item.of("powergrid:hv_switch", 1), Ingredient.of("#c:rods/iron"))
    event.remove({ output: "powergrid:hv_breaker" })
    zincMachine(event, Item.of("powergrid:hv_breaker", 1), "create:precision_mechanism")
    event.remove({ output: "powergrid:contactor" })
    zincMachine(event, Item.of("powergrid:contactor", 1), "powergrid:copper_coil")
    // add superior automated versions of some common recipes
    // coils: deploy stick on wire, 3:1 instead of 4:1 (Create 1.21 deploying: max 2 ingredients, use count in JSON)
    let copperWire = firstExistingItem(["immersiveengineering:wire_copper", "simpleradio:copper_wire"])
    let ironWire = firstExistingItem(["immersiveengineering:wire_iron"])
    let steelWire = firstExistingItem(["immersiveengineering:wire_steel"])
    if (copperWire) {
        createDeployingCount(event, "powergrid:copper_coil", copperWire, 3, "minecraft:stick")
        createDeployingCount(event, { id: "powergrid:insulated_copper_wire", count: 2 }, copperWire, 2, "kubejs:rubber")
    }
    if (ironWire) {
        createDeployingCount(event, "powergrid:resistive_coil", ironWire, 3, "minecraft:stick")
    }
    // connectors: filling metal on substrate, 10:1 instead of 30:1
    event.recipes.create.filling("powergrid:wire_connector", ["create:andesite_alloy", Fluid.of("blazinghot:molten_copper", 10)])
    event.recipes.create.filling("powergrid:heavy_wire_connector", ["minecraft:terracotta", Fluid.of("blazinghot:molten_iron", 10)])
    // insulated wire: kubejs rubber
    event.replaceInput({ output: /^powergrid:.*copper.*/ }, "minecraft:dried_kelp", "kubejs:rubber")
    if (Item.exists("powergrid:insulated_copper_wire")) {
        createDeployingCount(event, { id: "powergrid:copper_cord", count: 2 }, "powergrid:insulated_copper_wire", 4, "kubejs:rubber")
    }

    // wires
    // unify wire recipes by removing sawing versions
    event.remove({ type: "create:sawing", output: /.*_wire$/ })
    event.remove({ type: "create:sawing", output: /^immersiveengineering:wire_.*/ })
    // add createdieselgenerators cutting recipe for IE lead wire
    if (Platform.isLoaded("createdieselgenerators") && Item.exists("immersiveengineering:wire_lead")) {
        event.custom({
            "type": "createdieselgenerators:wire_cutting",
            "ingredients": [{
                "tag": "c:plates/lead"
            }],
            "results": [{
                "id": "immersiveengineering:wire_lead"
            }]
        })
    }
    // unify simpleradios copper wire
    event.remove({ id: "simpleradio:copper_wire" })
    // creating projectred red alloy wire with wiremaking machines (1:8)
    if (Item.exists("projectred_core:red_ingot") && Item.exists("projectred_transmission:red_alloy_wire")) {
        event.custom({
            "type":"createaddition:rolling",
            "input": {
                "item": "projectred_core:red_ingot"
            },
            "result": {
                "item": "projectred_transmission:red_alloy_wire",
                "count": 8
            }
        })
        event.custom({
            "type": "immersiveengineering:metal_press",
            "energy": 2400,
            "input": {
                "item": "projectred_core:red_ingot"
            },
            "mold": "immersiveengineering:mold_wire",
            "result": {
                "base_ingredient": {
                    "item": "projectred_transmission:red_alloy_wire"
                },
                "count": 8
            }
        })
    }
    if (Item.exists("projectred_transmission:red_alloy_wire") && Item.exists("projectred_transmission:white_insulated_wire")) {
    // recipe to insulate already-made red alloy wire
        event.recipes.create.deploying("projectred_transmission:white_insulated_wire", ["projectred_transmission:red_alloy_wire", "minecraft:white_wool"])
    }
    // automating createaddition barbed wire and IE razor wire
    if (ironWire) {
        createDeployingCount(event, "createaddition:barbed_wire", ironWire, 2, ironWire)
    }
    if (steelWire && Item.exists("immersiveengineering:treated_fence")) {
        createDeployingCount(event, "immersiveengineering:razor_wire", "immersiveengineering:treated_fence", 1, steelWire)
    }


    // //chisel integration
    // remove crafting recipes for chisel stones that are now chiseling only
    event.remove({ type: "minecraft:crafting_shaped", output: "chisel:marble/raw" })
    event.remove({ type: "minecraft:crafting_shaped", output: "chisel:limestone/raw" })
    event.remove({ type: "minecraft:crafting_shaped", output: "chisel:marblepillar/pillar" })
    // new marble pillar recipe
    event.shaped(Item.of("chisel:marblepillar/pillar", 6), [
        "MM",
        "MM",
        "MM"
    ], {
        M: "#forge:stone/marble"
    })
    // delete diabase (pointless clone of basalt)
    event.remove({ output: /chisel:diabase.*/ })
    event.remove({ input: /chisel:diabase.*/ })
})
