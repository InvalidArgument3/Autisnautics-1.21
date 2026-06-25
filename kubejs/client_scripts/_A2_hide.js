let hiddenNCMetals = ["platinum", "tin", "zinc", "bronze", "silver", "uranium", "steel", "cobalt", "aluminum", "electrum", "lead"]
let hiddenEmbersMetals = ["iron", "zinc", "lead", "copper", "nickel", "gold", "tin", "aluminum", "boron", "calorite", "cobalt", "desh", "lithium", "magnesium", "ostrum", "platinum", "silver", "thorium", "anthralite", "uranium", "bronze", "electrum", "constantan", "invar", "brass"]

RecipeViewerEvents.removeEntries("item", (event) => {
    event.remove("immersive_aircraft:boiler")
    // replaced with polished equivalent
    event.remove("create:rose_quartz")
    // tfmg meme stones
    // event.remove("tfmg:lignite")
    // event.remove(/.*tfmg.*galena.*/)
    // event.remove(/.*tfmg.*bauxite.*/)
    // useless entry
    // event.remove("nuclearcraft:portal")
    // redundant
    // event.remove("nuclearcraft:ethanol_bucket")
    // event.remove("nuclearcraft:redstone_ethanol_bucket")
    // duplicate
    if (Item.exists("jaopca:storage_blocks.sodium_chloride")) {
        event.remove("jaopca:storage_blocks.sodium_chloride")
    }
    // we redstone now
    // event.remove("thermal:cinnabar_dust")
    // no recipes
    event.remove("createdeco:netherite_sheet")
    let hideJaopca = (id) => { if (Item.exists(id)) event.remove(id) }
    hideJaopca("jaopca:create_crushed.netherite_scrap")
    hideJaopca("jaopca:dusts.netherite_scrap")
    hideJaopca("jaopca:molten.netherite_scrap")
    hideJaopca("jaopca:molten.coal")
    /*
    // coal coke unification - thermal
    // event.remove("tfmg:coal_coke")
    event.remove("immersiveengineering:coal_coke")
    // creosote unification - IE
    event.remove("thermal:creosote_bucket")
    // event.remove("tfmg:creosote_bucket")
    */
    // removed in favor of powergrid
    event.remove("createaddition:electric_motor")
    event.remove("createaddition:alternator")
    // pointless clone of basalt
    event.remove(/chisel:diabase.*/)
})

/*
RecipeViewerEvents.removeEntries("fluid", (event) => {
    // redundant
    // event.remove("nuclearcraft:ethanol")
    // event.remove("nuclearcraft:redstone_ethanol")
    // metal fluid unification
    // hiddenEmbersMetals.forEach(metal => { event.remove("embers:molten_" + metal) })
    // hiddenNCMetals.forEach(metal => { event.remove("nuclearcraft:" + metal) })
    // event.remove("createbigcannons:molten_steel")
    // event.remove("createbigcannons:molten_bronze")
    // event.remove("tfmg:molten_steel")
    // coal dupe
    // event.remove("jaopca:molten.coal")
// creosote unification - IE
// event.remove("thermal:creosote")
// event.remove("tfmg:creosote")
})
*/
