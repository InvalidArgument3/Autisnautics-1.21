// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 2a
    let transitional
    // -- Agonized Plasma
    event.recipes.create.mixing(Fluid.of("kubejs:agonized_plasma", 500), ["minecraft:twisting_vines", "minecraft:weeping_vines"]).heated()

    // -- Infernal Mechanisms
    transitional = "kubejs:incomplete_infernal_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:infernal_mechanism",
    ], "create:precision_mechanism", [
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("minecraft:lava", 1000)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("kubejs:agonized_plasma", 250)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("minecraft:lava", 1000)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("kubejs:agonized_plasma", 250)]),
        event.recipes.create.filling(transitional, [transitional, Fluid.sizedIngredientOf("minecraft:lava", 1000)])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:infernal_mechanism")

    // -- Powergrid Conductive Casing = Zinc Casing
    event.remove({ output: "powergrid:conductive_casing"})
    event.shapeless(Item.of("powergrid:conductive_casing", 2), ["create:zinc_ingot", "minecraft:stone"])

    // -- Zinc Machine
    donutCraft(event, "kubejs:zinc_machine", "powergrid:conductive_casing", "kubejs:infernal_mechanism")

    // - Nuclearcraft Usages: replacing lost thermal machines
    // Rock generation
    zincMachine(event, Item.of("nuclearcraftneohaul:cobblestone_generator", 1), "minecraft:piston")
    // Remove redundant cobble gens
    event.remove({ output: "nuclearcraftneohaul:cobblestone_generator_compact"})
    event.remove({ output: "nuclearcraftneohaul:cobblestone_generator_dense"})
    event.remove({ id: "nuclearcraftneohaul:collector/from_cobblestone_generator_compact_rate"})
    event.remove({ id: "nuclearcraftneohaul:collector/from_cobblestone_generator_dense_rate"})
    // Nullification
    zincMachine(event, Item.of("nuclearcraftneohaul:universal_bin", 1), "minecraft:cactus")

    // - Electricity Usages
    // powergrid
    // small parts, connectors, etc
    let powergridSmall = ["wire_connector", "cord_junction", "lv_switch", "lv_button", "light_fixture", "socket", "device_connector", "fuse_holder", "alarm_bell", "thermometer"]
    powergridSmall.forEach(widget => {
        event.remove({ output: "powergrid:" + widget })
        zincMachine(event, Item.of("powergrid:" + widget, 8))
    })
    // medium parts
    let powergridMedium = ["mv_switch", "spark_gap", "heavy_wire_connector", "generator_housing", "vertical_generator_housing"]
    powergridMedium.forEach(widget => {
        event.remove({ output: "powergrid:" + widget })
        zincMachine(event, Item.of("powergrid:" + widget, 4))
    })
    // larger parts
    let powergridLarge = ["current_gauge", "voltage_gauge", "electric_motor", "generator_commutator", "generator_vertical_commutator"]
    powergridLarge.forEach(widget => {
        event.remove({ output: "powergrid:" + widget })
        zincMachine(event, Item.of("powergrid:" + widget, 1))
    })
    zincMachine(event, Item.of("powergrid:transformer_core", 4), Ingredient.of("#c:plates/iron"))
    zincMachine(event, Item.of("powergrid:variac", 1), "powergrid:copper_coil")
    zincMachine(event, Item.of("powergrid:rheostat", 1), "powergrid:resistive_coil")
    zincMachine(event, Item.of("powergrid:carbon_pile_coil", 1), Ingredient.of("#c:plates/zinc"))
    zincMachine(event, Item.of("powergrid:plotter", 1), "minecraft:paper")
    zincMachine(event, Item.of("powergrid:hv_switch", 1), Ingredient.of("#c:rods/iron"))
    zincMachine(event, Item.of("powergrid:hv_breaker", 1), "minecraft:lever")
    zincMachine(event, Item.of("powergrid:contactor", 1), "minecraft:iron_ingot")
    zincMachine(event, Item.of("powergrid:power_resistor", 1), "minecraft:coal_block")
    // wheeeze
    // machines
    zincMachine(event, Item.of("powergrid:electromagnet", 1), "minecraft:copper_block")
    zincMachine(event, Item.of("powergrid:punch_card_reader", 1), "powergrid:punch_card")
    zincMachine(event, Item.of("powergrid:constant_speed_motor", 1), "create:rotation_speed_controller")
    zincMachine(event, Item.of("powergrid:servo", 1), "create:shaft")
    zincMachine(event, Item.of("powergrid:electric_fan", 1), "create:encased_fan")
    zincMachine(event, Item.of("powergrid:basin_heater", 1), "create:blaze_burner")
    zincMachine(event, Item.of("powergrid:heating_coil", 2)) // technically useless because of fan catalysts
    zincMachine(event, Item.of("powergrid:crt", 1), "minecraft:glowstone_dust")
    // replacing gizmos
    event.remove({ output: "powergrid:electrical_gizmo"})
    event.remove({ id: "powergrid:crafting/portable_battery"})
    event.shapeless(Item.of("powergrid:portable_battery", 1), ["create:copper_backtank", "powergrid:device_connector", "powergrid:battery"])
    event.remove({ id: "powergrid:crafting/multimeter"})
    event.shapeless(Item.of("powergrid:multimeter", 1), ["powergrid:voltage_gauge", Ingredient.of("#c:wires/copper"), "powergrid:current_gauge"])
    event.replaceInput({ id: "powergrid:mechanical_crafting/electrozapper" }, "powergrid:electrical_gizmo", "createaddition:tesla_coil")
    event.replaceInput({ id: "powergrid:mechanical_crafting/electrobaton" }, "powergrid:electrical_gizmo", "createaddition:tesla_coil")


    // createaddition
    event.remove({ output: "createaddition:capacitor"})
    event.remove({ output: "createaddition:alternator"}) // use powergrid :^)
    zincMachine(event, Item.of("createaddition:tesla_coil", 1), "minecraft:lightning_rod")
    zincMachine(event, Item.of("createaddition:portable_energy_interface", 2))
    zincMachine(event, Item.of("createaddition:electric_motor", 1), "createaddition:copper_spool")
    zincMachine(event, Item.of("createaddition:connector", 8))
    zincMachine(event, Item.of("createaddition:redstone_relay", 8))
    zincMachine(event, Item.of("createaddition:large_connector", 4))
    zincMachine(event, Item.of("createaddition:small_light_connector", 8), Ingredient.of("#c:wires/iron"))

    // - Batteries and Accumulators
    // unify sulfuric acid, prefer nuclearcraft's
    // remove powergrid:acid and its recipes
    event.remove({ id: "powergrid:mixing/acid"})
    event.remove({ id: "powergrid:sequenced_assembly/battery"})
    event.remove({ id: "powergrid:mixing/etched_circuit_board"})
    // add nuclearcraft sulfuric acid low tech recipe - 1:250 instead of 1:1000 and uses blaze powder
    event.recipes.create.mixing(
        Fluid.of("nuclearcraftneohaul:sulfuric_acid", 250),
        [Ingredient.of("#c:dusts/sulfur"), "minecraft:blaze_powder", Fluid.sizedIngredientOf("minecraft:water", 500)]
    ).heated()
    // powergrid battery: because of device connectors, it's basically just worse than an accumulator
    event.recipes.create.filling("powergrid:battery", ["kubejs:zinc_machine", Fluid.sizedIngredientOf("nuclearcraftneohaul:sulfuric_acid", 1000)])
    // accumulator: the RF battery
    event.remove({ id: "createaddition:crafting/modular_accumulator"})
    event.recipes.create.mechanical_crafting("createaddition:modular_accumulator", [
        "CCCC",
        "PBBP",
        "PBBP",
        "CCCC"
    ], {
        B: "powergrid:battery",
        C: "create:brass_casing",
        P: "create:brass_sheet"
    })
})
