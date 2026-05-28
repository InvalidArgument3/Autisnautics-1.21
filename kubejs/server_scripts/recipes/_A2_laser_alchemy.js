ServerEvents.recipes(event => {
    // supplementaries:jar automated qol recipe
    let incJar = "kubejs:incomplete_jar"
    event.recipes.create.sequenced_assembly(
        ["supplementaries:jar"],
        "minecraft:glass",
        [
            event.recipes.create.cutting(incJar, incJar),
            event.recipes.create.deploying(incJar, [incJar, Ingredient.of("#minecraft:wooden_slabs")])
        ])
        .transitionalItem(incJar)

    // opening a jar with a screwdriver
    event.recipes.create.item_application([incJar, "minecraft:oak_slab"], ["supplementaries:jar", "immersiveengineering:screwdriver"]).keepHeldItem()
    
    // putting the lid back on an empty jar
    event.recipes.create.item_application("supplementaries:jar", [incJar, Ingredient.of("#minecraft:wooden_slabs")])

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
                    event.recipes.create.deploying(incJar, [incJar, Ingredient.of("#minecraft:wooden_slabs")])
                ]).transitionalItem(incJar)
        })
    })
    
    // Accelerators
    event.recipes.create.sequenced_assembly(
        ["kubejs:accelerator_redstone"],
        incJar,
        [
            event.recipes.create.deploying(incJar, [incJar, Item.of("minecraft:redstone_block")]),
            event.recipes.create.deploying(incJar, [incJar, Ingredient.of("#minecraft:wooden_slabs")]),
            event.recipes.create.pressing(incJar, [incJar])
    ]).transitionalItem(incJar)
    event.recipes.create.sequenced_assembly(
        ["kubejs:accelerator_glowstone"],
        incJar,
        [
            event.recipes.create.deploying(incJar, [incJar, Item.of("minecraft:glowstone")]),
            event.recipes.create.deploying(incJar, [incJar, Ingredient.of("#minecraft:wooden_slabs")]),
            event.recipes.create.pressing(incJar, [incJar])
    ]).transitionalItem(incJar)
    
    // NC Fluid Extractor -> Reagent Extractor
    // Reagent Extraction
    let reagentExtract = (e, substrate, special) => {
        e.custom({
            "type": "nuclearcraftneohaul:extractor_recipe",
            "itemIngredients": [
                {
                "count": 1,
                "ingredient": {
                    "item": substrate.id
                }
                }
            ],
            "itemProducts": [
                {
                "chancePercent": special ? 100 : 75,
                "count": 1,
                "ingredient": {
                    "item": special ? special : substrate.ingredient
                }
                }
            ],
            "fluidProducts": [
                {
                "amount": 75,
                "ingredient": {
                    "fluid": "nuclearcraftneohaul:corium" // temporary
                }
                }
            ],
            "powerModifier": 1.0,
            "radiation": 0.0,
            "timeModifier": 0.25 // 30 seconds
        }).id(`kubejs:reagent_extraction/${substrate.id.slice(17)}`)
    }
    global.substrates.forEach(a => {
        a.forEach(s => {
            if (!s.ingredient) {
                // Weird Reagents
                if (s.id == "kubejs:substrate_silicon") {
                    reagentExtract(event, s, "ae2:silicon")
                }
                else if (s.id == "kubejs:substrate_silver") {
                    reagentExtract(event, s, "immersiveengineering:dust_silver")
                }
                else { return }
            }
            reagentExtract(event, s, null)
        })
    })
    
    // Failed Alchemy Analysis in the Rock Crusher
    let failure = (id, outputs) => {
        let dustProducts = []
        if (outputs[0] > 0)
            dustProducts.push({ "count": outputs[0], "ingredient": { "item": "supplementaries:ash" } })
        if (outputs[1] > 0)
            dustProducts.push({ "count": outputs[1], "ingredient": { "item": "minecraft:redstone" } })
        // testing 2 outputs
        if (outputs[2] > 0)
            dustProducts.push({ "count": outputs[2], "ingredient": { "item": "minecraft:glowstone_dust" } })
        
        event.custom({
            "type": "nuclearcraftneohaul:rock_crusher_recipe",
            "itemIngredients": [
                {
                "count": 1,
                "ingredient": {
                    "item": `kubejs:failed_alchemy_${id}`
                }
                }
            ],
            "itemProducts": dustProducts,
            "powerModifier": 1.0,
            "radiation": 0.0,
            "timeModifier": 15.0 // 5 minutes
        }).id(`kubejs:failed_alchemy_analysis/${id}`)
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
})
