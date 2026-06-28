ServerEvents.recipes(event => {
    // phantom membrane replacements
    if (Platform.isLoaded("railways")) {
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of("create:potion", 50, '{Potion:"minecraft:invisibility"}')])
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of("create:potion", 50, '{Potion:"minecraft:long_invisibility"}')])
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of("cofh_core:potion", 50, '{Potion:"minecraft:invisibility"}')])
        event.recipes.create.filling("railways:track_phantom", ["create:track", Fluid.of("cofh_core:potion", 50, '{Potion:"minecraft:long_invisibility"}')])
    }

    if (Platform.isLoaded("moreminecarts")) {
        event.replaceInput({}, "minecraft:phantom_membrane", "thermal:blitz_powder")
        event.recipes.create.crushing([Item.of("moreminecarts:levitation_powder"), Item.of("moreminecarts:levitation_powder", 1).withChance(.5)], "thermal:blitz_powder")
    }
    // alternate double jump recipe
    event.custom({
        "type": "tconstruct:modifier",
        "inputs": [
            { "item": "minecraft:piston" },
            { "item": "tconstruct:sky_slime" },
            { "item": "minecraft:piston" },
            { "item": "thermal:blitz_powder" },
            { "item": "thermal:blitz_powder" }
            // { "item": "trials:wind_charge" },
            // { "item": "trials:wind_charge" }
        ],
        "result": "tconstruct:double_jump",
        "slots": {
            "abilities": 1
        },
        "tools": {
            "tag": "tconstruct:modifiable/armor/boots"
        }
    }).id("tconstruct:tools/modifiers/ability/double_jump")
    // slow fall potion is in startup script potions.js
})

ServerEvents.loaded(event => {
    if (!event.server.persistentData.insomniaDisabled) {
        event.server.runCommandSilent("/gamerule doInsomnia false")
        event.server.persistentData.insomniaDisabled = true;
    }
})
