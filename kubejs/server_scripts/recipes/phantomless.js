ServerEvents.recipes(event => {
    // phantom membrane replacements
    if (Platform.isLoaded("railways")) {
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of('create:potion[create:potion_fluid_bottle_type="regular",minecraft:potion_contents={potion:"minecraft:invisibility"}]', 50)])
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of('create:potion[create:potion_fluid_bottle_type="regular",minecraft:potion_contents={potion:"minecraft:long_invisibility"}]', 50)])
    }

    if (Platform.isLoaded("moreminecarts") && Platform.isLoaded("thermal") && Item.exists("thermal:blitz_powder")) {
        event.replaceInput({}, "minecraft:phantom_membrane", "thermal:blitz_powder")
        event.recipes.create.crushing([Item.of("moreminecarts:levitation_powder"), CreateItem.of("moreminecarts:levitation_powder", 0.5)], "thermal:blitz_powder")
    }
    // Modern Foundry already registers modernfoundry:double_jump with phantom membranes.
    // The old alternate used tconstruct:modifier and thermal:blitz_powder. Neither the
    // recipe type nor the powder is in this pack, so that recipe is not submitted.
    // slow fall potion is in startup script potions.js
})

ServerEvents.loaded(event => {
    if (!event.server.persistentData.insomniaDisabled) {
        event.server.runCommandSilent("/gamerule doInsomnia false")
        event.server.persistentData.insomniaDisabled = true;
    }
})
