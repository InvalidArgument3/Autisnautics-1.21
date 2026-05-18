// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 2b
    let transitional
    // -- Liquid Pulp
    event.recipes.create.mixing(Fluid.of("kubejs:liquid_pulp", 500), ["create:pulp", Fluid.sizedIngredientOf("minecraft:water", 500)]).heated()
    // - Casting Liquid Pulp into cardboard things, in case you wanted to store pulp as liquid
    let moldMats = ["porcelain", "sturdy"]
    let pulpCast = (mb, mold, output) => {
        moldMats.forEach(moldMat => {
            let moldItem = `blazinghot:${moldMat}_${mold}_mold`
            event.custom({
                "type": "create:casting",
                "cooling_duration": 300,
                "ingredients": [
                    {
                        "item": moldItem
                    },
                    {
                        "type": "neoforge:tag",
                        "amount": mb,
                        "tag": "kubejs:liquid_pulp"
                    }
                ],
                "processing_time": 100,
                "results": [
                    {
                        "id": output
                    }
                ]
            })
            event.custom({
                "type": "create:casting",
                "cooling_duration": 300,
                "ingredients": [
                    {
                        "item": moldItem
                    },
                    {
                        "type": "neoforge:tag",
                        "amount": mb,
                        "tag": "c:molten_brass"
                    }
                ],
                "processing_time": 100,
                "results": [
                    {
                        "id": output
                    }
                ]
            })
        })
    }
    pulpCast(500, "sheet", "create:cardboard")
    pulpCast(1000, "rod", "create:cardboard_sword")
    event.recipes.create.compacting("create:cardboard_block", [Fluid.sizedIngredientOf('kubejs:liquid_pulp', 2000)])

    // -- Potion of Haste: modded duplicates removed in potions.js

    // -- Logistic Mechanisms
    transitional = "kubejs:incomplete_logistic_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:logistic_mechanism",
    ], "create:precision_mechanism", [
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf(
            Fluid.ingredientOf('create:potion',
                {'create:potion_fluid_bottle_type': 'regular',
                    'minecraft:potion_contents': { potion: 'kubejs:haste' },
                }
            ),
            500
        )]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("kubejs:liquid_pulp", 200)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("kubejs:liquid_pulp", 200)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("kubejs:liquid_pulp", 200)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:logistic_mechanism")

    // -- Lead Machine
    donutCraft(event, "kubejs:lead_machine", "kubejs:lead_casing", "kubejs:logistic_mechanism")
    // - Usages
    // Create
    leadMachine(event, Item.of("create:package_frogport", 1), Ingredient.of("#c:slimeballs"))
    leadMachine(event, Item.of("create:packager", 1), "create:cardboard_block")
    leadMachine(event, Item.of("create:repackager", 1), "create:bound_cardboard_block")
    leadMachine(event, Item.of("create:factory_gauge", 2))
    leadMachine(event, Item.of("create:redstone_requester", 1), "minecraft:redstone_torch")
    // replace transmitter in stock link recipe
    event.replaceInput({ id: "create:crafting/logistics/stock_link" }, "create:item_vault", "kubejs:lead_casing")
    event.replaceInput({ id: "create:crafting/logistics/stock_link" }, "create:transmitter", "minecraft:lightning_rod")
    // Item vault/Shipping container convenience recipes
    event.shapeless("create:item_vault", ["kubejs:lead_casing", Ingredient.of("#c:barrels/wooden")])
    colours.forEach(color => {
        event.shapeless("createdeco:" + color + "_shipping_container", ["minecraft:" + color + "_dye", "kubejs:lead_casing", Ingredient.of("#c:barrels/wooden")])
    })

    // Stellaris
    leadMachine(event, Item.of("stellaris:vacuumator", 1), "minecraft:bucket")
})
