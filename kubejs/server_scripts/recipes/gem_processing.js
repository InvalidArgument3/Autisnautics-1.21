ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")

    if (hasThermal) {
        let stone = CreateItem.of("minecraft:cobblestone", 0.5)
        let experience = CreateItem.of("create:experience_nugget", 0.75)

        event.recipes.create.crushing([Item.of("thermal:sapphire", 2), CreateItem.of("thermal:sapphire", 0.25), experience, stone], "thermal:sapphire_ore")
        event.recipes.create.crushing([Item.of("thermal:ruby", 2), CreateItem.of("thermal:ruby", 0.25), experience, stone], "thermal:ruby_ore")

        event.recipes.create.milling(Item.of("minecraft:redstone", 4), "thermal:cinnabar").processingTime(700)
        event.recipes.create.crushing(Item.of("minecraft:redstone", 6), "thermal:cinnabar").processingTime(500)
        event.remove({ id: "thermal:machines/pulverizer/pulverizer_cinnabar" })
        event.recipes.thermal.pulverizer(Item.of("minecraft:redstone", 8), "thermal:cinnabar", 0, 10000)
        // A2: cinnabar dust is pointless
        event.remove({input: "thermal:cinnabar_dust"})
        event.remove({output: "thermal:cinnabar_dust"})

        event.recipes.create.milling("thermal:sulfur_dust", "#c:gems/sulfur").processingTime(500)
        event.recipes.create.milling("thermal:niter_dust", "#c:gems/niter").processingTime(500)
        event.recipes.create.milling("thermal:apatite_dust", "#c:gems/apatite").processingTime(500)
    }

    // recompacting gem dusts into their gem form
    let recompact = (id, id2) => {
        event.recipes.create.compacting(id2, [id])
    }
    recompact("#c:dusts/obsidian", "minecraft:obsidian")
    recompact("#c:dusts/diamond", "minecraft:diamond")
    recompact("#c:dusts/emerald", "minecraft:emerald")
    recompact("#c:dusts/lapis", "minecraft:lapis_lazuli")
    if (hasThermal) {
        recompact("#c:dusts/sulfur", "thermal:sulfur")
        recompact("#c:dusts/apatite", "thermal:apatite")
        recompact("#c:dusts/niter", "thermal:niter")
        recompact("#c:dusts/sapphire", "thermal:sapphire")
        recompact("#c:dusts/ruby", "thermal:ruby")
    }
    recompact("#c:dusts/quartz", "minecraft:quartz")
})
