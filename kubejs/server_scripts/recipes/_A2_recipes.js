ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")
    const skipRecipe = { id() { return this } }
    const custom = (json) => {
        let type = json && json.type
        if (typeof type === "string") {
            let ns = type.split(":")[0]
            // Old type ids (tconstruct, embers, nuclearcraft) whose mod is not loaded.
            if (ns !== "minecraft" && ns !== "kubejs" && !Platform.isLoaded(ns)) return skipRecipe
        }
        return event.custom(json)
    }
    // Create 6 treats "3x #tag" and builder tag ingredients as sized fluids.
    let deployIngredient = (spec) => {
        let body = spec
        if (spec && spec.id) return { item: spec.id }
        if (typeof spec !== "string") return spec
        let sized = spec.match(/^(\d+)x (.+)$/)
        if (sized) body = sized[2]
        // Pack-defined forge:chisels holds chisel:iron_chisel, diamond_chisel, and hitech_chisel.
        // Other forge: item tags in this pack are empty; the items live under c:.
        if (body.startsWith("#forge:") && !body.startsWith("#forge:chisels")) {
            let name = body.slice("#forge:".length)
            if (name === "slimeballs") name = "slime_balls"
            else if (name === "dough") name = "foods/dough"
            else if (name === "glass") name = "glass_blocks"
            else if (name === "glazed_terracotta") name = "glazed_terracottas"
            else if (name.startsWith("sand/")) name = "sands/" + name.slice("sand/".length)
            body = "#c:" + name
        }
        let base = body.startsWith("#") ? { tag: body.slice(1) } : { item: body }
        // Create 6 processing recipes store plain ingredients, not stack counts.
        return base
    }
    let deploy = (result, inputs) => {
        let id = result
        let count = 1
        if (result && result.id) {
            id = result.id
            count = result.count || 1
        } else if (typeof result === "string") {
            let sized = result.match(/^(\d+)x (.+)$/)
            if (sized) {
                count = parseInt(sized[1], 10)
                id = sized[2]
            }
        }
        if (typeof id === "string" && !Item.exists(id)) return skipRecipe
        let needsSaw = inputs.some(spec => typeof spec === "string" && spec.indexOf("kubejs:saws") >= 0)
        if (needsSaw) {
            let saw = ["cb_microblock:stone_saw", "cb_microblock:iron_saw", "cb_microblock:diamond_saw", "projectred_exploration:gold_saw", "projectred_exploration:ruby_saw", "projectred_exploration:sapphire_saw", "projectred_exploration:peridot_saw"].some(item => Item.exists(item))
            if (!saw) return skipRecipe
        }
        return event.custom({
            type: "create:deploying",
            ingredients: inputs.map(deployIngredient),
            results: [{ id: id, count: count }]
        })
    }

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
    if (Platform.isLoaded("vs_clockwork")) event.replaceInput({ output: "vs_clockwork:wing" }, "create:sturdy_sheet", "create:golden_sheet")
    event.replaceInput({ output: "railways:fuel_tank" }, "create:sturdy_sheet", "create:golden_sheet")
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
    // gold: airships, quadrocopter, VS clockwork
    if (Platform.isLoaded("vs_clockwork")) {
        event.remove({ output: "vs_clockwork:physics_infuser" })
        goldMachine(event, Item.of("vs_clockwork:physics_infuser", 1), "vs_clockwork:wanderlite_matrix")
    }
    event.replaceInput({ output: "immersive_aircraft:quadrocopter" }, "immersive_aircraft:boiler", "kubejs:gold_machine")
    if (Item.exists("vs_sails:rope")) {
        event.remove({ output: "immersive_aircraft:airship" })
        event.shaped("immersive_aircraft:airship", [
            "SSS",
            "R R",
            "HGP"
        ], {
            S: "immersive_aircraft:sail",
            R: "vs_sails:rope",
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
    if (hasThermal) event.remove({ type: "minecraft:smelting", output: "thermal:white_rockwool" })
    event.remove({ type: "create:fan_blasting", output: "immersiveengineering:slag_glass" })

    // conflict with thermal:slag_block: slag brick is now stonecutting only
    event.remove({ type: "minecraft:crafting_shaped", output: "immersiveengineering:slag_brick" })

    // get all our slags in one place
    if (hasThermal) {
        event.replaceInput({}, "thermal:slag", "#c:slag")
        event.replaceOutput({}, "immersiveengineering:slag", "thermal:slag")// IE might still give IE slag
    }
    // event.replaceInput({}, "tfmg:slag", "#c:slag")
    event.replaceInput({}, "immersiveengineering:slag", "#c:slag")
    // event.replaceOutput({}, "thermal:slag", "tfmg:slag")
    // isn't almostunified supposed to do this?

    event.remove({ output: "immersiveengineering:sawdust" })// sawdust floor conflicts with JAOPCA storage block
    event.shaped("immersiveengineering:sawdust", [
        "   ",
        "   ",
        "SSS"
    ], {
        S: "#c:dusts/wood"
    })

    // unify sawdusts
    event.remove({ output: "jaopca:storage_blocks.wood" })
    event.remove({ input: "jaopca:storage_blocks.wood" })
    event.replaceInput({}, "immersiveengineering:dust_wood", "#c:dusts/wood")
    if (Item.exists("nuclearcraft:sawdust")) event.replaceInput({}, "nuclearcraft:sawdust", "#c:dusts/wood")
    if (hasThermal) {
        event.replaceInput({}, "thermal:sawdust", "#c:dusts/wood")
        event.replaceOutput({}, "immersiveengineering:dust_wood", "thermal:sawdust")// IE might still give IE sawdust
        if (Item.exists("nuclearcraft:sawdust")) event.replaceOutput({}, "nuclearcraft:sawdust", "thermal:sawdust")
    }

    // remove vanilla blast furnace -> steel (lol?)
    event.remove({ type: "minecraft:blasting", input: "minecraft:iron_ingot" })// done by input in case of steel unification changes
    // chainmail no longer meltable (it gave steel and chainmail can be crafted)
    event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_helmet" })
    event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_chestplate" })
    event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_leggings" })
    event.remove({ type: "tconstruct:damagable_melting", input: "minecraft:chainmail_boots" })


    // Create Addition: disable rotational force/energy conversion because powergrid does it better
    event.remove({ output: "createaddition:electric_motor" })
    event.remove({ input: "createaddition:electric_motor" })
    event.remove({ output: "createaddition:alternator" })

    // nuclearcraft: charcoal dust was useless? Those ids are not in nuclearcraftneohaul.
    if (Item.exists("nuclearcraft:charcoal_dust") && Item.exists("nuclearcraft:coal_dust")) {
        event.replaceOutput({}, "nuclearcraft:charcoal_dust", "nuclearcraft:coal_dust")
    }

    // //using thermal:device_fisher in place of the water strainer mod
    // use canvas in slot to get sand and/or clay
    if (hasThermal) {
        event.recipes.thermal.fisher_boost('farmersdelight:canvas', 2, 0.02, 'autisnautics2datapack:gameplay/straining')
    }
    // add a few more sources of straw for canvas
    let knifeCut = (input, results) => {
        if (!Item.exists(input)) return
        custom({
            type: "farmersdelight:cutting",
            ingredients: [{ item: input }],
            result: results,
            tool: { tag: "c:tools/knife" }
        })
    }
    knifeCut("minecraft:wheat", [
        { item: { id: "farmersdelight:straw", count: 1 } },
        { item: { id: "minecraft:wheat_seeds", count: 1 }, chance: 0.25 }
    ])
    knifeCut("supplementaries:flax", [
        { item: { id: "farmersdelight:straw", count: 1 } },
        { item: { id: "minecraft:string", count: 1 } }
    ])
    // modify aquatic entangler recipe so it's easier to get
    if (hasThermal) {
        event.replaceInput({ output: "thermal:device_fisher" }, "thermal:redstone_servo", "minecraft:barrel")
        event.replaceInput({ output: "thermal:junk_net" }, "minecraft:iron_nugget", "#c:ingots/lead")
        if (Item.exists("rats:garbage_pile")) event.replaceInput({ output: "thermal:junk_net" }, "minecraft:stick", "rats:garbage_pile")
    }

    // tfmg/ie synthetic leathers and strings support
    event.replaceInput({}, "minecraft:leather", "#c:leathers")
    // event.replaceInput({}, "minecraft:string", "#c:string")
    // fix synthetic leather recipe collision with immersiveengineering:plate_duroplast
    // event.remove({ output: "tfmg:synthetic_leather" })
    // event.recipes.create.deploying("tfmg:synthetic_leather", ["#c:ingots/plastic", "minecraft:paper"])

    // make tome of alkahestry an endgame item
    event.remove({ type: "minecraft:crafting_shapeless", input: "reliquary:witch_hat", output: "reliquary:alkahestry_tome" })
    if (Item.exists("embers:alchemy_pedestal") && Item.exists("botania:conjuration_catalyst") && Item.exists("dungeonnowloading:chaotic_hexahedron")) {
        event.recipes.create.mechanical_crafting("reliquary:alkahestry_tome", [
            " HTE ",
            "  A  ",
            "SXADW",
            "PGMRP",
            "OOCOO"
        ], {
            T: "enigmaticlegacyplus:void_tome",
            E: "reliquary:eye_of_the_storm",
            G: "kubejs:accelerator_glowstone",
            R: "kubejs:accelerator_redstone",
            M: "kubejs:missingno",
            C: "botania:conjuration_catalyst",
            H: "dungeonnowloading:chaotic_hexahedron",
            O: "minecraft:crying_obsidian",
            P: "embers:alchemy_pedestal",
            X: "enigmaticlegacyplus:execution_axe",
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

    // another step of sawing turns a wooden slab into 8 plates
    event.custom({
        type: "create:cutting",
        ingredients: [{ tag: "minecraft:wooden_slabs" }],
        results: [{ id: "cuisinedelight:plate", count: 8 }],
        processing_time: 150
    }).id("kubejs:cutting/wooden_slab_to_plates")
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
    deploy("createbigcannons:very_large_cast_mould", ["#minecraft:logs", "#kubejs:saws"])
    // smaller moulds are made by sawing the very large mould down smaller and smaller
    deploy("createbigcannons:large_cast_mould", ["createbigcannons:very_large_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:medium_cast_mould", ["createbigcannons:large_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:small_cast_mould", ["createbigcannons:medium_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:very_small_cast_mould", ["createbigcannons:small_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:autocannon_breech_cast_mould", ["createbigcannons:very_small_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:autocannon_recoil_spring_cast_mould", ["createbigcannons:autocannon_breech_cast_mould", "#kubejs:saws"])
    deploy("createbigcannons:autocannon_barrel_cast_mould", ["createbigcannons:autocannon_recoil_spring_cast_mould", "#kubejs:saws"])
    // the other moulds branch off from the generic ones by chiseling or sawing
    deploy("createbigcannons:sliding_breech_cast_mould", ["createbigcannons:large_cast_mould", "#forge:chisels"])
    deploy("createbigcannons:cannon_end_cast_mould", ["createbigcannons:medium_cast_mould", "#forge:chisels"])
    deploy("createbigcannons:screw_breech_cast_mould", ["createbigcannons:cannon_end_cast_mould", "#kubejs:saws"])

    // might as well fix this createbigcannons shit while i'm at it
    event.remove({ id: "createbigcannons/cutting/autocannon_cartridge_sheet_copper" })// duplicate
    event.replaceInput({ output: "createbigcannons:spring_wire" }, "#c:plates/iron", "#c:plates/lead")// another ingredient+type with multiple outputs

    // //Chapter 2B: Lead Machines replacement
    // replacing the chassis with the lead machine
    // Installed mod is nuclearcraftneohaul. Old nuclearcraft: item ids are gone.
    if (Item.exists("nuclearcraft:chassis")) {
        event.remove({ output: "nuclearcraft:chassis" })
        event.replaceInput({ output: "nuclearcraft:turbine_casing" }, "nuclearcraft:chassis", "kubejs:lead_casing")
        event.replaceInput({}, "nuclearcraft:chassis", "kubejs:lead_machine")
    }
    if (Item.exists("nuclearcraftneohaul:manufactory")) {
        event.replaceInput({ output: "nuclearcraftneohaul:manufactory" }, "minecraft:piston", "kubejs:lead_machine")
    }
    if (Item.exists("nuclearcraft:alloy_smelter")) {
        event.replaceInput({ output: "nuclearcraft:alloy_smelter" }, "minecraft:blast_furnace", "kubejs:lead_machine")
    }

    // //atomic mechanisms
    // Logistic Mechanism and kubejs pulp recipes removed in chapters.js
    // Filling steps use Tinkers molten metals. Skip when that mod is absent.
    if (Platform.isLoaded("tconstruct")) {
        custom({
            "type": "create:sequenced_assembly",
            "ingredient": { "item": "create:precision_mechanism" },
            "results": [
                { "item": "kubejs:logistic_mechanism" }
            ],
            "loops": 1,
            "sequence": [
                {
                    "type": "create:filling",
                    "ingredients": [
                        { "item": "kubejs:incomplete_logistic_mechanism" },
                        { "fluid": "tconstruct:molten_steel", "amount": 90 }// can't use tags for fluids
                    ],
                    "results": [
                        { "item": "kubejs:incomplete_logistic_mechanism" }
                    ]
                },
                {
                    "type": "create:filling",
                    "ingredients": [
                        { "item": "kubejs:incomplete_logistic_mechanism" },
                        { "fluid": "tconstruct:molten_uranium", "amount": 10 }
                    ],
                    "results": [
                        { "item": "kubejs:incomplete_logistic_mechanism" }
                    ]
                },
                {
                    "type": "create:filling",
                    "ingredients": [
                        { "item": "kubejs:incomplete_logistic_mechanism" },
                        { "fluid": "tconstruct:molten_lead", "amount": 90 }
                    ],
                    "results": [
                        { "item": "kubejs:incomplete_logistic_mechanism" }
                    ]
                },
                {
                    "type": "create:filling",
                    "ingredients": [
                        { "item": "kubejs:incomplete_logistic_mechanism" },
                        { "fluid": "nuclearcraftneohaul:radaway", "amount": 250 }
                    ],
                    "results": [
                        { "item": "kubejs:incomplete_logistic_mechanism" }
                    ]
                }
            ],
            "transitionalItem": { "item": "kubejs:incomplete_logistic_mechanism" }
        }).id("kubejs:logistic_mechanism")
    }
    // Radaway: 3x glowing mushroom and 1 bottle of milk, heated mixing
    event.recipes.create.mixing([Fluid.of("nuclearcraftneohaul:radaway", 250)], [Item.of("nuclearcraftneohaul:glowing_mushroom", 3), Fluid.of("minecraft:milk", 250)]).heated()
    // original NC recipe: replace Ethanol with Milk
    event.remove({ output: Fluid.of("nuclearcraftneohaul:radaway") })
    event.remove({ output: Fluid.of("nuclearcraftneohaul:radaway_slow") })
    // remove NC ethanol while we're at it, other mods do it better
    event.remove({ output: Fluid.of("nuclearcraftneohaul:ethanol") })
    event.remove({ output: Fluid.of("nuclearcraftneohaul:redstone_ethanol") })
    event.remove({ output: "nuclearcraftneohaul:ethanol_bucket" })
    event.remove({ output: "nuclearcraftneohaul:redstone_ethanol_bucket" })

    custom({
        "type": "nuclearcraft:fluid_enricher",
        "input": [
            {
                "count": 3,
                "item": "nuclearcraft:glowing_mushroom"
            }
        ],
        "inputFluids": [
            {
                "amount": 250,
                "tag": "kubejs:milk"
            }
        ],
        "outputFluids": [
            {
                "amount": 250,
                "fluid": "nuclearcraft:radaway"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })
    custom({
        "type": "nuclearcraft:fluid_enricher",
        "input": [
            {
                "count": 1,
                "item": "minecraft:redstone"
            }
        ],
        "inputFluids": [
            {
                "amount": 250,
                "tag": "c:radaway"
            }
        ],
        "outputFluids": [
            {
                "amount": 250,
                "fluid": "nuclearcraft:radaway_slow"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })
    // synthetic glowing mushroom recipe: 50mb of Potion of Glowing on a brown mushroom
    if (Item.exists("nuclearcraftneohaul:glowing_mushroom")) {
        event.recipes.create.filling("nuclearcraftneohaul:glowing_mushroom", ["minecraft:brown_mushroom", Fluid.of('create:potion[create:potion_fluid_bottle_type="regular",minecraft:potion_contents={potion:"alexscaves:glowing"}]', 50)])
    }
    // potion of glowing recipe in startup_scripts


    // //just one word: PLASTICS
    // tfmg-less edition: end product is now nuclearcraft:bioplastic

    // Rats is not installed. These ids throw if read.
    if (Platform.isLoaded("rats")) {
        event.remove({ output: "rats:raw_plastic" })
        event.remove({ input: "rats:plastic_waste" })
        event.replaceInput({}, "rats:raw_plastic", "#c:ingots/plastic")

        // converting plastic waste to liquid plastic by "recycling", now with kubejs:liquid_plastic
        event.recipes.create.mixing([Fluid.of("kubejs:liquid_plastic", 10), "quark:dirty_shard"], [Item.of("rats:plastic_waste", 9), Fluid.of("minecraft:water", 1000)]).heated()
        // glass shard gives em an extra step to process plus it's a glass source I guess
        // alt liquid_plastic recipe: nuclearcraft melter, 1 plastic waste -> 10mb liquid plastic (9x more efficient and no waste product)
        custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "count": 1,
                    "item": "rats:plastic_waste"
                }
            ],
            "outputFluids": [
                {
                    "amount": 10,
                    "tag": "kubejs:liquid_plastic"
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
        // melting bioplastic to liquid for whatever reason
        custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "count": 1,
                    "tag": "c:ingots/plastic"
                }
            ],
            "outputFluids": [
                {
                    "amount": 90,
                    "fluid": "kubejs:liquid_plastic"
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
        // converting raw_plastic (loot only) to liquid_plastic
        event.recipes.create.mixing([Fluid.of("kubejs:liquid_plastic", 90)], [Item.of("rats:raw_plastic", 1), Fluid.of("minecraft:water", 250)]).heated()
        custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "count": 1,
                    "item": "rats:raw_plastic"
                }
            ],
            "outputFluids": [
                {
                    "amount": 90,
                    "fluid": "kubejs:liquid_plastic"
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
    }

    // forming bioplastic from liquid plastic
    // tconstruct casting table, plate cast
    custom({
        "type": "tconstruct:casting_table",
        "cast": { "tag": "tconstruct:casts/multi_use/plate" },
        "cast_consumed": false,
        "fluid": { "tag": "c:liquid_plastic", "amount": 90 },// one ingot
        "result": { "item": "nuclearcraft:bioplastic" },
        "cooling_time": 100
    })
    custom({
        "type": "tconstruct:casting_table",
        "cast": { "tag": "tconstruct:casts/single_use/plate" },
        "cast_consumed": true,
        "fluid": { "tag": "c:liquid_plastic", "amount": 90 },
        "result": { "item": "nuclearcraft:bioplastic" },
        "cooling_time": 100
    })
    // NC ingot former
    custom({
        "type": "nuclearcraft:ingot_former",
        "inputFluids": [
            {
                "amount": 90,
                "tag": "c:liquid_plastic"
            }
        ],
        "output": [
            {
                "item": "nuclearcraft:bioplastic"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })

    // replace sugarcane for bioplastic with biomass
    event.remove({ output: "nuclearcraftneohaul:bioplastic" })
    custom({
        "type": "nuclearcraft:manufactory",
        "input": [
            {
                "count": 2,
                "item": "createaddition:biomass"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "nuclearcraft:bioplastic"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })


    // fix broken radaway item recipes
    event.remove({ output: Item.of("nuclearcraftneohaul:radaway") })
    event.remove({ output: Item.of("nuclearcraftneohaul:radaway_slow") })
    let fillRadaway = (result, fluid) => {
        if (!Item.exists(result) || !Item.exists("nuclearcraftneohaul:bioplastic")) return
        event.custom({
            type: "create:filling",
            ingredients: [
                { item: "nuclearcraftneohaul:bioplastic" },
                { type: "neoforge:single", fluid: fluid, amount: 250 }
            ],
            results: [{ id: result, count: 1 }]
        })
    }
    fillRadaway("nuclearcraftneohaul:radaway", "nuclearcraftneohaul:radaway")
    fillRadaway("nuclearcraftneohaul:radaway_slow", "nuclearcraftneohaul:radaway_slow")
    custom({
        "type": "nuclearcraft:fluid_infuser",
        "inputFluids": [
            {
                "amount": 250,
                "tag": "c:radaway"
            }
        ],
        "input": [
            {
                "count": 1,
                "tag": "c:ingots/plastic"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "nuclearcraft:radaway"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })
    custom({
        "type": "nuclearcraft:fluid_infuser",
        "inputFluids": [
            {
                "amount": 250,
                "tag": "c:radaway_slow"
            }
        ],
        "input": [
            {
                "count": 1,
                "tag": "c:ingots/plastic"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "nuclearcraft:radaway_slow"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })

    // remove redundant "rose quartz" for polished rose quartz
    event.replaceOutput({}, "create:rose_quartz", "create:polished_rose_quartz")// jaopca only
    // event.replaceInput({}, "create:rose_quartz", "create:polished_rose_quartz")// jaopca, cosmetic stonecutting block, rock candy

    // remove bugged melting recipe for "raw tungsten" which doesn't exist
    event.remove({ id: "tconstruct:smeltery/melting/metal/tungsten/raw" })
    event.remove({ id: "tconstruct:smeltery/melting/metal/tungsten/raw_block" })

    // tfmg meme stones
    // event.remove({ input: /.*tfmg.*galena.*/ })
    // event.remove({ input: /.*tfmg.*bauxite.*/ })
    // event.remove({ input: "tfmg:lignite" })
    // event.remove({ output: /.*tfmg.*galena.*/ })
    // event.remove({ output: /.*tfmg.*bauxite.*/ })
    // event.remove({ output: "tfmg:lignite" })

    // alexscaves limestone integration
    event.replaceInput({}, "alexscaves:limestone", "#c:stone/limestone")
    event.replaceInput({}, "create:limestone", "#c:stone/limestone")

    // .withChance() throws on 1.21. Drop outputs whose items are not installed.
    let chanceCrush = (input, time, pairs) => {
        let outs = []
        pairs.forEach(p => { if (Item.exists(p[0])) outs.push(CreateItem.of(p[0], p[1])) })
        if (outs.length) event.recipes.create.crushing(outs, input).processingTime(time)
    }
    chanceCrush("alexscaves:galena", 500, [
        ["thermal:lead_dust", 0.05],
        ["create:crushed_raw_lead", 0.01],
        ["thermal:raw_lead", 0.001]
    ])
    chanceCrush("alexscaves:scrap_metal", 1, [
        ["minecraft:iron_nugget", 0.5],
        ["create:copper_nugget", 0.03],
        ["create:zinc_nugget", 0.03],
        ["thermal:tin_nugget", 0.03],
        ["thermal:lead_nugget", 0.03],
        ["createbigcannons:cast_iron_nugget", 0.025],
        ["createdeco:industrial_iron_nugget", 0.025],
        ["thermal:nickel_nugget", 0.0125],
        ["thermal:bronze_nugget", 0.0125],
        ["nuclearcraft:lithium_nugget", 0.0125],
        ["scguns:anthralite_nugget", 0.0125],
        ["tconstruct:rose_gold_nugget", 0.004],
        ["tconstruct:pig_iron_nugget", 0.004],
        ["tconstruct:amethyst_bronze_nugget", 0.004],
        ["tconstruct:steel_nugget", 0.0025],
        ["createaddition:electrum_nugget", 0.001],
        ["thermal:silver_nugget", 0.0005],
        ["rats:plastic_waste", 0.0125],
        ["youkaishomecoming:can", 0.025],
        ["tconstruct:debris_nugget", 0.000001]
    ])

    // synthesize alexscaves neodymium using nuclearcraft neodymium dust which is otherwise useless
    custom({
        "type": "nuclearcraft:assembler",
        "input": [
            {
                "count": 1,
                "tag": "c:dusts/neodymium"
            },
            {
                "count": 1,
                "tag": "c:gems/cinnabar"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "alexscaves:raw_scarlet_neodymium"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })
    custom({
        "type": "nuclearcraft:assembler",
        "input": [
            {
                "count": 1,
                "tag": "c:dusts/neodymium"
            },
            {
                "count": 1,
                "tag": "c:gems/lapis"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "alexscaves:raw_azure_neodymium"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })

    // globes for the wasteland
    event.remove({ output: "supplementaries:globe" })
    event.remove({ output: "supplementaries:globe_sepia" })
    // sepia globe: globe in NC nuclear furnace
    custom({
        "type": "nuclearcraft:alloy_smelter",
        "input": [
            {
                "count": 1,
                "item": "supplementaries:globe"
            },
            {
                "count": 1,
                "item": "minecraft:clock"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "supplementaries:globe_sepia"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 30.0
    })
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
    event.remove({ input: "jaopca:storage_blocks.sodium_chloride" })
    event.remove({ output: "jaopca:storage_blocks.sodium_chloride" })

    // //createdeco
    // zinc sheets unification
    event.replaceInput({}, "createdeco:zinc_sheet", "#c:plates/zinc")
    // remove useless netherite sheet
    if (Item.exists("createdeco:netherite_sheet")) event.remove({ output: "createdeco:netherite_sheet" })
    // no counterfeiting
    event.remove({ input: /^createdeco:.*coin(stack)?$/ })
    event.remove({ output: /^createdeco:.*coin(stack)?$/ })

    // forcing the use of ae2 ender dust
    if (Platform.isLoaded("ae2")) event.replaceOutput({}, "thermal:ender_pearl_dust", "ae2:ender_dust")

    // adding electrotine alloy recipes (where did the original one go?)
    if (Item.exists("projectred_core:electrotine_dust") && Item.exists("projectred_core:electrotine_ingot")) {
        event.shapeless("projectred_core:electrotine_ingot", [Item.of("projectred_core:electrotine_dust", 8), "minecraft:iron_ingot"])
    }

    // //fluid unification
    // embers:molten_.*
    event.remove({ id: "embers:mixing/molten_invar" })// progression meme
    // the rest is in A2 datapack (kubejs incompatible)
    // nuclearcraft:molten_.*
    if (Platform.isLoaded("nuclearcraft") && Platform.isLoaded("tconstruct")) {
        let unifiedNCFluids = ["bronze", "cobalt", "electrum", "lead", "platinum", "silver", "tin", "uranium", "zinc"]
        unifiedNCFluids.forEach(fluid => {
            event.replaceOutput({ type: "nuclearcraft:melter" }, "nuclearcraft:molten_" + fluid, "tconstruct:molten_" + fluid)
        })
    }

    // remove jaopca "molten coal" for uselessness and possible dupe exploit
    event.remove({ id: /^jaopca:.*molten.*coal$/ })

    // fixing weird coal coke storage block recipes
    event.remove({ id: "thermal:storage/coal_coke_block" })
    event.remove({ id: "immersiveengineering:crafting/coal_coke_to_coke" })
    // event.remove({ id: "tfmg:crafting/coal_coke_block" })
    //
    // event.remove({ id: "tfmg:crafting/coal_coke_from_block" })
    event.remove({ id: "immersiveengineering:crafting/coke_to_coal_coke" })
    // event.remove({ output: "tfmg:coal_coke_block" })
    if (hasThermal) event.shapeless("thermal:coal_coke_block", ["9x #c:coal_coke"])

    // //creosote unification - have to use IE because it's hardcoded
    // tfmg hardened wood block = treated wood block + 125mb more creosote
    // event.remove({ id: "tfmg:filling/hardened_wood_creosote" })
    // event.recipes.create.filling("tfmg:hardened_planks", ["#c:treated_wood", Fluid.of("immersiveengineering:creosote", 125)])

    // //powergrid integration
    // replace conductive casing recipes with zinc machine
    // event.replaceInput({}, "powergrid:conductive_casing", "kubejs:zinc_machine")
    // it's now a cosmetic block so make it cheaper
    // event.remove({ output: "powergrid:conductive_casing" })
    // event.shapeless(Item.of("powergrid:conductive_casing", 8), ["#c:ingots/zinc", "create:andesite_casing"])
    // various junctions, connectors, etc. are now from stonecutting zinc machine 8:1
    if (Platform.isLoaded("powergrid")) {
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
        zincMachine(event, Item.of("powergrid:hv_switch", 1), "#c:rods/iron")
        event.remove({ output: "powergrid:hv_breaker" })
        zincMachine(event, Item.of("powergrid:hv_breaker", 1), "create:precision_mechanism")
        event.remove({ output: "powergrid:contactor" })
        zincMachine(event, Item.of("powergrid:contactor", 1), "powergrid:copper_coil")
        // add superior automated versions of some common recipes
        // Create 6 deployer recipes accept two unsized ingredients, so a 3-wire
        // deploy cannot be expressed. Mechanical crafting keeps the 3:1 ratio.
        let coil = (result, wireTag) => event.custom({
            type: "create:mechanical_crafting",
            pattern: ["WWW", " S "],
            key: {
                W: { tag: wireTag },
                S: { item: "minecraft:stick" }
            },
            result: { id: result, count: 1 },
            accept_mirrored: false
        })
        if (Item.exists("powergrid:copper_coil")) coil("powergrid:copper_coil", "c:wires/copper")
        if (Item.exists("powergrid:resistive_coil")) coil("powergrid:resistive_coil", "c:wires/iron")
        // connectors: filling metal on substrate, 10:1 instead of 30:1
        if (Platform.isLoaded("tconstruct")) {
            event.recipes.create.filling("powergrid:wire_connector", ["create:andesite_alloy", Fluid.of("tconstruct:molten_copper", 10)])
            event.recipes.create.filling("powergrid:heavy_wire_connector", ["minecraft:terracotta", Fluid.of("tconstruct:molten_iron", 10)])
        }
        // insulated wire: always uses rubber not kelp, can deploy for 2:1 rubber efficiency
        event.replaceInput({ output: /^powergrid:.*copper.*/ }, "minecraft:dried_kelp", "kubejs:rubber")
        if (Item.exists("powergrid:insulated_copper_wire") && Item.exists("kubejs:rubber")) {
            event.shapeless(Item.of("powergrid:insulated_copper_wire", 2), ["#c:wires/copper", "#c:wires/copper", "kubejs:rubber"])
                .id("kubejs:powergrid_insulated_copper_wire")
        }
        if (Item.exists("powergrid:copper_cord") && Item.exists("kubejs:rubber")) {
            event.shapeless(Item.of("powergrid:copper_cord", 2), [
                "powergrid:insulated_copper_wire", "powergrid:insulated_copper_wire",
                "powergrid:insulated_copper_wire", "powergrid:insulated_copper_wire",
                "kubejs:rubber"
            ]).id("kubejs:powergrid_copper_cord")
        }
    }

    // wires
    // unify wire recipes by removing sawing versions
    event.remove({ type: "create:sawing", output: /.*_wire$/ })
    event.remove({ type: "create:sawing", output: /^immersiveengineering:wire_.*/ })
    // add createdieselgenerators cutting recipe for IE lead wire
    custom({
        "type": "createdieselgenerators:wire_cutting",
        "ingredients": [{
            "tag": "c:plates/lead"
        }],
        "results": [{
            "id": "immersiveengineering:wire_lead"
        }]
    })
    // unify simpleradios copper wire
    event.remove({ id: "simpleradio:copper_wire" })
    // creating projectred red alloy wire with wiremaking machines (1:8)
    if (Item.exists("projectred_core:red_ingot") && Item.exists("projectred_transmission:red_alloy_wire")) {
        custom({
            "type":"createaddition:rolling",
            "input": {
                "item": "projectred_core:red_ingot"
            },
            "result": {
                "item": "projectred_transmission:red_alloy_wire",
                "count": 8
            }
        })
        custom({
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
        // recipe to insulate already-made red alloy wire
        event.recipes.create.deploying("projectred_transmission:white_insulated_wire", ["projectred_transmission:red_alloy_wire", "minecraft:white_wool"])
    }
    // automating createaddition barbed wire and IE razor wire
    if (Item.exists("createaddition:barbed_wire")) {
        event.shapeless("createaddition:barbed_wire", ["#c:wires/iron", "#c:wires/iron", "#c:wires/iron"])
            .id("kubejs:createaddition_barbed_wire")
    }
    deploy("immersiveengineering:razor_wire", ["immersiveengineering:treated_fence", "#c:wires/steel"])


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
        M: "#c:stone/marble"
    })
    // delete diabase (pointless clone of basalt)
    event.remove({ output: /chisel:diabase.*/ })
    event.remove({ input: /chisel:diabase.*/ })
})
