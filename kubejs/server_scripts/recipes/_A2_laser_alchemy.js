ServerEvents.recipes(event => {
    // supplementaries:jar automated qol recipe
    let incJar = "kubejs:incomplete_jar"
    event.recipes.create.sequenced_assembly(
        ["supplementaries:jar"],
        "minecraft:glass",
        [
            event.recipes.create.cutting(incJar, incJar),
            event.recipes.create.deploying(incJar, [incJar, "minecraft:oak_slab"])
        ])
        .transitionalItem(incJar)

    event.recipes.create.deploying([incJar, "minecraft:oak_slab"], ["supplementaries:jar", "immersiveengineering:screwdriver"]).keepHeldItem(),

    // Creating a reagent: j-j-jam it in
    global.substrates.forEach(a => {
        a.forEach(e => {
            if (!e.ingredient)
                return
            event.recipes.create.sequenced_assembly(
                [e.id],
                incJar,
                [
                    event.recipes.create.deploying(incJar, [incJar, Item.of(e.ingredient)]),
                    event.recipes.create.deploying(incJar, [incJar, "minecraft:oak_slab"])
                ]).transitionalItem(incJar)
        })
    })


    // Analyzing failed alchemy in Oritech centrifuge
    let failure = (id, outputs) => {
        let outputFluids = []
        let outputItems = []
        if (outputs[0] > 0)
            outputFluids.push("" + outputs[0] + "000x kubejs:flux_goo")
        if (outputs[1] > 0)
            outputItems.push("" + outputs[1] + "x minecraft:redstone")
        if (outputs[2] > 0)
            outputItems.push("" + outputs[2] + "x minecraft:glowstone_dust")

        event.recipes.oritech
            .centrifuge_fluid()
            .itemInputs(`kubejs:failed_alchemy_${id}`)
            .fluidInput("4000x minecraft:water")
            .itemOutputs(outputItems)
            .fluidOutputs(outputFluids)
            .time(600)// 30 seconds in the microwave
    }

    for (let i = 0; i < 15; i++) {
        failure(i++, [4, 0, 0])
        failure(i++, [3, 1, 0])
        failure(i++, [3, 0, 1])
        failure(i++, [2, 2, 0])
        failure(i++, [2, 0, 2])

        failure(i++, [2, 1, 1])
        failure(i++, [1, 3, 0])
        failure(i++, [1, 0, 3])
        failure(i++, [1, 2, 1])
        failure(i++, [1, 1, 2])

        failure(i++, [0, 4, 0])
        failure(i++, [0, 0, 4])
        failure(i++, [0, 3, 1])
        failure(i++, [0, 1, 3])
        failure(i++, [0, 2, 2])
    }

    //

    /*
    let mundane = (id, outputs) => {
        let jsonOut = []
        if (outputs[0] > 0)
            jsonOut.push({
                "item": "supplementaries:ash",
                "count": outputs[0]
            })
        if (outputs[1] > 0)
            jsonOut.push({
                "item": "minecraft:redstone",
                "count": outputs[1]
            })
        if (outputs[2] > 0)
            jsonOut.push({
                "item": "minecraft:glowstone_dust",
                "count": outputs[2]
            })
        event.custom({
            "type": "thermal:centrifuge",
            "ingredient": {
                "item": `kubejs:failed_alchemy_${id}`
            },
            "result": jsonOut
        })
    }

    if (hasThermal) {
        let i = 0;

        mundane(i++, [4, 0, 0])
        mundane(i++, [3, 1, 0])
        mundane(i++, [3, 0, 1])
        mundane(i++, [2, 2, 0])
        mundane(i++, [2, 0, 2])

        mundane(i++, [2, 1, 1])
        mundane(i++, [1, 3, 0])
        mundane(i++, [1, 0, 3])
        mundane(i++, [1, 2, 1])
        mundane(i++, [1, 1, 2])

        mundane(i++, [0, 4, 0])
        mundane(i++, [0, 0, 4])
        mundane(i++, [0, 3, 1])
        mundane(i++, [0, 1, 3])
        mundane(i++, [0, 2, 2])
    }


    // Subtrate bottling and extracting
    event.remove({ type: "thermal:sawmill" })
    event.remove({ type: "thermal:centrifuge" })

    if (hasThermal) {
        global.substrates.forEach(a => {
            a.forEach(e => {
                if (!e.ingredient)
                    return
                event.custom({
                    "type": "thermal:bottler",
                    "ingredients": [Ingredient.of(e.ingredient).toJson(), { "fluid": "tconstruct:molten_glass", "amount": 100 }],
                    "result": [{ "item": e.id }]
                })
                event.custom({
                    "type": "thermal:sawmill",
                    "ingredient": { "item": e.id },
                    "result": [{ "item": e.outputItem ? e.outputItem : typeof e.ingredient == "string" ? e.ingredient : e.ingredient[0], "chance": 0.75 }],
                    "energy": 2000
                })
            })
        })
    }

    // Silicon and silver special cases
    if (hasThermal) {
        event.custom({
            "type": "thermal:sawmill",
            "ingredient": { "item": "kubejs:substrate_silicon" },
            "result": [{ "item": "ae2:silicon", "count": 1 }],
            "energy": 2000
        })

        event.custom({
            "type": "thermal:sawmill",
            "ingredient": { "item": "kubejs:substrate_silver" },
            "result": [{ "item": "thermal:silver_dust", "count": 1 }],
            "energy": 2000
        })
    }

    // Accelerators
    if (hasThermal) {
        event.custom({
            "type": "thermal:bottler",
            "ingredients": [
                { "item": "thermal:signalum_nugget" },
                { "fluid": "tconstruct:molten_glass", "amount": 100 }
            ],
            "result": [{ "item": "kubejs:accellerator_redstone" }]
        })

        event.custom({
            "type": "thermal:bottler",
            "ingredients": [
                { "item": "thermal:lumium_nugget" },
                { "fluid": "tconstruct:molten_glass", "amount": 100 }
            ],
            "result": [{ "item": "kubejs:accellerator_glowstone" }]
        })

        // Not sure why silver subtrate has a recipe
        event.custom({
            "type": "thermal:bottler",
            "ingredients": [
                { "item": "thermal:silver_dust" },
                { "fluid": "tconstruct:molten_glass", "amount": 100 }
            ],
            "result": [{ "item": "kubejs:substrate_silver" }]
        })
    }
    */
})
