StartupEvents.registry("item", event => {
    // 1.21: thermal rubber replacement
    event.create("rubber").texture("a2:item/rubber").displayName("Rubber")
    event.create("raw_rubber").texture("a2:item/raw_rubber").displayName("Raw Rubber")
    // 1.21: snow golem spirit cycle
    event.create("haunted_rind").texture("alexsmobs:tarantula_hawk_wing_fragment").displayName("Haunted Rind")// placeholder texture
    event.create("haunted_dust").texture("cabin:item/ground_slimy_fern").color(0, 0xAC2EFC).displayName("Haunted Dust")
    // 1.21: new alchemy oil item
    event.create("crystallized_oil").texture("a2:item/crystallized_oil").displayName("Crystallized Oil")
})

StartupEvents.registry("fluid", event => {
    // 1.21: tconstruct liquid soul replacement
    // placeholder texture
    event.create("liquid_soul").displayName("Liquid Soul").stillTexture("oritech:block/fluid/fluid_roiling_plasma").flowingTexture("oritech:block/fluid/fluid_roiling_plasma")
    // 1.21: flux waste
    event.create("flux_goo").displayName("Flux Goo").stillTexture("alexscaves:block/purple_soda_still").flowingTexture("alexscaves:block/purple_soda_flowing")
})

StartupEvents.registry("block", event => {
    // 1.21: lidless jar transitional item
    event.create("kubejs:incomplete_jar").modelGenerator(m => { m.parent("cabin:block/lidless_substrate")})
        .soundType("glass")
        .hardness(0.1)
        .displayName("Lidless Jar")
        .renderType("cutout")
        .mapColor("NONE")
        .waterlogged()
        .box(.25, 0, .25, .75, 14.0 / 16.0, .75, false)
})