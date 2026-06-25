if(Platform.isLoaded("create_enchantment_industry")) {
    ServerEvents.recipes(event => {
        if (Item.exists("create_enchantment_industry:disenchanter")) {
            zincMachine(event, Item.of("create_enchantment_industry:disenchanter", 1), "#create:sandpaper")
        }
        if (Item.exists("create_enchantment_industry:printer")) {
            zincMachine(event, Item.of("create_enchantment_industry:printer", 1), "#c:storage_blocks/lapis")
        }

        // 2.5 preview removed hyper_experience — only restore if the fluid still exists
        if (Fluid.exists("create_enchantment_industry:hyper_experience")) {
            event.remove({ id:"create_enchantment_industry:mixing/hyper_experience"})
            event.recipes.create.mixing(Fluid.of("create_enchantment_industry:hyper_experience", 10), [
                "#forge:dusts/enderium",
                "minecraft:lapis_lazuli",
                "minecraft:glow_ink_sac",
                Fluid.of("create_enchantment_industry:experience", 100)
            ]).superheated()
        }

        event.replaceInput( {id: "create_enchantment_industry:crafting/enchanting_guide"}, "create:sturdy_sheet", "create:schedule" )
    })
}
