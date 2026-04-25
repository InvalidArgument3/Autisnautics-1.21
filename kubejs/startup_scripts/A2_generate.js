StartupEvents.registry("item", event => {
    // 1.21: thermal rubber replacement
    event.create("rubber").texture("a2:item/rubber").displayName("Rubber")
    event.create("raw_rubber").texture("a2:item/raw_rubber").displayName("Raw Rubber")
})

StartupEvents.registry("fluid", event => {
    // 1.21: tconstruct liquid soul replacement
    // placeholder texture
    event.create("liquid_soul").displayName("Liquid Soul").stillTexture("oritech:block/fluid/fluid_roiling_plasma").flowingTexture("oritech:block/fluid/fluid_roiling_plasma")
})
