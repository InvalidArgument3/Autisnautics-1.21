StartupEvents.modifyCreativeTab("create:base", event => {
    event.addAfter("create:brass_ingot", ["create:chromatic_compound", "create:refined_radiance", "create:shadow_steel"])
})

StartupEvents.modifyCreativeTab("minecraft:tools_and_utilities", event => {
    event.addAfter("minecraft:recovery_compass", "minecraft:bundle")

    if (Platform.isLoaded("cb_microblock")) {
        event.add("cb_microblock:stone_saw")
        event.add("cb_microblock:iron_saw")
        event.add("cb_microblock:diamond_saw")
    }
})

if (Platform.isLoaded("cb_microblock")) {
    StartupEvents.modifyCreativeTab("minecraft:ingredients", event => {
        event.addAfter("minecraft:stick", ["cb_microblock:stone_rod"])
    })
}

if (Platform.isLoaded("supplementaries")) {
    StartupEvents.modifyCreativeTab("minecraft:functional_blocks", event => {
        // globe may be stripped from the tab by Supplementaries config; don't addAfter it
        event.add("supplementaries:globe_sepia")
    })
}
