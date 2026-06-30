// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 3a
    let transitional

    // -- Potion of Glowing
    // recipe in potions.js: glow lichen + awkward potion

    // -- Glowing Mushroom
    event.remove({ id: "nuclearcraftneohaul:glowing_mushroom" })
    event.remove({ id: "minecraft:glowing_mushroom" }) // nc un-crafting recipe using minecraft namespace
    event.recipes.create.filling("nuclearcraftneohaul:glowing_mushroom",
        ["minecraft:brown_mushroom",
            potionIngredient("alexscaves:glowing", 50)]) // helper function

    // -- Radaway
    event.recipes.create.mixing([Fluid.of("nuclearcraftneohaul:radaway", 250)], [SizedIngredient.of("nuclearcraftneohaul:glowing_mushroom", 3), Fluid.sizedIngredientOf("minecraft:milk", 250)]).heated()
    event.recipes.create.mixing([Fluid.of("nuclearcraftneohaul:radaway_slow", 250)], [SizedIngredient.of("minecraft:redstone", 1), Fluid.sizedIngredientOf("nuclearcraftneohaul:radaway", 250)]).heated()
    // - replace default recipes
    event.remove({ id: /nuclearcraftneohaul:fluid_enricher\/radaway.*/ })
    event.custom({
        "type": "nuclearcraftneohaul:enricher_recipe",
        "fluidIngredients": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "minecraft:milk"
                }
            }
        ],
        "fluidProducts": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:radaway"
                }
            }
        ],
        "itemIngredients": [
            {
                "count": 3,
                "ingredient": {
                    "item": "nuclearcraftneohaul:glowing_mushroom"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    })
    event.custom({
        "type": "nuclearcraftneohaul:enricher_recipe",
        "fluidIngredients": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:radaway"
                }
            }
        ],
        "fluidProducts": [
            {
                "amount": 250,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:radaway_slow"
                }
            }
        ],
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "item": "minecraft:redstone"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    })
    // - IE compat
    event.custom({
        "type": "immersiveengineering:mixer",
        "energy": 3200,
        "fluid": {
            "amount": 250,
            "tag": "c:milk"
        },
        "inputs": [
            {
                "item": "nuclearcraftneohaul:glowing_mushroom",
                "count": 3
            }
        ],
        "result": {
            "amount": 250,
            "id": "nuclearcraftneohaul:radaway"
        }
    })
    event.custom({
        "type": "immersiveengineering:mixer",
        "energy": 3200,
        "fluid": {
            "amount": 250,
            "tag": "nuclearcraftneohaul:radaway"
        },
        "inputs": [
            {
                "item": "minecraft:redstone",
                "count": 1
            }
        ],
        "result": {
            "amount": 250,
            "id": "nuclearcraftneohaul:radaway_slow"
        }
    })

    // -- Boron production
    // there are no natural sources left! but the trace amounts in evaporite...
    // - Borax production
    // 10% byproduct from crushing Ancient Sandstone found in Yung's Lost Caves biome
    if (Platform.isLoaded("yungscavebiomes")) {
        event.recipes.create.crushing(["yungscavebiomes:ancient_sand", CreateItem.of("nuclearcraftneohaul:borax", 0.1)], "yungscavebiomes:ancient_sandstone")
    }
    // 1% trace byproduct from throwing sandstones into NC rock crusher: huge RF hog, but can be fully automated
    event.custom({
        "type": "nuclearcraftneohaul:rock_crusher_recipe",
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "tag": "c:sandstone/uncolored_blocks"
                }
            }
        ],
        "itemProducts": [
            {
                "chancePercent": 100,
                "count": 1,
                "ingredient": {
                    "item": "minecraft:sand"
                }
            },
            {
                "chancePercent": 1,
                "count": 1,
                "ingredient": {
                    "item": "nuclearcraftneohaul:borax"
                }
            }
        ],
        "powerModifier": 100.0,
        "radiation": 0.0,
        "timeModifier": 10.0
    }).id("kubejs:rock_crusher/borax_from_sandstone")
    event.custom({
        "type": "nuclearcraftneohaul:rock_crusher_recipe",
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "tag": "c:sandstone/red_blocks"
                }
            }
        ],
        "itemProducts": [
            {
                "chancePercent": 100,
                "count": 1,
                "ingredient": {
                    "item": "minecraft:red_sand"
                }
            },
            {
                "chancePercent": 1,
                "count": 1,
                "ingredient": {
                    "item": "nuclearcraftneohaul:borax"
                }
            }
        ],
        "powerModifier": 100.0,
        "radiation": 0.0,
        "timeModifier": 10.0
    }).id("kubejs:rock_crusher/borax_from_red_sandstone")
    // - Borax Solution from Borax: fluid enricher, default nc recipe
    // - Boric Acid from Borax Solution: 250mb borax solution + 50mb HCl (alexscaves acid) = 100mb boric acid + 125mb water + NaCl (galosphere:pink_salt_shard)
    event.recipes.create.mixing([Fluid.of("nuclearcraftneohaul:boric_acid", 100), Fluid.of("minecraft:water", 125), "galosphere:pink_salt_shard"],
        [Fluid.sizedIngredientOf("nuclearcraftneohaul:borax_solution", 250), Fluid.sizedIngredientOf("alexscaves:acid", 50)]).heated()
    // - Boron Dust from Boric Acid: Boric acid heats to Boron Trioxide, reacting with Aluminum = elemental boron, and a fiery explosion (thermite reaction)
    // rightClicked event in thermite_reaction.js, todo add information

    // -- Ferroboron Alloy
    // - tweak recipes so Ferroboron alloy is only 20/80 boron/steel instead of 50/50
    event.remove({ id: "nuclearcraftneohaul:fluid_mixer/ferroboron_from_steel_boron" })
    event.custom({
        "type": "nuclearcraftneohaul:salt_mixer_recipe",
        "fluidIngredients": [
            {
                "amount": 72,
                "ingredient": {
                    "fluid": "createbigcannons:molten_steel"
                }
            },
            {
                "amount": 18,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:boron"
                }
            }
        ],
        "fluidProducts": [
            {
                "amount": 90,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:ferroboron"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    }).id("kubejs:fluid_mixer/ferroboron")
    event.remove({ id: "nuclearcraftneohaul:alloy_furnace/ferroboron_alloy" })
    event.custom({
        "type": "nuclearcraftneohaul:alloy_furnace_recipe",
        "itemIngredients": [
            {
                "count": 4,
                "ingredient": [
                    {
                        "tag": "c:dusts/steel"
                    },
                    {
                        "tag": "c:ingots/steel"
                    }
                ]
            },
            {
                "count": 1,
                "ingredient": [
                    {
                        "tag": "c:dusts/boron"
                    },
                    {
                        "tag": "c:ingots/boron"
                    }
                ]
            }
        ],
        "itemProducts": [
            {
                "count": 5,
                "ingredient": {
                    "item": "nuclearcraftneohaul:ferroboron_alloy"
                }
            }
        ],
        "powerModifier": 2.25,
        "radiation": 0.0,
        "timeModifier": 5.0
    }).id("kubejs:alloy_smelter/ferroboron")
    // - compat
    // create
    event.recipes.create.mixing(Fluid.of("nuclearcraftneohaul:ferroboron", 90),
        [Fluid.sizedIngredientOf("createbigcannons:molten_steel", 72),
            Fluid.sizedIngredientOf("nuclearcraftneohaul:boron", 18)])
        .superheated()
    // IE arc furnace
    event.custom({
        "type": "immersiveengineering:arc_furnace",
        "additives": [
            {
                "tag": "c:ingots/boron"
            }
        ],
        "energy": 51200,
        "input": {
            "basePredicate": {
                "tag": "c:ingots/steel"
            },
            "count": 4
        },
        "results": [
            {
                "basePredicate": {
                    "tag": "c:ingots/ferroboron"
                },
                "count": 5
            }
        ],
        "time": 500 // due to 5x output
    })

    // -- Atomic Mechanisms
    transitional = "kubejs:incomplete_atomic_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:atomic_mechanism",
    ], "kubejs:inductive_mechanism", [
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("nuclearcraftneohaul:ferroboron", 180)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("nuclearcraftneohaul:uranium", 10)]),
        event.recipes.create.pressing(transitional, [transitional]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("nuclearcraftneohaul:lead", 90)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("nuclearcraftneohaul:radaway", 250)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:atomic_mechanism")

    // -- Boron Casing
    event.shapeless(Item.of("kubejs:boron_casing", 2), [Ingredient.of("#c:ingots/ferroboron"), "immersiveengineering:sheetmetal_lead"])

    // -- Boron Machine
    donutCraft(event, "kubejs:boron_machine", "kubejs:boron_casing", "kubejs:atomic_mechanism")
    // - Usages
    // nuclearcraft
    // rest of the machines
    boronMachine(event, Item.of("nuclearcraftneohaul:centrifuge", 1), "create:mechanical_mixer")
    boronMachine(event, Item.of("nuclearcraftneohaul:chemical_reactor", 1), "minecraft:brewing_stand")
    boronMachine(event, Item.of("nuclearcraftneohaul:decay_hastener", 1), "minecraft:composter")
    boronMachine(event, Item.of("nuclearcraftneohaul:electrolyzer", 1), "minecraft:lightning_rod")
    boronMachine(event, Item.of("nuclearcraftneohaul:fluid_infuser", 1), "create:spout")
    boronMachine(event, Item.of("nuclearcraftneohaul:fuel_reprocessor", 1), "create:crushing_wheel")
    boronMachine(event, Item.of("nuclearcraftneohaul:pressurizer", 1), "create:mechanical_press")
    boronMachine(event, Item.of("nuclearcraftneohaul:separator", 1), "create:mechanical_saw")
    boronMachine(event, Item.of("nuclearcraftneohaul:supercooler", 1), "minecraft:blue_ice")
    // also uses a machine chassis
    boronMachine(event, Item.of("nuclearcraftneohaul:machine_interface", 1))
    // multiblock machines/fission/etc gated behind lithium (wasteland)
    // misc
    event.remove({ id: "nuclearcraftneohaul:basic_voltaic_pile" }) // it's worse than an accumulator so skip it
    boronMachine(event, Item.of("nuclearcraftneohaul:advanced_voltaic_pile", 1), "nuclearcraftneohaul:magnesium_block") // wasteland gated
    // speed upgrade uses rare(?) silver
    boronMachine(event, Item.of("nuclearcraftneohaul:speed_upgrade", 1), "#c:storage_blocks/silver")
    // energy upgrade: spend energy now to save energy later
    event.custom({
        "type": "createaddition:charging",
        "energy": 200000,
        "ingredients": [
            {
                "item": "kubejs:boron_machine"
            }
        ],
        "max_charge_rate": 2000,
        "results": [
            {
                "id": "nuclearcraftneohaul:energy_upgrade"
            }
        ]
    })

    // -- Epilogue
    event.remove({ output: "supplementaries:globe" })
    event.shaped("supplementaries:globe", [
        "RM ",
        "MBM",
        " MR"
    ], {
        R: "#c:rods/brass",
        M: "minecraft:map",
        B: "#c:slimeballs"
    })
    event.remove({ output: "supplementaries:globe_sepia" })
    event.custom({
        "type": "nuclearcraftneohaul:infuser_recipe",
        "fluidIngredients": [
            {
                "amount": 1000,
                "ingredient": {
                    "fluid": "blazinghot:molten_ancient_debris"
                }
            }
        ],
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "item": "supplementaries:globe"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "supplementaries:globe_sepia"
                }
            }
        ],
        "powerModifier": 100.0, // 1000 rf/t
        "radiation": 0.0,
        "timeModifier": 40 // one in-game day
    })
})
