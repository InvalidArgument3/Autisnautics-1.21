// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 3
    let transitional

    // -- Farming Ender Dust from Snow Golems
    // - Grinding Haunted Rind into Haunted Dust
    event.recipes.create.milling(["kubejs:haunted_dust"], "kubejs:haunted_rind")
    event.recipes.create.crushing(["kubejs:haunted_dust", CreateItem.of("kubejs:haunted_dust", 0.25)], "kubejs:haunted_rind")
    event.custom({
        "type": "immersiveengineering:crusher",
        "energy": 1600,
        "input": {
            "item": "kubejs:haunted_rind"
        },
        "result": {
            "id": "kubejs:haunted_dust"
        },
        "secondaries": [
            {
                "chance": 0.25,
                "output": {
                    "item": "kubejs:haunted_dust"
                }
            }
        ]
    })
    event.custom({
        "type": "nuclearcraftneohaul:manufactory_recipe",
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "item": "kubejs:haunted_rind"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "kubejs:haunted_dust"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 0.5
    })
    // - Bulk Washing Haunted Dust to Ender Dust (can also be obtained through laser alchemy)
    event.recipes.create.splashing(["ae2:ender_dust"], "kubejs:haunted_dust")

    // -- Gunpowder Production
    // - Remove unwanted sources
    event.remove({ id: "galosphere:gunpowder_from_pink_salt_shard" })
    event.remove({ id: "alexscaves:gunpowder_from_sulfur" })
    event.remove({ id: "create:splashing/crushed_raw_zinc" }) // metallurgy redo should fix this anyway
    event.remove({ id: "create:filling/gunpowder" })
    // - CDG Diesel as a sulfur source - simpler than IE high pressure refiner but inferior
    // Lubricant -> Sulfurized Diesel + Paraffin Wax
    event.recipes.create.mixing([Fluid.of("immersivepetroleum:diesel_sulfur", 800), CreateItem.of("immersivepetroleum:paraffin_wax", 0.75)],
        [Fluid.sizedIngredientOf("immersivepetroleum:lubricant", 1000),
            Fluid.sizedIngredientOf("minecraft:water", 500)])
        .superheated()
    // Sulfurized Diesel -> CDG Diesel + Sulfur
    event.recipes.create.mixing([Fluid.of("createdieselgenerators:diesel", 800),
        Item.of("immersiveengineering:dust_sulfur", 3),
        CreateItem.of("immersiveengineering:dust_sulfur", 0.75)],
    [Fluid.sizedIngredientOf("immersivepetroleum:diesel_sulfur", 1000),
        Fluid.sizedIngredientOf("minecraft:water", 500)])
        .superheated()
    // - Galosphere Pink Salt stuff as a nitrate source
    event.recipes.create.milling([Item.of("immersiveengineering:dust_saltpeter", 2)], "galosphere:pink_salt_shard")
    event.recipes.create.crushing(["immersiveengineering:dust_saltpeter", CreateItem.of("immersiveengineering:dust_saltpeter", 0.5)],
        "galosphere:pink_salt")
    event.recipes.create.crushing(["immersiveengineering:dust_saltpeter", CreateItem.of("immersiveengineering:dust_saltpeter", 0.5)],
        "galosphere:rose_pink_salt")
    event.recipes.create.crushing(["immersiveengineering:dust_saltpeter", CreateItem.of("immersiveengineering:dust_saltpeter", 0.5)],
        "galosphere:pastel_pink_salt")
    // - Small nitrate byproduct from crushing dripstone block
    event.recipes.create.crushing(["minecraft:clay_ball", CreateItem.of("immersiveengineering:dust_saltpeter", 0.25)], "minecraft:dripstone_block")

    // -- Singularity Production
    // - Crushing Wheels: simpler recipe
    event.remove({ id: "create:mechanical_crafting/crushing_wheel" })
    event.recipes.create.mechanical_crafting(Item.of("create:crushing_wheel", 2), [
        " AAA ",
        "AABAA",
        "ABBBA",
        "AABAA",
        " AAA "
    ], {
        A: "#c:cobblestones",
        B: "minecraft:stick"
    })
    // - Crushing the Crushing Wheel in the Crushing Wheel...
    event.recipes.create.crushing([Item.of("ae2:singularity")], "create:crushing_wheel").processingTime(250)

    // -- Chromatic Singularities
    let chromaticColors = colours.slice(1).filter((entry) => (entry.includes("gray") || entry.includes("black")) == false ) // only colorful dyes allowed
    chromaticColors.forEach(color => {
        event.recipes.create.compacting(Item.of("kubejs:dye_entangled_singularity", 1), [`minecraft:${color}_dye`, "ae2:quantum_entangled_singularity"])
    })
    // - More efficient but complex recipe using create dragons plus liquid dye
    if (Platform.isLoaded("create_dragons_plus")) {
        chromaticColors.forEach(color => {
            event.recipes.create.compacting([Item.of("kubejs:dye_entangled_singularity", 1), Fluid.of("minecraft:water", 125)],
                [Fluid.sizedIngredientOf(`create_dragons_plus:${color}_dye`, 125), "ae2:quantum_entangled_singularity"])
        })
    }

    // -- Paint Balls and Color Correction
    event.remove({ id: /ae2:tools\/paintballs.*/ })
    event.recipes.create.crushing([
        CreateItem.of("ae2:red_paint_ball", 0.9),
        CreateItem.of("ae2:yellow_paint_ball", 0.75),
        CreateItem.of("ae2:green_paint_ball", 0.6),
        CreateItem.of("ae2:blue_paint_ball", 0.45),
        CreateItem.of("ae2:magenta_paint_ball", 0.3)],
    "kubejs:dye_entangled_singularity").processingTime(50)
    // - Paint Ball Depleting: using supplementaries:lumisene as a more fun waste material, which can also be lit on fire as a light source later
    let spectrum = ["red", "yellow", "green", "blue", "magenta", "black"]
    for (let i = 0; i < spectrum.length; i++) {
        let color = spectrum[i];
        if (i == spectrum.length - 1)
            continue;
        event.recipes.create.emptying([`ae2:${spectrum[i + 1]}_paint_ball`, Fluid.of("supplementaries:lumisene", 100)], `ae2:${color}_paint_ball`)
    }

    // -- Chromatic Compound
    event.recipes.create.mechanical_crafting("create:chromatic_compound", [
        "AA",
        "AA"
    ], {
        A: "ae2:magenta_paint_ball"
    })
    // Convenience recipe with higher tech
    event.custom({
        "type": "nuclearcraftneohaul:assembler_recipe",
        "itemIngredients": [
            {
                "count": 4,
                "ingredient": {
                    "item": "ae2:magenta_paint_ball"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "create:chromatic_compound"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 1.0
    })


    // -- Refined Radiance: Create is bugged so this can't be made without a beacon, temporary fix below (EntityEvents.spawned for dropped items)
    // - besides burning lumisene waste, you can manufacture torches from trees, deploy bonemeal on glow lichen, etc...
    // todo: mysterious conversion/information entry/tooltip etc

    // -- Radiant Coils
    event.recipes.create.pressing("kubejs:radiant_sheet", "create:refined_radiance")
    event.recipes.create.mechanical_crafting("kubejs:radiant_coil", ["A"], { A: "kubejs:radiant_sheet" })
    // Convenience recipe with higher tech
    event.custom({
        "type": "nuclearcraftneohaul:assembler_recipe",
        "itemIngredients": [
            {
                "count": 1,
                "ingredient": {
                    "item": "kubejs:radiant_sheet"
                }
            }
        ],
        "itemProducts": [
            {
                "count": 1,
                "ingredient": {
                    "item": "kubejs:radiant_coil"
                }
            }
        ],
        "powerModifier": 1.0,
        "radiation": 0.0,
        "timeModifier": 0.5
    })

    // -- Chromatic Resonator
    // - normal recipe, using powergrid:magnet
    event.shaped("kubejs:chromatic_resonator", [
        " M ",
        "M M",
        "LM "
    ], {
        M: "powergrid:magnet",
        L: Ingredient.of("#c:ingots/lead")
    })
    // - alternate recipe using alexscaves magnet parts
    event.shaped("kubejs:chromatic_resonator", [
        " R ",
        "R B",
        "LB "
    ], {
        R: "alexscaves:scarlet_neodymium_ingot",
        L: Ingredient.of("#c:ingots/lead"),
        B: "alexscaves:azure_neodymium_ingot"
    })

    // -- Inductive Mechanisms
    transitional = "kubejs:incomplete_inductive_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:inductive_mechanism",
    ], "create:precision_mechanism", [
        event.recipes.create.deploying(transitional, [transitional, "kubejs:radiant_coil"]),
        event.recipes.create.deploying(transitional, [transitional, "kubejs:radiant_coil"]),
        event.recipes.create.deploying(transitional, [transitional, Ingredient.of("#kubejs:chromatic_resonators")])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:inductive_mechanism")

    // -- Constantan Casing
    event.shapeless(Item.of("kubejs:constantan_casing", 2), [Ingredient.of("#c:plates/constantan"), "immersiveengineering:sheetmetal_steel"])

    // -- Constantan Machines
    donutCraft(event, "kubejs:constantan_machine", "kubejs:constantan_casing", "kubejs:inductive_mechanism")
    // - Usages
    // Nuclearcraft
    // todo
})


// getting around bugged Refined Radiance
const CreateAllDataComponents = Java.loadClass('com.simibubi.create.AllDataComponents')

EntityEvents.spawned(event => {
    if (event.entity.item.get(CreateAllDataComponents.CHROMATIC_COMPOUND_COLLECTING_LIGHT) >= 1) {
        if (event.entity.item.getCustomData().getInt("kubejsLight")) {
            let data = event.entity.item.getCustomData()
            let light = data.getInt("kubejsLight")
            data.putInt("kubejsLight", (light + 1))
            event.entity.item.setCustomData(data)
        }
        else {
            let data = event.entity.item.getCustomData()
            data.putInt("kubejsLight", 1)
            event.entity.item.setCustomData(data)
        }
        if (event.entity.item.getCustomData().getInt("kubejsLight") >= 50) { // 5x slower than default config because lumisene lasts a long time
            event.entity.item.set(CreateAllDataComponents.CHROMATIC_COMPOUND_COLLECTING_LIGHT, 10) // should still work?
        }
    }
})
