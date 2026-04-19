if(Platform.isLoaded("create_enchantment_industry")) {
    ServerEvents.recipes(event => {
        zincMachine(event, Item.of("create_enchantment_industry:disenchanter", 1), "#create:sandpaper")
        zincMachine(event, Item.of("create_enchantment_industry:printer", 1), "#forge:storage_blocks/lapis")

        event.remove({ id:"create_enchantment_industry:mixing/hyper_experience"})
        event.recipes.create.mixing(Fluid.of("create_enchantment_industry:hyper_experience", 10), [
            "#forge:dusts/enderium",
            "minecraft:lapis_lazuli",
            "minecraft:glow_ink_sac",
            Fluid.of("create_enchantment_industry:experience", 100).toJson()
        ]).superheated()

        event.replaceInput( {id: "create_enchantment_industry:crafting/enchanting_guide"}, "create:sturdy_sheet", "create:schedule" )
    })
}
