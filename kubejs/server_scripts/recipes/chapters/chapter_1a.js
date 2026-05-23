// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 1A
    let transitional
    // -- Rubber production
    // - Raw rubber
    // Compacting 4 flowers or vines in a basin with water
    event.recipes.create.compacting("kubejs:raw_rubber", [SizedIngredient.of("minecraft:vine", 4), Fluid.sizedIngredientOf('minecraft:water', 1000)])
    event.recipes.create.compacting("kubejs:raw_rubber", [SizedIngredient.of("#minecraft:flowers", 4), Fluid.sizedIngredientOf('minecraft:water', 1000)])
    // Or bulk wash minecraft:resin, obtained by scarring trees with a knife
    event.recipes.create.splashing("kubejs:raw_rubber", "minecraft:resin_clump")
    // - Rubber
    event.smelting("kubejs:rubber", "kubejs:raw_rubber").xp(0).cookingTime(200)
    event.blasting("kubejs:rubber", "kubejs:raw_rubber").xp(0).cookingTime(100)
    // - Rubber usecases
    // Belts - 3x output
    event.remove({ output: "create:belt_connector" })
    event.shaped(Item.of("create:belt_connector", 3), [
        "SSS",
        "SSS"
    ], {
        S: "kubejs:rubber"
    })
    // Replacing dried kelp in several recipes
    let kelpToRubber = (recipe) => {
        event.replaceInput({ id: recipe }, "minecraft:dried_kelp", "kubejs:rubber")
    }
    kelpToRubber("createdieselgenerators:crafting/pumpjack_head")
    // kelpToRubber("createdieselgenerators:crafting/kelp_handle") // Zinc Machines
    kelpToRubber("createornithopterglider:rope")
    kelpToRubber("offroad:small_tire")
    kelpToRubber("offroad:tire")
    kelpToRubber("powergrid:crafting/insulated_copper_wire")
    kelpToRubber("powergrid:crafting/copper_cord")

    // -- Sealed Mechanisms
    transitional = "kubejs:incomplete_kinetic_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:sealed_mechanism",
    ], "kubejs:kinetic_mechanism", [
        event.recipes.create.deploying(transitional, [transitional, "kubejs:rubber"]),
        event.recipes.create.deploying(transitional, [transitional, "kubejs:rubber"]),
        event.recipes.create.pressing(transitional, [transitional])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:sealed_mechanism")
    // Manual recipe
    event.shaped("kubejs:sealed_mechanism", [
        "RMR"
    ], {
        M: "kubejs:kinetic_mechanism",
        R: "kubejs:rubber"
    }).id("kubejs:sealed_mechanism_manual_only")

    // -- Copper Machine
    donutCraft(event, "kubejs:copper_machine", "create:copper_casing", "kubejs:sealed_mechanism")
    // - Usage
    // Create
    copperMachine(event, Item.of("create:copper_backtank", 1), "minecraft:copper_block")
    copperMachine(event, Item.of("create:spout", 1), "minecraft:hopper")
    copperMachine(event, Item.of("create:item_drain", 1), "minecraft:iron_bars")
    copperMachine(event, Item.of("create:hose_pulley", 1))
    copperMachine(event, Item.of("create:portable_fluid_interface", 2))
    copperMachine(event, Item.of("create:smart_fluid_pipe", 2))
    copperMachine(event, Item.of("create:potato_cannon", 1), "create:fluid_pipe")
    // Simulated/Aeronautics
    copperMachine(event, Item.of("simulated:plunger_launcher", 1), "minecraft:slime_ball")
    copperMachine(event, Item.of("aeronautics:mounted_potato_cannon", 1), "create:potato_cannon")
    // blazinghot
    copperMachine(event, Item.of("blazinghot:casting_depot", 1))
    // Create Dragons Plus
    if (Platform.isLoaded("create_dragons_plus")) { copperMachine(event, Item.of("create_dragons_plus:fluid_hatch", 1), "create:chute") }
    // Create Diesel Generators
    // if (Platform.isLoaded("createdieselgenerators")) { copperMachine(event, Item.of("createdieselgenerators:pumpjack_hole", 1)) } // Zinc Machines
    // sliceanddice
    if (Platform.isLoaded("sliceanddice")) { copperMachine(event, Item.of("sliceanddice:sprinkler", 3)) }
    // Water-generating blocks:
    // cookingforblockheads:sink
    if (Platform.isLoaded("cookingforblockheads")) { copperMachine(event, Item.of("cookingforblockheads:sink", 1), "minecraft:bucket") }
    // functionalstorage:water_generator_upgrade
    if (Platform.isLoaded("functionalstorage")) { copperMachine(event, Item.of("functionalstorage:water_generator_upgrade", 1), "minecraft:glass_bottle") }
})
