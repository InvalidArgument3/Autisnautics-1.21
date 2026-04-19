StartupEvents.modifyCreativeTab("create:base", event => {
    event.addAfter("create:brass_ingot", ["create:chromatic_compound", "create:refined_radiance", "create:shadow_steel"])
})

StartupEvents.modifyCreativeTab("minecraft:tools_and_utilities", event => {
    event.addAfter("minecraft:recovery_compass", "minecraft:bundle")

    event.add("cb_microblock:stone_saw")
    event.add("cb_microblock:iron_saw")
    event.add("cb_microblock:diamond_saw")
})

StartupEvents.modifyCreativeTab("minecraft:ingredients", event => {
    event.addAfter("minecraft:stick", ["cb_microblock:stone_rod"])
})

// Does not work
// StartupEvents.modifyCreativeTab("cb_microblock:microblocks", event => {
//     event.remove({ mod: "cb_microblock"})
//     event.removeDisplay({ mod: "cb_microblock"})
//     event.removeSearch({ mod: "cb_microblock"})
// })


StartupEvents.modifyCreativeTab('minecraft:functional_blocks', event => {
    // necessary because of dumb supplementaries config behavior
    event.addAfter('supplementaries:globe', 'supplementaries:globe_sepia')
})
