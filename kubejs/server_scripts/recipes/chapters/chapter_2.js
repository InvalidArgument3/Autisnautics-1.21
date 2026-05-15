// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 2
    let transitional
    // -- Remove unneeded default recipes
    // - sky stone dust
    event.remove({ id: "create:milling/compat/ae2/sky_stone_block" })
    // - Electron tubes
    event.remove({ id: "create:crafting/materials/electron_tube" })
    // - Rose quartz, including alternate mod sanding options
    event.remove({ output: "create:polished_rose_quartz" })
    event.remove({ id: "create:sandpaper_polishing/rose_quartz" })
    // - Default certus crystal growing
    event.remove({ id: "ae2:transform/certus_quartz_crystals" })
    // - Create precision mechanisms
    event.remove({ id: "create:sequenced_assembly/precision_mechanism" })
    // - Simulated gyroscopic mechanisms, replaced with brass machines
    event.remove({ id: "simulated:sequenced_assembly/gyroscopic_mechanism" })
    
    // -- Sky stone dust: infinimilling
    event.recipes.create.milling(["ae2:sky_dust", "ae2:sky_stone_block"], "ae2:sky_stone_block").processingTime(1000)
    
    // -- Certus Quartz farming
    // - Crafting seed from dust + sand
    event.shapeless("2x kubejs:certus_crystal_seed", ["ae2:certus_quartz_dust", "#minecraft:sand"])
    // - Superior Mechanical Crafting version: splitting a crystal into two seeds
    event.recipes.create.mechanical_crafting(Item.of("kubejs:certus_crystal_seed", 2), ["A"], { A: "ae2:certus_quartz_crystal" })
    event.recipes.create.mechanical_crafting(Item.of("kubejs:fluix_crystal_seed", 2), ["A"], { A: "ae2:fluix_crystal" })
    // - Growth loop
    let grow = (from, via, to) => {
        event.recipes.create.sequenced_assembly([
            to,
        ], from, [
            event.recipes.create.filling(via, [via, Fluid.sizedIngredientOf('minecraft:water', 500)])
        ]).transitionalItem(via)
            .loops(4)
            .id("kubejs:grow_" + to.split(":")[1])
    }
    // Certus
    grow("kubejs:certus_crystal_seed", "kubejs:growing_certus_seed", "kubejs:tiny_certus_crystal")
    grow("kubejs:tiny_certus_crystal", "kubejs:growing_tiny_certus_crystal", "kubejs:small_certus_crystal")
    grow("kubejs:small_certus_crystal", "kubejs:growing_small_certus_crystal", "ae2:certus_quartz_crystal")
    // Fluix
    grow("kubejs:fluix_crystal_seed", "kubejs:growing_fluix_seed", "kubejs:tiny_fluix_crystal")
    grow("kubejs:tiny_fluix_crystal", "kubejs:growing_tiny_fluix_crystal", "kubejs:small_fluix_crystal")
    grow("kubejs:small_fluix_crystal", "kubejs:growing_small_fluix_crystal", "ae2:fluix_crystal")

    // -- Volatile Sky Solution
    event.recipes.create.mixing(
        Fluid.of('kubejs:volatile_sky_solution', 500), 
        [SizedIngredient.of("ae2:sky_dust", 4), Fluid.sizedIngredientOf('minecraft:water', 500)]
    )
    
    // -- Rising Sky Solution
    event.recipes.create.mixing(
        ["ae2:certus_quartz_crystal", Fluid.of('kubejs:rising_sky_solution', 250)], 
        ["ae2:charged_certus_quartz_crystal", Fluid.sizedIngredientOf('kubejs:volatile_sky_solution', 250)]
    )
    
    // -- Polished Rose Quartz
    event.recipes.create.mixing(
        "create:polished_rose_quartz",
        ["ae2:certus_quartz_crystal", Fluid.sizedIngredientOf('kubejs:rising_sky_solution', 250)]
    )
    
    // -- Electron tubes
    event.recipes.create.filling(
        "create:electron_tube", 
        ["create:polished_rose_quartz", Fluid.sizedIngredientOf('blazinghot:molten_iron', 10)]
    )

    // -- Precision mechanisms
    transitional = "create:incomplete_precision_mechanism"
    event.recipes.create.sequenced_assembly([
        "create:precision_mechanism",
    ], "kubejs:kinetic_mechanism", [
        event.recipes.create.deploying(transitional, [transitional, "create:electron_tube"]),
        event.recipes.create.deploying(transitional, [transitional, "create:electron_tube"]),
        event.recipes.create.deploying(transitional, [transitional, "createdieselgenerators:wire_cutters"])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:precision_mechanism")

    // -- Brass Machine
    donutCraft(event, "kubejs:brass_machine", "create:brass_casing", "create:precision_mechanism")
    // - Usages
    // Create
    brassMachine(event, Item.of("create:mechanical_crafter", 3), "minecraft:crafting_table")
    brassMachine(event, Item.of("create:sequenced_gearshift", 2))
    brassMachine(event, Item.of("create:steam_engine", 1))
    brassMachine(event, Item.of("create:rotation_speed_controller", 1))
    brassMachine(event, Item.of("create:mechanical_arm", 1))
    brassMachine(event, Item.of("create:stockpile_switch", 2))
    brassMachine(event, Item.of("create:content_observer", 2))
    brassMachine(event, Item.of("create:brass_funnel", 4))
    brassMachine(event, Item.of("create:brass_tunnel", 4))
    brassMachine(event, Item.of("create:smart_chute", 4))
    brassMachine(event, Item.of("create:clockwork_bearing", 2))
    brassMachine(event, Item.of("create:elevator_pulley", 1))
    brassMachine(event, Item.of("create:steam_whistle", 4))
    // items formerly using create:transmitter
    brassMachine(event, Item.of("create:display_link", 1), "minecraft:redstone_torch")
    // Simulated/Aeronautics 
    brassMachine(event, Item.of("aeronautics:steam_vent", 1))
    brassMachine(event, Item.of("simulated:navigation_table", 1), "minecraft:map")
    brassMachine(event, Item.of("simulated:docking_connector", 2), "create:chute")
    brassMachine(event, Item.of("simulated:analog_transmission", 1))
    brassMachine(event, Item.of("simulated:linked_typewriter", 1), Ingredient.of("#minecraft:buttons"))
    // items formerly using create:transmitter
    brassMachine(event, Item.of("simulated:directional_linked_receiver", 1), Ingredient.of("#c:plates/iron"))
    brassMachine(event, Item.of("simulated:modulating_linked_receiver", 1), Ingredient.of("#c:plates/gold"))
    // items formerly using Gyroscopic Mechanism
    brassMachine(event, Item.of("simulated:gimbal_sensor", 1), "minecraft:compass")
    brassMachine(event, Item.of("aeronautics:gyroscopic_propeller_bearing", 2))
    if (Platform.isLoaded("immersive_aircraft")) {
        brassMachine(event, Item.of("aeronautics:smart_propeller", 1), "immersive_aircraft:propeller")
    }
    else {
        brassMachine(event, Item.of("aeronautics:smart_propeller", 1), "create:propeller")
    }
    // Create Connected
    if(Platform.isLoaded("create_connected")) {
        brassMachine(event, Item.of("create_connected:empty_fan_catalyst", 1), "minecraft:iron_bars")
        brassMachine(event, Item.of("create_connected:inventory_access_port", 2))
        brassMachine(event, Item.of("create_connected:inventory_bridge", 2))
    }
    // Create Hypertube
    if(Platform.isLoaded("create_hypertube")) {
        brassMachine(event, Item.of("create_hypertube:hypertube_entrance", 1), "kubejs:rubber")
        brassMachine(event, Item.of("create_hypertube:hypertube_accelerator", 2), "create:belt_connector")
        brassMachine(event, Item.of("create_hypertube:hypertube", 8), Ingredient.of("#c:glass_panes"))
        brassMachine(event, Item.of("create_hypertube:redstone_detector_tube_attachment", 2), "minecraft:redstone_torch")
        brassMachine(event, Item.of("create_hypertube:tube_scanner_attachment", 2), "minecraft:comparator")
    }
    // Create Crafts & Additions
    if(Platform.isLoaded("createaddition")) {
        brassMachine(event, Item.of("createaddition:digital_adapter", 1), Ingredient.of("#computercraft:wired_modem"))
    }
    // Numismatics
    if (Platform.isLoaded("numismatics")) {
        brassMachine(event, Item.of("numismatics:brass_depositor", 1))
        brassMachine(event, Item.of("numismatics:vendor", 1), "create:depot")
    }
    // Immersive Aircraft - airship, warship, biplane, seaplane
    if (Platform.isLoaded("immersive_aircraft")) {
        brassMachine(event, Item.of("immersive_aircraft:engine", 1), "minecraft:blast_furnace")
    }
})