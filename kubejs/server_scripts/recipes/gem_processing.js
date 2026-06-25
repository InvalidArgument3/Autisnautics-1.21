ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")

    let stone = chanceItem("minecraft:cobblestone", 0.5)
    let experience = chanceItem("create:experience_nugget", 0.75)

    if (Item.exists("thermal:sapphire_ore")) {
        event.recipes.create.crushing([Item.of("thermal:sapphire", 2), chanceItem(Item.of("thermal:sapphire", 1), 0.25), experience, stone], "thermal:sapphire_ore")
    }
    if (Item.exists("thermal:ruby_ore")) {
        event.recipes.create.crushing([Item.of("thermal:ruby", 2), chanceItem(Item.of("thermal:ruby", 1), 0.25), experience, stone], "thermal:ruby_ore")
    }

    event.recipes.create.milling(Item.of("minecraft:redstone", 4), "kubejs:substrate_cinnabar").processingTime(700)
    event.recipes.create.crushing(Item.of("minecraft:redstone", 6), "kubejs:substrate_cinnabar").processingTime(500)
    if (hasThermal) {
        event.remove({ id: "thermal:machines/pulverizer/pulverizer_cinnabar" })
        event.recipes.thermal.pulverizer(Item.of("minecraft:redstone", 8), "kubejs:substrate_cinnabar", 0, 10000)
    }

    if (Item.exists("scguns:sulfur")) {
        event.recipes.create.milling("immersiveengineering:dust_sulfur", "scguns:sulfur").processingTime(500)
    }

    let recompact = (dustTag, gem) => {
        if (typeof gem === "string" && gem.indexOf(":") >= 0 && Item.exists(gem)) {
            event.recipes.create.compacting(gem, [dustTag])
            return
        }
        const preferred = getPreferredItemFromTag(gem)
        if (preferred !== "minecraft:air" && preferred !== "minecraft:barrier") {
            event.recipes.create.compacting(preferred, [dustTag])
        }
    }
    recompact("#forge:dusts/obsidian", "minecraft:obsidian")
    recompact("#forge:dusts/diamond", "minecraft:diamond")
    recompact("#forge:dusts/emerald", "minecraft:emerald")
    recompact("#forge:dusts/lapis", "minecraft:lapis_lazuli")
    recompact("#forge:dusts/sulfur", "immersiveengineering:dust_sulfur")
    recompact("#forge:dusts/niter", "immersiveengineering:dust_saltpeter")
    recompact("#forge:dusts/quartz", "minecraft:quartz")
})
