ServerEvents.recipes(event => {

    let illusion = (input) => {
        const output = `kubejs:trial_${input.replace(/^minecraft:|^trials:|^everythingcopper:/, "")}`;
        if (!Item.exists(input) || !Item.exists(output)) return
        event.custom({
            "type": "create:filling",
            "ingredients": [
                { "item": input },
                { "type": "neoforge:single", "fluid": "supplementaries:lumisene", "amount": 90 }
            ],
            "results": [
                { "id": output, "count": 9 }
            ]
        })
    }

    illusion("minecraft:copper_block");
    illusion("minecraft:cut_copper");
    illusion("minecraft:cut_copper_stairs");
    illusion("minecraft:cut_copper_slab");
    illusion("minecraft:oxidized_copper");
    illusion("minecraft:oxidized_cut_copper_stairs");
    illusion("minecraft:oxidized_cut_copper_slab");
    illusion("minecraft:oxidized_cut_copper");
    // 1.21 moved chiseled copper and copper grates into vanilla. Everything Copper
    // still ships the models, but those four items are not in the item registry.
    // The fluid input is the same neoforge:single shape the other copper fills use.
    let copperVariant = (modern, legacy) => Item.exists(legacy) ? legacy : modern
    illusion(copperVariant("minecraft:chiseled_copper", "everythingcopper:chiseled_copper"))
    illusion(copperVariant("minecraft:copper_grate", "everythingcopper:copper_grate"))
    illusion(copperVariant("minecraft:oxidized_chiseled_copper", "everythingcopper:oxidized_chiseled_copper"))
    illusion(copperVariant("minecraft:oxidized_copper_grate", "everythingcopper:oxidized_copper_grate"))
})
