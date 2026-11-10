// priority: 1

wood_types.push("architects_palette:twisted")

ServerEvents.recipes(event => {
    event.remove({ id: "architects_palette:smelting/charcoal_block_from_logs_that_burn_smoking" })
    event.remove({ id: "architects_palette:charcoal_block" })
    event.stonecutting("architects_palette:charcoal_block", "minecraft:charcoal")
    donutCraft(event, Item.of("architects_palette:plating_block", 8), "create:iron_sheet", "minecraft:stone")
    // The 1.21 port is Modern Foundry (mod id modernfoundry), not tconstruct.
    if (Platform.isLoaded("modernfoundry") && Item.exists("modernfoundry:necrotic_bone")) {
        event.replaceInput({ id: "architects_palette:wither_lamp" }, "architects_palette:withered_bone", "modernfoundry:necrotic_bone")
        event.replaceInput({ id: "architects_palette:withered_bone_block" }, "architects_palette:withered_bone", "modernfoundry:necrotic_bone")
    }
    event.remove({ id: "architects_palette:withered_bone" })
})
