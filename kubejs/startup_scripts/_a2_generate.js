StartupEvents.registry("item", event => {
    // 1.21: thermal rubber replacement
    event.create("rubber").texture("a2:item/rubber").displayName("Rubber")
    event.create("raw_rubber").texture("a2:item/raw_rubber").displayName("Raw Rubber")
    // 1.21: snow golem spirit cycle
    event.create("haunted_rind").texture("alexsmobs:tarantula_hawk_wing_fragment").displayName("Haunted Rind")// placeholder texture
    event.create("haunted_dust").texture("cabin:item/ground_slimy_fern").color(0, 0xAC2EFC).displayName("Haunted Dust")
})

StartupEvents.registry("fluid", event => {
    // 1.21: tconstruct liquid soul replacement
    // placeholder texture
    event.create("liquid_soul").displayName("Liquid Soul").stillTexture("oritech:block/fluid/fluid_roiling_plasma").flowingTexture("oritech:block/fluid/fluid_roiling_plasma")// placeholder texture
})
