// priority: 5
// Core Create mechanisms, Chromatic Compound, Refined Radiance, and Shadow Steel.

ServerEvents.recipes(event => {
    // --- Kinetic Mechanism ---
    const incKinetic = "kubejs:incomplete_kinetic_mechanism"
    event.recipes.create.sequenced_assembly(
        ["kubejs:kinetic_mechanism"],
        Ingredient.of("#minecraft:wooden_slabs"),
        [
            event.recipes.create.deploying(incKinetic, [incKinetic, "create:andesite_alloy"]),
            event.recipes.create.deploying(incKinetic, [incKinetic, "create:andesite_alloy"]),
            event.recipes.create.deploying(incKinetic, [incKinetic, "createdieselgenerators:hammer"])
        ]
    ).transitionalItem(incKinetic).loops(1).id("kubejs:kinetic_mechanism")

    event.shapeless("kubejs:kinetic_mechanism", [
        "createdieselgenerators:hammer",
        "create:cogwheel",
        "create:andesite_alloy",
        "#c:stripped_logs"
    ]).id("kubejs:kinetic_mechanism_manual")

    // --- Sealed Mechanism ---
    const incSealed = "kubejs:incomplete_sealed_mechanism"
    event.recipes.create.sequenced_assembly(
        ["kubejs:sealed_mechanism"],
        "kubejs:kinetic_mechanism",
        [
            event.recipes.create.deploying(incSealed, [incSealed, "kubejs:rubber"]),
            event.recipes.create.deploying(incSealed, [incSealed, "create:copper_sheet"])
        ]
    ).transitionalItem(incSealed).loops(1).id("kubejs:sealed_mechanism")

    // --- Precision Mechanism (Create default kept, plus kinetic_mechanism path) ---
    const incPrecision = "create:incomplete_precision_mechanism"
    event.recipes.create.sequenced_assembly(
        ["create:precision_mechanism"],
        "kubejs:kinetic_mechanism",
        [
            event.recipes.create.deploying(incPrecision, [incPrecision, "create:electron_tube"]),
            event.recipes.create.deploying(incPrecision, [incPrecision, "create:electron_tube"]),
            event.recipes.create.deploying(incPrecision, [incPrecision, "create:brass_sheet"])
        ]
    ).transitionalItem(incPrecision).loops(1).id("kubejs:precision_mechanism")

    // --- Chromatic Compound ---
    event.recipes.create.mixing("create:chromatic_compound", [
        "create:polished_rose_quartz",
        "minecraft:glowstone_dust",
        "minecraft:glowstone_dust",
        "minecraft:glowstone_dust",
        "create:powdered_obsidian",
        "create:powdered_obsidian",
        "create:powdered_obsidian"
    ]).superheated().id("kubejs:chromatic_compound")

    // --- Refined Radiance ---
    if (Fluid.exists("supplementaries:lumisene")) {
        event.recipes.create.filling("create:refined_radiance", [
            "create:chromatic_compound",
            Fluid.of("supplementaries:lumisene", 250)
        ]).id("kubejs:refined_radiance")
    } else {
        event.recipes.create.mixing("create:refined_radiance", [
            "create:chromatic_compound",
            "minecraft:glowstone",
            "minecraft:blaze_powder"
        ]).superheated().id("kubejs:refined_radiance")
    }

    // --- Shadow Steel ---
    event.recipes.create.mixing("create:shadow_steel", [
        "create:chromatic_compound",
        "minecraft:crying_obsidian",
        "minecraft:obsidian",
        "minecraft:echo_shard"
    ]).superheated().id("kubejs:shadow_steel")
})
