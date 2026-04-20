(() => {
    const hiddenNCMetals = ["platinum", "tin", "zinc", "bronze", "silver", "uranium", "steel", "cobalt", "aluminum", "electrum", "lead"]
    const hiddenEmbersMetals = ["iron", "zinc", "lead", "copper", "nickel", "gold", "tin", "aluminum", "boron", "calorite", "cobalt", "desh", "lithium", "magnesium", "ostrum", "platinum", "silver", "thorium", "anthralite", "uranium", "bronze", "electrum", "constantan", "invar", "brass"]
    const __hasRecipeViewerEvents = typeof RecipeViewerEvents !== "undefined"

    if (__hasRecipeViewerEvents) RecipeViewerEvents.removeEntries("item", event => {
        const hide = (entry) => event.remove(entry)
        hide("immersive_aircraft:boiler")
        // replaced with polished equivalent
        hide("create:rose_quartz")
        // tfmg meme stones
        // hide("tfmg:lignite")
        // hide(/.*tfmg.*galena.*/)
        // hide(/.*tfmg.*bauxite.*/)
        // useless entry
        // hide("nuclearcraft:portal")
        // redundant
        // hide("nuclearcraft:ethanol_bucket")
        // hide("nuclearcraft:redstone_ethanol_bucket")
        // duplicate
        hide("jaopca:storage_blocks.sodium_chloride")
        // we redstone now
        // hide("thermal:cinnabar_dust")
        // no recipes
        hide("createdeco:netherite_sheet")
        // fluid unification (buckets)
        // hide(/embers:molten_(?!dawnstone).*/)
        // hiddenNCMetals.forEach(metal => { hide("nuclearcraft:" + metal + "_bucket") })
        hide("createbigcannons:molten_steel_bucket")
        hide("createbigcannons:molten_bronze_bucket")
        // hide("tfmg:molten_steel_bucket")
        // crimes against humanity
        // hide("tfmg:casting_basin")
        // hide("tfmg:casting_spout")
        // hide("tfmg:block_mold")
        // hide("tfmg:ingot_mold")
        // removed
        hide("jaopca:create_crushed.netherite_scrap")
        hide("jaopca:dusts.netherite_scrap")
        hide("jaopca:molten.netherite_scrap")
        hide("jaopca:molten.coal")
        /*
    // coal coke unification - thermal
    // hide("tfmg:coal_coke")
    hide("immersiveengineering:coal_coke")
    // creosote unification - IE
    hide("thermal:creosote_bucket")
    // hide("tfmg:creosote_bucket")
    */
        // removed in favor of powergrid
        hide("createaddition:electric_motor")
        hide("createaddition:alternator")

        // pointless clone of basalt
        hide(/chisel:diabase.*/)
    })
    else if (typeof JEIEvents !== "undefined") JEIEvents.hideItems(event => {
        const hide = (entry) => event.hide(entry)
        // redundant create_sa fueling tanks
        // hide("create_sa:small_fueling_tank")
        // hide("create_sa:large_fueling_tank")
        // we've made this redundant
        hide("immersive_aircraft:boiler")
        // these too
        // hide("create_sa:heat_engine")
        // hide("create_sa:steam_engine")
        // hide("create_sa:hydraulic_engine")
        // replaced with polished equivalent
        hide("create:rose_quartz")
        // tfmg meme stones
        // hide(/.*tfmg.*galena.*/)
        // hide(/.*tfmg.*bauxite.*/)
        // useless entry
        // hide("nuclearcraft:portal")
        // redundant
        // hide("nuclearcraft:ethanol_bucket")
        // hide("nuclearcraft:redstone_ethanol_bucket")
        // duplicate
        hide("jaopca:storage_blocks.sodium_chloride")
        // we redstone now
        hide("thermal:cinnabar_dust")
        // no recipes
        hide("createdeco:netherite_sheet")
        // fluid unification (buckets)
        // hide(/embers:molten_(?!dawnstone).*/)
        // hiddenNCMetals.forEach(metal => { hide("nuclearcraft:" + metal + "_bucket") })
        hide("createbigcannons:molten_steel_bucket")
        hide("createbigcannons:molten_bronze_bucket")
        // hide("tfmg:molten_steel_bucket")
        // crimes against humanity
        // hide("tfmg:casting_basin")
        // hide("tfmg:casting_spout")
        // hide("tfmg:block_mold")
        // hide("tfmg:ingot_mold")
        // removed
        hide("jaopca:create_crushed.netherite_scrap")
        hide("jaopca:dusts.netherite_scrap")
        hide("jaopca:molten.netherite_scrap")
        hide("jaopca:molten.coal")
        /*
    // coal coke unification - thermal
    // hide("tfmg:coal_coke")
    hide("immersiveengineering:coal_coke")
    // creosote unification - IE
    hide("thermal:creosote_bucket")
    // hide("tfmg:creosote_bucket")
    */
        // removed in favor of powergrid
        hide("createaddition:electric_motor")
        hide("createaddition:alternator")

        // pointless clone of basalt
        hide(/chisel:diabase.*/)
    })

    if (__hasRecipeViewerEvents) RecipeViewerEvents.removeEntries("fluid", event => {
        const hide = (entry) => event.remove(entry)
        // redundant
        // hide("nuclearcraft:ethanol")
        // hide("nuclearcraft:redstone_ethanol")
        // metal fluid unification
        // hiddenEmbersMetals.forEach(metal => { hide("embers:molten_" + metal) })
        // hiddenNCMetals.forEach(metal => { hide("nuclearcraft:" + metal) })
        hide("createbigcannons:molten_steel")
        hide("createbigcannons:molten_bronze")
        // hide("tfmg:molten_steel")
        // coal dupe
        hide("jaopca:molten.coal")
    // creosote unification - IE
    // hide("thermal:creosote")
    // hide("tfmg:creosote")
    })
    else if (typeof JEIEvents !== "undefined") JEIEvents.hideFluids(event => {
        const hide = (entry) => event.hide(entry)
        // redundant
        // hide("nuclearcraft:ethanol")
        // hide("nuclearcraft:redstone_ethanol")
        // metal fluid unification
        // hiddenEmbersMetals.forEach(metal => { hide("embers:molten_" + metal) })
        // hiddenNCMetals.forEach(metal => { hide("nuclearcraft:" + metal) })
        hide("createbigcannons:molten_steel")
        hide("createbigcannons:molten_bronze")
        // hide("tfmg:molten_steel")
        // coal dupe
        hide("jaopca:molten.coal")
    // creosote unification - IE
    // hide("thermal:creosote")
    // hide("tfmg:creosote")
    })

    if (__hasRecipeViewerEvents) RecipeViewerEvents.addEntries("item", event => {
        event.add("ae2:ender_dust")// no idea why this is needed
    })
    else if (typeof JEIEvents !== "undefined") JEIEvents.addItems(event => {
        event.add("ae2:ender_dust")
    })
})();
