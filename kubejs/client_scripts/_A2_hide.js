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
    event.remove("jaopca:storage_blocks.sodium_chloride")
    // we redstone now
    // event.remove("thermal:cinnabar_dust")
    // no recipes
    event.remove("createdeco:netherite_sheet")
    // fluid unification (buckets)
    // event.remove(/embers:molten_(?!dawnstone).*/)
    // hiddenNCMetals.forEach(metal => { event.remove("nuclearcraft:" + metal + "_bucket") })
    // event.remove("createbigcannons:molten_steel_bucket")
    // event.remove("createbigcannons:molten_bronze_bucket")
    // event.remove("tfmg:molten_steel_bucket")
    // crimes against humanity
    // event.remove("tfmg:casting_basin")
    // event.remove("tfmg:casting_spout")
    // event.remove("tfmg:block_mold")
    // event.remove("tfmg:ingot_mold")
    // removed
    event.remove("jaopca:create_crushed.netherite_scrap")
    event.remove("jaopca:dusts.netherite_scrap")
    event.remove("jaopca:molten.netherite_scrap")
    event.remove("jaopca:molten.coal")
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