if(Platform.isLoaded("create_enchantment_industry")) {
    ServerEvents.recipes(event => {
        // 2.5.4 has printer, not disenchanter, and no hyper_experience fluid.
        zincMachine(event, Item.of("create_enchantment_industry:printer", 1), "#forge:storage_blocks/lapis")

        event.replaceInput( {id: "create_enchantment_industry:crafting/enchanting_guide"}, "create:sturdy_sheet", "create:schedule" )
    })
}
