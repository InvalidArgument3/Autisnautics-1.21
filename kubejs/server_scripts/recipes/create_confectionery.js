// priority: 0

ServerEvents.recipes(event => {
    // The jar's candy cane mix uses the old fluid_stack ingredient, which Create
    // still loads but which drops the potion components. Without them any
    // create:potion satisfies the recipe. Match a regular mundane potion, the
    // same way Create's own potion filling recipes do.
    event.remove({ id: "create_confectionery:candy_cane_recipe" })
    event.custom({
        type: "create:mixing",
        heat_requirement: "heated",
        ingredients: [
            { item: "minecraft:sugar" },
            { item: "minecraft:sugar" },
            { item: "minecraft:slime_ball" },
            {
                type: "neoforge:components",
                amount: 250,
                fluids: "create:potion",
                components: {
                    "create:potion_fluid_bottle_type": "regular",
                    "minecraft:potion_contents": {
                        potion: "minecraft:mundane"
                    }
                }
            }
        ],
        results: [
            { id: "create_confectionery:candy_cane", count: 4 }
        ]
    }).id("create_confectionery:candy_cane_recipe")
})
