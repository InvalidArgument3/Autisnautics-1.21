// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 1B
    let transitional

    // -- Remove sturdy sheets, sturdy ingots, sturdy casing, molten sturdy alloy + bucket, sturdy everything
    event.remove({ id: "create:sequenced_assembly/sturdy_sheet"})
    event.remove({ id: /blazinghot:casting.*sturdy.*mold$/})
    event.remove({ id: /blazinghot:casting.*sturdy_sheet$/})
    event.remove({ id: /blazinghot:casting.*sturdy_alloy$/})
    event.remove({ id: "blazinghot:pressing/sturdy_alloy"})
    event.remove({ id: "blazinghot:compacting/sturdy_alloy"})
    event.remove({ id: /blazinghot:blaze_mixing\/melting\/sturdy.*/})
    event.remove({ id: /blazinghot:.*mixing.*molten_sturdy_alloy$/})
    event.remove({ id: /blazinghot:.*sturdy_casing.*/})
    /*
    event.remove({ output: { item: "blazinghot:sturdy_alloy" } })
    event.remove({ output: { item: "blazinghot:sturdy_alloy_bucket" } })
    event.remove({ output: Fluid.of("blazinghot:molten_sturdy_alloy") })
    event.remove({ output: { item: "blazinghot:sturdy_casing" } })
    event.remove({ input: { item: "create:sturdy_sheet" } })
    */
    // - Sturdy sheet replacements
    // create:schedule - below
    // immersive_aircraft:engine - brass machines
    event.remove({ id: "immersive_aircraft:engine" })
    // numismatics:brass_depositor - brass machines
    event.remove({ id: "numismatics:brass_depositor" })
    // numismatics:banking_guide
    event.remove({ output: "numismatics:banking_guide" })
    event.shapeless("numismatics:banking_guide", ["numismatics:cog", "minecraft:paper"])
    // - Sturdy casing: give a separate recipe so it's a cosmetic block
    event.recipes.create.haunting("blazinghot:sturdy_casing", "create:railway_casing")
    // Might as well get the blaze casing and mixer while we're at it
    event.remove({ id: /blazinghot:.*blaze_casing.*/ })
    event.remove({ output: "blazinghot:blaze_whisk" })
    event.remove({ output: "blazinghot:blaze_mixer" })
    event.recipes.create.haunting("blazinghot:blaze_casing", "create:copper_casing")
    // - Sturdy molds: use molten brass or molten gold instead, like tinkers
    // Blank molds are removed because they have zero purpose
    let goldMold = (outputType, inputTag) => {
        let mold = `blazinghot:sturdy_${outputType}_mold`
        event.custom({
            "type": "create:casting",
            "cooling_duration": 300,
            "ingredients": [
                {
                    "tag": inputTag
                },
                {
                    "type": "neoforge:tag",
                    "amount": 180,
                    "tag": "c:molten_gold"
                }
            ],
            "processing_time": 100,
            "results": [
                {
                    "id": mold
                }
            ]
        })
        event.custom({
            "type": "create:casting",
            "cooling_duration": 300,
            "ingredients": [
                {
                    "tag": inputTag
                },
                {
                    "type": "neoforge:tag",
                    "amount": 180,
                    "tag": "c:molten_brass"
                }
            ],
            "processing_time": 100,
            "results": [
                {
                    "id": mold
                }
            ]
        })
    }
    goldMold("ingot", "c:ingots")
    goldMold("nugget", "c:nuggets")
    goldMold("sheet", "c:plates")
    goldMold("rod", "c:rods")

    // -- Magma block production
    event.blasting("minecraft:magma_block", "minecraft:deepslate")

    // -- Reinforced mechanisms
    transitional = "kubejs:incomplete_reinforced_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:reinforced_mechanism",
    ], "kubejs:kinetic_mechanism", [
        event.recipes.create.deploying(transitional, [transitional, "minecraft:obsidian"]),
        event.recipes.create.deploying(transitional, [transitional, "minecraft:obsidian"]),
        event.recipes.create.pressing(transitional, [transitional])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:reinforced_mechanism")
    // - Manual recipe
    event.shaped("kubejs:reinforced_mechanism", [
        "OCO"
    ], {
        C: "kubejs:kinetic_mechanism",
        O: "minecraft:obsidian"
    }).id("kubejs:reinforced_mechanism_manual_only")

    // -- Gold Machine
    donutCraft(event, "kubejs:gold_machine", "create:railway_casing", "kubejs:reinforced_mechanism")
    // - Usage
    // Create
    goldMachine(event, Item.of("create:controls", 1), "minecraft:lever")
    goldMachine(event, Item.of("create:track_station", 2))
    goldMachine(event, Item.of("create:track_signal", 4))
    goldMachine(event, Item.of("create:schedule", 4))
    goldMachine(event, Item.of("create:track_observer", 2))
    // Simulated/Aeronautics
    goldMachine(event, Item.of("simulated:auger_shaft", 4))
    goldMachine(event, Item.of("simulated:swivel_bearing", 2))
    goldMachine(event, Item.of("aeronautics:propeller_bearing", 2))

})
