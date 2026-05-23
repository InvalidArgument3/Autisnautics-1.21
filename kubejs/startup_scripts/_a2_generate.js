StartupEvents.registry("item", event => {
    // 1.21: thermal rubber replacement
    event.create("rubber").texture("a2:item/rubber").displayName("Rubber")
    event.create("raw_rubber").texture("a2:item/raw_rubber").displayName("Raw Rubber")
    // 1.21: snow golem spirit cycle
    event.create("haunted_rind").texture("alexsmobs:tarantula_hawk_wing_fragment").displayName("Haunted Rind")// placeholder texture
    event.create("haunted_dust").texture("cabin:item/ground_slimy_fern").color(0, 0xAC2EFC).displayName("Haunted Dust")
    // 1.21: new alchemy oil item
    event.create("crystallized_oil").texture("a2:item/crystallized_oil").displayName("Crystallized Oil")
    // Chapter 1: saw blade and drill head
    event.create("saw_blade").texture("a2:item/saw_blade").displayName("Saw Blade")
    event.create("drill_head").texture("a2:item/drill_head").displayName("Drill Head")
    // Chapter 2c: coke cake, IE compatibility
    event.create("coke_cake").texture("a2:item/coke_cake").displayName("Coke Cake")
})

StartupEvents.registry("fluid", event => {
    // 1.21: tconstruct liquid soul replacement
    // placeholder texture
    event.create("agonized_plasma").displayName("Agonized Plasma").stillTexture("blazinghot:fluid/molten_sturdy_alloy_still").flowingTexture("blazinghot:fluid/molten_sturdy_alloy_flow")
    // 1.21: register new fluid for chapter 2 (temp texture)
    event.create("rising_sky_solution").displayName("Rising Sky Solution").stillTexture("blazinghot:fluid/molten_sturdy_alloy_still").flowingTexture("blazinghot:fluid/molten_sturdy_alloy_flow")
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
