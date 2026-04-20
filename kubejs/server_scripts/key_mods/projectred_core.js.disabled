// priority: 1
ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")

    // So many recipes that we don't want
    event.remove({ mod: "projectred_core" })
    // red alloy ingot
    if (hasThermal) {
        event.recipes.create.compacting([Item.of("projectred_core:red_ingot")], [Fluid.of("thermal:redstone", 250).toJson(), Item.of("minecraft:copper_ingot")])
        event.recipes.thermal.smelter("projectred_core:red_ingot", ["minecraft:copper_ingot", "minecraft:redstone"])
    }
    event.recipes.create.compacting([Item.of("projectred_core:red_ingot")], [Item.of("minecraft:redstone", 4), Item.of("minecraft:copper_ingot")] )
    // recreate the circuit plate smelting recipes
    event.smelting(Item.of("projectred_core:plate", 2), "minecraft:smooth_stone")
    // recreate illumar recipes
    colours.forEach(c=>{
        event.shapeless(`projectred_core:${c}_illumar`, ["minecraft:glowstone_dust", "minecraft:glowstone_dust", "#forge:dyes/" + c, "#forge:dyes/" + c])
    })
    // recreate screwdriver recipe
    event.shaped("projectred_core:screwdriver", [
        "I  ",
        " IB",
        " BI"
    ], {
        I: "#forge:ingots/iron",
        B: "#forge:dyes/blue"
    })
    // Platformed plate
    // The projectred transmission script replaces red ingot with red alloy wire
    event.shapeless("projectred_core:platformed_plate", [
        "projectred_core:plate",
        Platform.isLoaded("projectred_transmission") ? "projectred_transmission:red_alloy_wire" : "projectred_core:red_ingot",
        "create:andesite_alloy"
    ]).id("kubejs:platformed_plate")

    // Circuit cutting. Projectred Transmission circuit recipes are added in the circuit script in the mods folder
    let circuit = (id, override) => {
        console.log(`Attempting to create circuit for: ${id}`)
        let itemStack = Item.of(id, 1)
        console.log(`ItemStack created: ${itemStack}, isEmpty: ${itemStack.isEmpty()}`)

        if (override)
            event.remove({ output: id })
        event.stonecutting(itemStack, "projectred_core:platformed_plate")
    }

    circuit("minecraft:repeater", false)
    circuit("minecraft:comparator", false)
    // Commenting out Create items that might not exist or are causing issues
    // circuit("create:pulse_repeater", true)
    // circuit("create:pulse_extender", true)
    // circuit("create:pulse_timer", true)
    // circuit("create:powered_latch", true)
    // circuit("create:powered_toggle_latch", true)
})
