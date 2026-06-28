// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 2c
    let transitional

    // -- Obliterating biofuels and biofuel accessories
    // - Unifying #c:plantoil into immersiveengineering:plantoil
    event.remove({ id: "createaddition:compacting/seed_oil" })
    event.remove({ id: "createdieselgenerators:compacting/plant_oil" })
    event.recipes.create.compacting(Fluid.of('immersiveengineering:plantoil', 100), [Ingredient.of("#c:seeds")]) // inferior to IE but simpler
        .id("kubejs:compacting/plant_oil")
    // - Removing #c:ethanol as it's no longer required
    // createdieselgenerators
    event.remove({ id: "createdieselgenerators:basin_fermenting/fermentable" })
    event.remove({ id: "createdieselgenerators:bulk_fermenting/fermentable" })
    // immersiveengineering
    event.remove({ id: /immersiveengineering:fermenter\/.*/ })
    event.remove({ id: /farmersdelight:integration\/immersiveengineering\/fermenter.*/ })
    // remove industrial fermenter multiblock with datapack; only produces ethanol
    event.remove({ id: "immersiveengineering:refinery/acetaldehyde" }) // we have a petroleum alternative
    event.remove({ id: "immersiveengineering:mixer/herbicide" })
    event.custom({
        "type": "immersiveengineering:mixer",
        "energy": 3200,
        "fluid": { "amount": 500, "tag": "minecraft:water" },
        "inputs": [
            { "tag": "c:dusts/sulfur" },
            { "tag": "c:dusts/copper" }
        ],
        "result": { "amount": 500, "id": "immersiveengineering:herbicide" }
    })
    // unfortunate casualty
    event.remove({ id: "tmted:bottling/wodka" })
    // nuclearcraftneohaul:ethanol still exists as a chemistry ingredient (CO byproduct, radaway etc)
    // - createaddition
    // bioethanol
    event.remove({ id: "createaddition:mixing/bioethanol" })
    // biomass and pellets
    event.remove({ id: /createaddition:mixing\/biomass_from.*/ })
    event.remove({ output: "createaddition:biomass_pellet" })
    event.remove({ output: "createaddition:biomass_pellet_block" })
    // replace biomass usage in createaddition christmas lights
    event.replaceInput({ id: "createaddition:crafting/festive_spool" }, "createaddition:biomass", "minecraft:green_dye")
    // - createdieselgenerators:biodiesel
    event.remove({ id: "createdieselgenerators:mixing/biodiesel" })
    // - immersiveengineering
    event.remove({ id: "immersiveengineering:refinery/biodiesel" })
    event.remove({ id: "immersiveengineering:refinery/high_power_biodiesel" })
    event.remove({ id: "immersiveengineering:generator_fuel/biodiesel" })
    event.remove({ id: "immersiveengineering:generator_fuel/high_power_biodiesel" })

    // -- Removing other unwanted fuels
    // - Stellaris
    // Crude Oil
    event.remove({ id: "stellaris:misc/pumpjack" })
    event.remove({ id: "stellaris:misc/pumpjack_drill" })
    // Diesel and Fuel
    event.remove({ id: "stellaris:fuel_refining" })
    // now we no longer need this either
    event.remove({ id: "stellaris:fuel_refinery" })
    // - immersiveengineering
    // creosote_oil - todo make sure it's no longer a fuel for other mods
    event.remove({ id: "immersiveengineering:generator_fuel/creosote" })
    // - createaddition
    // remove default liquid_burning fuels
    event.remove({ id: /createaddition:liquid_burning\/.*/ })
    // add diesel and gasoline
    event.custom({
        "type": "createaddition:liquid_burning",
        "burn_time": 12000, // about the same rate as cdg burner on max
        "ingredients": [
            {
                "type": "fluid_tag",
                "amount": 1000,
                "fluid_tag": "c:gasoline"
            }
        ],
        "results": [],
        "superheated": true
    })
    event.custom({
        "type": "createaddition:liquid_burning",
        "burn_time": 12000,
        "ingredients": [
            {
                "type": "fluid_tag",
                "amount": 1000,
                "fluid_tag": "c:diesel"
            }
        ],
        "results": []
    })

    // -- Removing unwanted generators
    // - simulated: portable_engine is hardcoded to run on sticks
    event.remove({ id: "simulated:red_portable_engine" })
    // - Immersive Engineering
    // kinetic dynamo: we have create bro
    event.remove({ output: "immersiveengineering:dynamo"})
    event.remove({ output: "immersiveengineering:watermill"})
    event.remove({ output: "immersiveengineering:waterwheel_segment"})
    event.remove({ output: "immersiveengineering:windmill"})
    event.remove({ output: "immersiveengineering:windmill_sail"})
    // thermoelectric generator: free RF
    event.remove({ output: "immersiveengineering:thermoelectric_generator"})
    // portable generator: removed for now, charge a battery for small mobile RF
    event.remove({ output: "immersivepetroleum:gas_generator"})
    // - Stellaris: not needed for now
    event.remove({ id: /stellaris:misc\/.*generator/})
    // - Create Connected Kinetic Battery: use electricity if you want to store power
    event.remove({ id: "create_connected:kinetic_battery" })
    // NC decay generator is fine, requires rare mats and the output is pathetic

    // -- Superheated-tier Fuel Production
    // - Blaze Cake/Blaze Roll
    // no more easy automated lava without superheating, to encourage other solutions
    event.remove({ id: "createdieselgenerators:bulk_fermenting/lava" })
    // add NC melter recipe for later
    event.custom({
        "type": "nuclearcraftneohaul:melter_recipe",
        "fluidProducts": [{
            "amount": 50,
            "ingredient": {
                "fluid": "minecraft:lava"
            }
        }],
        "itemIngredients": [{
            "count": 1,
            "ingredient": {
                "tag": "c:cobblestones"
            }
        }],
        "powerModifier": 1.5,
        "radiation": 0.0,
        "timeModifier": 1.25
    })
    // - Coke Cake
    transitional = "kubejs:incomplete_coke_cake"
    event.recipes.create.sequenced_assembly([
        "kubejs:coke_cake",
    ], "create:blaze_cake_base", [
        event.recipes.create.deploying(transitional, [transitional, Ingredient.of("#c:coal_coke")]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("immersiveengineering:creosote", 250)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:coke_cake")

    event.recipes.create.sequenced_assembly([
        "kubejs:coke_cake",
    ], "create:blaze_cake_base", [
        event.recipes.create.deploying(transitional, [transitional, Ingredient.of("#neoforge:coal_petcoke")]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("immersiveengineering:creosote", 250)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:coke_cake_petcoke")
    // IE Furnace Fuel recipes will only accept Coke Cake now
    event.remove({ id: /immersiveengineering:blastfurnace\/fuel_.*/ })
    event.remove({ id: /immersivepetroleum:blastfurnace\/fuel_.*/ })
    event.custom({
        "type": "immersiveengineering:blast_furnace_fuel",
        "input": {
            "item": "kubejs:coke_cake"
        },
        "time": 19200 // same efficiency as superheated mixer @ 64 RPM
    }).id("kubejs:blastfurnace/fuel_coke_cake")
    // Coke Cake as Blaze Burner fuel (1600t/half of blaze cake)
    // added as datapack: create\data_maps\item\superheated_blaze_burner_fuels.json

    // -- Steel
    // - Remove unwanted recipes
    // magistuarmory
    if (Platform.isLoaded("magistuarmory")) {
        event.remove({ id: /magistuarmory:furnace\/steel.*/ })
        // uncaught duplicate recipe, shaped type
        event.remove({ id: "magistuarmory:steel_ingot_to_steel_nuggets" })
        /* almostunified(tm)
        //event.remove({ output: "magistuarmory:steel_nugget" })
        //event.remove({ id: "magistuarmory:steel_plate" })
        */
        // automated armor mat recipes
        event.custom({
            "type": "createdieselgenerators:compression_molding",
            "ingredients": [
                { "tag": "c:nuggets/steel" }
            ],
            "mold": "createdieselgenerators:chain",
            "results": [
                { "id": "magistuarmory:steel_ring", "count": 2 }
            ]
        })
        event.custom({
            "type": "createdieselgenerators:compression_molding",
            "ingredients": [
                { "item": "magistuarmory:steel_ring" },
                { "item": "magistuarmory:steel_ring" },
                { "item": "magistuarmory:steel_ring" }
            ],
            "mold": "createdieselgenerators:chain",
            "results": [
                { "id": "magistuarmory:steel_chain" }
            ]
        })
        event.recipes.create.pressing("magistuarmory:small_steel_plate", Ingredient.of("#c:nuggets/steel"))
        event.recipes.create.cutting(Item.of("magistuarmory:small_steel_plate", 12), Ingredient.of("#c:plates/steel")).processingTime(300)
        event.custom({ // automated with slice&dice
            "type": "farmersdelight:cutting",
            "ingredients": [
                {
                    "tag": "c:leathers"
                }
            ],
            "result": [
                {
                    "item": {
                        "count": 3,
                        "id": "magistuarmory:leather_strip"
                    }
                }
            ],
            "tool": {
                "tag": "c:tools/knife"
            }
        })
    }
    // createbigcannons
    if (Platform.isLoaded("createbigcannons")) {
        event.remove({ id: "createbigcannons:mixing/alloy_steel" })
        // replace NC molten steel with createbigcannons
        event.remove({ id: /nuclearcraftneohaul:melter\/molten_steel.*/ })
        let ncMelter = (ingredientTag, amount) => event.custom({
            "type": "nuclearcraftneohaul:melter_recipe",
            "fluidProducts": [
                {
                    "amount": amount,
                    "ingredient": {
                        "fluid": "createbigcannons:molten_steel"
                    }
                }
            ],
            "itemIngredients": [
                {
                    "count": 1,
                    "ingredient": {
                        "tag": ingredientTag
                    }
                }
            ],
            "powerModifier": 1.0,
            "radiation": 0.0,
            "timeModifier": Number.parseFloat((amount / 90).toFixed(16))
        })
        ncMelter("c:ingots/steel", 90)
        ncMelter("c:dusts/steel", 90)
        ncMelter("c:nuggets/steel", 10)
        ncMelter("c:storage_blocks/steel", 810)
        // ferroboron usage in chapter_3a
    }
    // Stellaris: """"steel ore""""
    let steelOres = ["stellaris:raw_steel_ingot", "stellaris:steel_ore", "stellaris:deepslate_steel_ore", "stellaris:raw_steel_block", "stellaris:moon_steel_ore",
        Ingredient.of("#c:ores/steel"), Ingredient.of("#c:raw_materials/steel"), Ingredient.of("#c:storage_blocks/raw_steel")]
    steelOres.forEach(i => {
        event.remove({ input: i })
        event.remove({ output: i })
    })
    // todo: completely obliterate these from existence
    event.remove({id: /stellaris:misc\/steel_ingot_from.*/})
    event.remove({id: /jaopca:immersiveengineering.*steel/})

    // - New recipes
    // Superheated Mixing - produces createbigcannons:molten_steel
    event.recipes.create.mixing(Fluid.of("createbigcannons:molten_steel", 90), [Ingredient.of("#c:ingots/iron"), "minecraft:coal"]).superheated()
    event.recipes.create.mixing(Fluid.of("createbigcannons:molten_steel", 10),
        [Fluid.sizedIngredientOf("blazinghot:molten_iron", 10),
            Fluid.sizedIngredientOf("nuclearcraftneohaul:coal", 10)]).superheated()
    // Nuclearcraft recipes: fully electrified
    // Alloy Furnace
    event.custom({
        "type": "nuclearcraftneohaul:alloy_furnace_recipe",
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": [
                    {
                        "tag": "c:dusts/iron"
                    },
                    {
                        "tag": "c:ingots/iron"
                    }
                ]
            },
            {
                "count": 2,
                "ingredient": [
                    {
                        "tag": "c:dusts/coal"
                    },
                    {
                        "tag": "c:dusts/graphite"
                    },
                    {
                        "tag": "c:ingots/graphite"
                    },
                    {
                        "item": "minecraft:coal"
                    }
                ]
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "immersiveengineering:ingot_steel"
                }
            }
        ],
        "powerModifier": 10.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    })
    // Fluid Mixer
    event.custom({
        "type": "nuclearcraftneohaul:salt_mixer_recipe",
        "fluidIngredients": [
            {
                "amount": 90,
                "ingredient": {
                    "fluid": "blazinghot:molten_iron"
                }
            },
            {
                "amount": 90,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:coal"
                }
            }
        ],
        "fluidProducts": [
            {
                "amount": 90,
                "ingredient": {
                    "fluid": "createbigcannons:molten_steel"
                }
            }
        ],
        "powerModifier": 10.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    })

    // -- Mechanism Lubrication: Plant Oil or petroleum Lubricant
    // - Seed Oil/Plant Oil: unified above
    // - Replaced Create Diesel Generators Refining recipes to add Lubricant byproduct in datapack (fluid tag problems)
    // event.remove({ id: /createdieselgenerators:distillation.*/ })

    // -- Refined Mechanisms
    transitional = "kubejs:incomplete_refined_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:refined_mechanism",
    ], "create:precision_mechanism", [
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("createbigcannons:molten_steel", 90)]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("createbigcannons:molten_steel", 90)]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("immersivepetroleum:lubricant", 10)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:refined_mechanism")

    event.recipes.create.sequenced_assembly([
        "kubejs:refined_mechanism",
    ], "create:precision_mechanism", [
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("createbigcannons:molten_steel", 90)]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("createbigcannons:molten_steel", 90)]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("immersiveengineering:plantoil", 100)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:refined_mechanism_vegan")

    // -- Steel Casing (blazinghot:sturdy_casing retexture?)
    event.shapeless(Item.of("blazinghot:sturdy_casing", 2), [Ingredient.of("#c:plates/steel"), "minecraft:deepslate"])

    // -- Steel Machine
    donutCraft(event, "kubejs:steel_machine", "blazinghot:sturdy_casing", "kubejs:refined_mechanism")
    // - Usages
    // Create Diesel Generators
    event.remove({id: "createdieselgenerators:crafting/engine_piston" }) // deprecated
    event.remove({id: "createdieselgenerators:crafting/engine_turbocharger" }) // removed
    steelMachine(event, Item.of("createdieselgenerators:distillation_controller", 4))
    steelMachine(event, Item.of("createdieselgenerators:oil_scanner", 1))
    steelMachine(event, Item.of("createdieselgenerators:pumpjack_hole", 1), "create:copper_casing")
    steelMachine(event, Item.of("createdieselgenerators:pumpjack_bearing", 1), "create:mechanical_bearing")
    steelMachine(event, Item.of("createdieselgenerators:pumpjack_crank", 1), "create:shaft")
    steelMachine(event, Item.of("createdieselgenerators:pumpjack_head", 1), "kubejs:rubber")
    steelMachine(event, Item.of("createdieselgenerators:burner", 1), "create:empty_blaze_burner")
    steelMachine(event, Item.of("createdieselgenerators:diesel_engine", 1), "create:brass_funnel")
    steelMachine(event, Item.of("createdieselgenerators:large_diesel_engine", 1), "create:brass_tunnel")
    steelMachine(event, Item.of("createdieselgenerators:huge_diesel_engine", 1), "create:steam_engine")
    // Immersive Engineering/Petroleum
    steelMachine(event, Item.of("immersiveengineering:light_engineering", 2))
    steelMachine(event, Item.of("immersiveengineering:rs_engineering", 1), "minecraft:redstone")
    steelMachine(event, Item.of("immersiveengineering:heavy_engineering", 1))
    steelMachine(event, Item.of("immersiveengineering:resonanz_engineering", 1), "minecraft:echo_shard")
    steelMachine(event, Item.of("immersiveengineering:generator", 1), "immersiveengineering:coil_mv")
    steelMachine(event, Item.of("immersiveengineering:radiator", 1), Ingredient.of("#c:plates/constantan"))

    // -- Plastics
    // - Plastic Production
    // Making Nuclearcraft Phenol by heated mixing CDG gasoline with a catalyst
    event.recipes.create.mixing(Fluid.of("nuclearcraftneohaul:phenol", 250), [Fluid.sizedIngredientOf("createdieselgenerators:gasoline", 1000), Ingredient.of("#c:dusts/aluminum")]).heated()
    // Using nc phenol to make IE plastic, unifying IE and NC plastic
    event.remove({ id: "nuclearcraftneohaul:manufactory/bioplastic" })
    event.replaceInput({}, "nuclearcraftneohaul:bioplastic", Ingredient.of("#c:plates/plastic"))
    // replace fluid infuser radaway recipes
    event.remove({ id: /nuclearcraftneohaul:fluid_infuser\/radawa.*/ })
    event.custom({
        "type": "nuclearcraftneohaul:infuser_recipe",
        "fluidIngredients": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:radaway"
                }
            }
        ],
        "itemIngredients": [
            {
                "count": 2,
                "ingredient": {
                    "tag": "c:plates/plastic"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "nuclearcraftneohaul:radaway"
                }
            }
        ],
        "powerModifier": 0.5,
        "radiation": 0.0,
        "timeModifier": 1.0
    })
    event.custom({
        "type": "nuclearcraftneohaul:infuser_recipe",
        "fluidIngredients": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:radaway_slow"
                }
            }
        ],
        "itemIngredients": [
            {
                "count": 2,
                "ingredient": {
                    "tag": "c:plates/plastic"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "nuclearcraftneohaul:radaway_slow"
                }
            }
        ],
        "powerModifier": 0.5,
        "radiation": 0.0,
        "timeModifier": 1.0
    })
    // duroplast in ingot former
    event.custom({
        "type": "nuclearcraftneohaul:ingot_former_recipe",
        "fluidIngredients": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:phenol"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "immersiveengineering:plate_duroplast"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 0.5
    })
    // basin compacting plastic fluids
    event.recipes.create.compacting("immersiveengineering:duroplast", [Fluid.sizedIngredientOf('immersiveengineering:phenolic_resin', 1000)])
    event.recipes.create.compacting("immersiveengineering:duroplast", [Fluid.sizedIngredientOf('nuclearcraftneohaul:phenol', 1000)])
    // blazinghot casting plastic fluids with sheet mold
    event.custom({
        "type": "create:casting",
        "cooling_duration": 150,
        "ingredients": [
            {
                "item": "blazinghot:sturdy_sheet_mold"
            },
            {
                "type": "neoforge:tag",
                "amount": 250,
                "tag": "c:molten_plastic"
            }
        ],
        "keep_mold": true,
        "processing_time": 50,
        "results": [
            {
                "id": "immersiveengineering:plate_duroplast"
            }
        ]
    })
    event.custom({
        "type": "create:casting",
        "cooling_duration": 150,
        "ingredients": [
            {
                "item": "blazinghot:porcelain_sheet_mold"
            },
            {
                "type": "neoforge:tag",
                "amount": 250,
                "tag": "c:molten_plastic"
            }
        ],
        "processing_time": 50,
        "results": [
            {
                "id": "immersiveengineering:plate_duroplast"
            }
        ]
    })

    // - Plastic Uses
    // createaddition:straw - lets any blaze burner sip oil. paper straws are BANNED
    event.remove({ id: "create:rolling/paper" })
    event.remove({ id: "create:rolling/bamboo" })
    event.custom({
        "type": "createaddition:rolling",
        "ingredients": [
            {
                "item": "immersiveengineering:plate_duroplast"
            }
        ],
        "results": [
            {
                "id": "createaddition:straw"
            }
        ]
    })
    // create fluid pipes and tanks
    event.shapeless(Item.of("create:fluid_pipe", 4), ["immersiveengineering:plate_duroplast", "immersiveengineering:plate_duroplast", "immersiveengineering:plate_duroplast", "minecraft:orange_dye"])
    donutCraft(event, "create:fluid_tank", "immersiveengineering:plate_duroplast", "minecraft:orange_dye")

    // - todo more Plastic Uses
    // synthetics: string, leather, paper, bottles and jars, light bulbs, spools, insulated wires, barrels, glass(?)
})
